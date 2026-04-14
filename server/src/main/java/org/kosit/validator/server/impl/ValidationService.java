package org.kosit.validator.server.impl;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.s9api.Processor;
import org.kosit.validator.api.Configuration;
import org.kosit.validator.api.Input;
import org.kosit.validator.api.Result;
import org.kosit.validator.api.XmlError;
import org.kosit.validator.impl.DefaultCheck;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.EngineInformation;
import org.kosit.validator.impl.tasks.ScenarioSelectionAction;
import org.kosit.validator.impl.xml.ProcessorProvider;
import org.kosit.validator.model.mvrl.AcceptanceStatusType;
import org.kosit.validator.model.mvrl.MVRLCompactReport;
import org.kosit.validator.model.mvrl.ObjectFactory;
import org.kosit.validator.model.mvrl.ResultType;
import org.kosit.validator.server.config.ValidationConfig;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
@Startup
@Named("validationService")
public class ValidationService {

    private final Processor processor = ProcessorProvider.getProcessor();

    private final List<Configuration> configuration;

    private final EngineInformation engineInformation;

    private final DefaultCheck check;

    public ValidationService(final ValidationConfig cfg, final EngineInformation engineInformation) {
        this.configuration = getConfiguration(cfg, processor);
        this.engineInformation = engineInformation;
        check = new DefaultCheck(engineInformation, processor, configuration.toArray(new Configuration[0]));
        log.info("Validator started");
    }

    public List<Scenario> getScenarios() {
        return configuration != null ? configuration.stream().flatMap(c -> c.getScenarios().stream()).toList() : Collections.emptyList();
    }

    public Result validate(final Input input) {
        long t0 = System.currentTimeMillis();

        final Result result = check.checkInput(input);

        log.info("Validated {} input in {} ms", input.getName(), System.currentTimeMillis() - t0);
        return result;
    }

    public MVRLCompactReport convertMinimal(final Input input, final Result defaultResult) {
        return convertMinimal(Map.of(input.getName(), defaultResult));
    }

    public MVRLCompactReport convertMinimal(final Map<String, Result> defaultResults) {
        final ObjectFactory mvrlObjectFactory = new ObjectFactory();
        MVRLCompactReport compactReport = mvrlObjectFactory.createMVRLCompactReport();
        defaultResults.entrySet().stream().forEach(entry -> {
            final ResultType result = mvrlObjectFactory.createResultType();
            result.setFile(entry.getKey());
            final Result defaultResult = entry.getValue();
            result.setSchema(defaultResult.isSchemaValid());
            result.setSchematron(defaultResult.isSchematronValid());
            result.setAcceptance(AcceptanceStatusType.valueOf(defaultResult.getAcceptRecommendation().name()));
            result.setErrordescription(joinErrors(defaultResult));
            compactReport.getResult().add(result);
        });
        compactReport.setAcceptable(defaultResults.entrySet().stream().filter(e -> e.getValue().isAcceptable()).count());
        compactReport.setRejected(defaultResults.entrySet().stream().filter(e -> !e.getValue().isAcceptable()).count());
        compactReport.setProcessingerrors(defaultResults.entrySet().stream().filter(e -> !e.getValue().isProcessingSuccessful()).count());
        return compactReport;
    }

    private static String joinErrors(final Result value) {
        final StringBuilder b = new StringBuilder();
        b.append(String.join(";", value.getProcessingErrors()));
        if (value.getSchemaViolations() != null && !value.getSchemaViolations().isEmpty()) {
            b.append(b.length() > 0 ? ";" : "");
            b.append(value.getSchemaViolations().stream().map(XmlError::getMessage).collect(Collectors.joining(";")));
        }
        if (value.getSchematronResult() != null && !value.getSchematronResult().isEmpty()) {
            b.append(b.length() > 0 ? ";" : "");
            b.append(value.getSchematronResult().stream().flatMap(e -> e.getMessages().stream()).collect(Collectors.joining(";")));
        }
        return b.toString();
    }

    private static List<Configuration> getConfiguration(final ValidationConfig cfg, Processor processor) {
        return cfg.scenarios().stream().map(scenarioBundle -> {
            assertFileExistance(scenarioBundle.scenarioPath(), "scenario");
            final URI scenarioLocation = scenarioBundle.scenarioPath().toUri();
            final URI repositoryLocation = findRepository(scenarioLocation, scenarioBundle.repositoryOpt());

            return Configuration.load(scenarioLocation, repositoryLocation).build(processor);
        }).toList();
    }

    private static URI findRepository(final URI scenarioLocation, final Optional<Path> repositoryOpt) {
        final Path path = repositoryOpt.orElse(Paths.get(scenarioLocation).getParent());
        return determineRepository(path);
    }

    private static URI determineRepository(final Path d) {
        if (Files.isDirectory(d)) {
            return d.toUri();
        } else {
            throw new IllegalArgumentException(
                    String.format("Not a valid path for repository definition specified: '%s'", d.toAbsolutePath()));
        }
    }

    private static void assertFileExistance(final Path f, final String type) {
        if (!Files.isRegularFile(f)) {
            throw new IllegalArgumentException(
                    String.format("Not a valid path for %s definition specified: '%s'", type, f.toAbsolutePath()));
        }
    }

    /**
     * Is used for Readiness Healthcheck.
     * 
     * @return if at least 1 configuration available and loaded
     */
    public boolean isReady() {
        return configuration != null && !configuration.isEmpty();
    }

    /**
     * Is used for Readiness Healthcheck.
     * 
     * @return amount of configurations available and loaded
     */
    public int getConfigurationCount() {
        return configuration != null ? configuration.size() : 0;
    }
}
