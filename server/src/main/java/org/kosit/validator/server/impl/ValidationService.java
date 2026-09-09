package org.kosit.validator.server.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.conformatron.api.model.source.CTReadResource;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.ConformanceValidation;
import org.kosit.validator.impl.EngineInformation;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kosit.validator.server.config.ValidationConfig;
import org.kost.validator.api.saxon.ProcessorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import net.sf.saxon.s9api.Processor;

/**
 * The validator behind the REST resource: runs the canonical pipeline over a document and keeps the resulting CVR so
 * that it can be fetched by the identifier of its run.
 * <p>
 * The engine is {@link ConformanceValidation} — the same one the CLI drives. What this service adds is the resource
 * semantics: a run has an identity, its result outlives the request that created it, and the report is the report the
 * engine wrote, byte for byte.
 * </p>
 */
@ApplicationScoped
@Startup
@Named("validationService")
public class ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationService.class);

    private final Processor processor = ProcessorProvider.getProcessor();

    private final List<VConfiguration> configuration;

    private final ConformanceValidation engine;

    private final ValidationRunStore results;

    public ValidationService(final ValidationConfig cfg, final EngineInformation engineInformation, final ValidationRunStore results) {
        this.configuration = getConfiguration(cfg, this.processor);
        this.engine = new ConformanceValidation(engineInformation, this.processor, this.configuration.toArray(new VConfiguration[0]));
        this.results = results;
        LOGGER.info("Validator started");
    }

    public List<Scenario> getScenarios() {
        return this.configuration != null ? this.configuration.stream().flatMap(c -> c.getScenarios().stream()).toList()
                : Collections.emptyList();
    }

    /**
     * Creates a validation run: validates the document and keeps its CVR.
     *
     * @param input the document to validate
     * @return the identifier of the run, under which the result can be fetched
     */
    public UUID createRun(final CTReadResource input) {
        final long t0 = System.currentTimeMillis();
        final ConformanceValidationResult result = this.engine.validate(input);
        final ByteArrayOutputStream cvr = new ByteArrayOutputStream();
        try {
            result.writeCvr(cvr);
        } catch (final IOException e) {
            throw new UncheckedIOException("Can not serialize the report of " + input.getName(), e);
        }
        final UUID id = this.results.put(cvr.toByteArray());
        LOGGER.info("Validated {} in {} ms — {} — run {}", input.getName(), System.currentTimeMillis() - t0, result.getDecision(), id);
        return id;
    }

    /**
     * @param id the identifier of a run
     * @return its CVR, if the run is known and its result has not expired
     */
    public Optional<byte[]> getResult(final UUID id) {
        return this.results.get(id);
    }

    private static List<VConfiguration> getConfiguration(final ValidationConfig cfg, final Processor processor) {
        return cfg.scenarios().stream().map(scenarioBundle -> {
            // Normalized, because a configured path containing ".." survives Path.toUri() and then no longer matches
            // the repository base URI that the artifact resolution compares against.
            final Path scenarioPath = scenarioBundle.scenarioPath().toAbsolutePath().normalize();
            assertFileExistance(scenarioPath, "scenario");
            final URI scenarioLocation = scenarioPath.toUri();
            final URI repositoryLocation = findRepository(scenarioLocation, scenarioBundle.repositoryOpt());
            return VConfiguration.load(scenarioLocation, repositoryLocation).build(processor);
        }).toList();
    }

    private static URI findRepository(final URI scenarioLocation, final Optional<Path> repositoryOpt) {
        final Path path = repositoryOpt.orElse(Paths.get(scenarioLocation).getParent());
        return determineRepository(path);
    }

    private static URI determineRepository(final Path d) {
        final Path repository = d.toAbsolutePath().normalize();
        if (Files.isDirectory(repository)) {
            return repository.toUri();
        }
        throw new IllegalArgumentException("Not a valid path for repository definition specified: '" + repository + "'");
    }

    private static void assertFileExistance(final Path f, final String type) {
        if (!Files.isRegularFile(f)) {
            throw new IllegalArgumentException("Not a valid path for " + type + " definition specified: '" + f.toAbsolutePath() + "'");
        }
    }

    /**
     * Is used for Readiness Healthcheck.
     *
     * @return if at least 1 configuration available and loaded
     */
    public boolean isReady() {
        return this.configuration != null && !this.configuration.isEmpty();
    }

    /**
     * Is used for Readiness Healthcheck.
     *
     * @return amount of configurations available and loaded
     */
    public int getConfigurationCount() {
        return this.configuration != null ? this.configuration.size() : 0;
    }
}
