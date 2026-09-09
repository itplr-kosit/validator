package org.kosit.validator.server.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.conformatron.api.model.source.CTReadResource;
import org.jspecify.annotations.Nullable;
import org.kosit.base.xml.XmlHelper;
import org.kosit.validator.api.ScenarioSet;
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

    private static final String XSLT_NAMESPACE = "http://www.w3.org/1999/XSL/Transform";

    private static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    private static final String SCHEMATRON_NAMESPACE = "http://purl.oclc.org/dsdl/schematron";

    /** Upper bound of an unpacked repository ZIP, against a zip bomb. */
    private static final long MAX_REPOSITORY_BYTES = 256L * 1024 * 1024;

    private final List<ScenarioSet> configuration;

    private final ConformanceValidation engine;

    private final EngineInformation engineInformation;

    private final ValidationRunStore results;

    public ValidationService(final ValidationConfig cfg, final EngineInformation engineInformation, final ValidationRunStore results) {
        this.engineInformation = engineInformation;
        this.configuration = getConfiguration(cfg, this.processor);
        this.engine = new ConformanceValidation(engineInformation, this.processor, this.configuration.toArray(new ScenarioSet[0]));
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
        return store(input, this.engine.validate(input), System.currentTimeMillis());
    }

    /**
     * A validation artifact posted as a single file.
     *
     * @param fileName the name the client sent, or {@code null}; decides the kind when it has a known extension
     * @param content the bytes
     */
    public record PostedResource(@Nullable String fileName, byte[] content) {
    }

    /**
     * Creates an ad hoc validation run: validates the document against the given artifacts alone - the same pipeline
     * over one scenario assembled from them ({@link ConformanceValidation#adHoc}) - and keeps its CVR like every other
     * run.
     * <p>
     * The artifacts are written to a directory of their own, which is the repository of the scenario, and removed once
     * the run is over. A repository ZIP is unpacked into it with its paths, so includes and imports between its entries
     * resolve; the named entries are the rule sets. A single posted resource is named after the file name the client
     * sent, or - without a usable one - after its root element ({@code resource-N.xsd}, {@code .sch}, {@code .xsl}).
     * </p>
     *
     * @param input the document to validate
     * @param resources artifacts posted as single files, applied in this order after the repository entries
     * @param repositoryZip a ZIP that becomes the repository, or {@code null}
     * @param artifactEntries the entries of the ZIP that are applied as rule sets, in this order
     * @return the identifier of the run, under which the result can be fetched
     * @throws IllegalArgumentException if no artifact results, an entry is missing, a name escapes the repository or a
     *             resource is of no supported kind - the caller's mistake, a {@code 400}
     */
    public UUID createAdHocRun(final CTReadResource input, final List<PostedResource> resources, final byte @Nullable [] repositoryZip,
            final List<String> artifactEntries) {
        final long t0 = System.currentTimeMillis();
        final Path directory;
        try {
            directory = Files.createTempDirectory("validator-adhoc-");
        } catch (final IOException e) {
            throw new UncheckedIOException("Can not create the repository of the ad hoc run", e);
        }
        try {
            final List<URI> artifacts = new ArrayList<>();
            if (repositoryZip != null) {
                unzip(repositoryZip, directory);
            }
            for (final String entry : artifactEntries) {
                final Path artifact = inside(directory, entry);
                if (!Files.isRegularFile(artifact)) {
                    throw new IllegalArgumentException("The repository has no entry '" + entry + "'");
                }
                artifacts.add(artifact.toUri());
            }
            int count = 0;
            for (final PostedResource resource : resources) {
                count++;
                final Path artifact = inside(directory, fileNameFor(resource, count));
                Files.write(artifact, resource.content());
                artifacts.add(artifact.toUri());
            }
            if (artifacts.isEmpty()) {
                throw new IllegalArgumentException(
                        "An ad hoc validation needs at least one artifact: a 'resource' part, or a 'repository' with 'artifact' entries");
            }
            final ConformanceValidation adHoc = ConformanceValidation.adHoc(this.engineInformation, this.processor, artifacts,
                    directory.toUri(), false);
            return store(input, adHoc.validate(input), t0);
        } catch (final IOException e) {
            throw new UncheckedIOException("Can not write the artifacts of the ad hoc run", e);
        } finally {
            deleteQuietly(directory);
        }
    }

    /** A path within the repository directory; a name that escapes it (zip slip, {@code ..}) is refused. */
    private static Path inside(final Path directory, final String name) {
        final Path resolved = directory.resolve(name).normalize();
        if (!resolved.startsWith(directory)) {
            throw new IllegalArgumentException("The name '" + name + "' escapes the repository");
        }
        return resolved;
    }

    private static void unzip(final byte[] zip, final Path directory) throws IOException {
        long total = 0;
        try ( ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip)) ) {
            for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
                final Path target = inside(directory, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                    continue;
                }
                Files.createDirectories(target.getParent());
                final byte[] content = in.readAllBytes();
                total += content.length;
                if (total > MAX_REPOSITORY_BYTES) {
                    throw new IllegalArgumentException("The repository ZIP unpacks to more than " + MAX_REPOSITORY_BYTES + " bytes");
                }
                Files.write(target, content);
            }
        }
    }

    /**
     * The file name a posted resource is stored under: the name the client sent if it carries a supported extension,
     * otherwise a generated one with the extension its root element calls for.
     */
    static String fileNameFor(final PostedResource resource, final int count) {
        if (resource.fileName() != null) {
            final String base = resource.fileName().replace('\\', '/');
            final String name = base.substring(base.lastIndexOf('/') + 1);
            final String lower = name.toLowerCase(Locale.ROOT);
            if (!name.isBlank()
                    && (lower.endsWith(".xsd") || lower.endsWith(".sch") || lower.endsWith(".xsl") || lower.endsWith(".xslt"))) {
                return name;
            }
        }
        return "resource-" + count + kindOf(resource.content());
    }

    /**
     * The extension for the kind of artifact the bytes are, read from the root element: {@code .xsd} for an XML Schema,
     * {@code .sch} for a Schematron, {@code .xsl} for a stylesheet (a precompiled Schematron).
     *
     * @throws IllegalArgumentException for anything else - not XML, or a root element of none of the three kinds
     */
    static String kindOf(final byte[] content) {
        try {
            final XMLStreamReader reader = XmlHelper.createSecureXmlInputFactory().createXMLStreamReader(new ByteArrayInputStream(content));
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                    final String namespace = reader.getNamespaceURI();
                    if (XSD_NAMESPACE.equals(namespace)) {
                        return ".xsd";
                    }
                    if (SCHEMATRON_NAMESPACE.equals(namespace)) {
                        return ".sch";
                    }
                    if (XSLT_NAMESPACE.equals(namespace)) {
                        return ".xsl";
                    }
                    throw new IllegalArgumentException("Unsupported artifact: the root element {" + namespace + "}" + reader.getLocalName()
                            + " is neither an XML Schema, a Schematron nor a stylesheet");
                }
            }
        } catch (final XMLStreamException e) {
            throw new IllegalArgumentException("Unsupported artifact: not well-formed XML (" + e.getMessage() + ")", e);
        }
        throw new IllegalArgumentException("Unsupported artifact: no root element");
    }

    private UUID store(final CTReadResource input, final ConformanceValidationResult result, final long started) {
        final ByteArrayOutputStream cvr = new ByteArrayOutputStream();
        try {
            result.writeCvr(cvr);
        } catch (final IOException e) {
            throw new UncheckedIOException("Can not serialize the report of " + input.getName(), e);
        }
        final UUID id = this.results.put(cvr.toByteArray());
        LOGGER.info("Validated {} in {} ms — {} — run {}", input.getName(), System.currentTimeMillis() - started, result.getDecision(), id);
        return id;
    }

    private static void deleteQuietly(final Path directory) {
        try ( Stream<Path> files = Files.walk(directory) ) {
            files.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (final IOException e) {
                    LOGGER.warn("Can not delete {}", p, e);
                }
            });
        } catch (final IOException e) {
            LOGGER.warn("Can not remove the repository of the ad hoc run {}", directory, e);
        }
    }

    /**
     * @param id the identifier of a run
     * @return its CVR, if the run is known and its result has not expired
     */
    public Optional<byte[]> getResult(final UUID id) {
        return this.results.get(id);
    }

    private static List<ScenarioSet> getConfiguration(final ValidationConfig cfg, final Processor processor) {
        return cfg.scenarios().stream().map(scenarioBundle -> {
            // Normalized, because a configured path containing ".." survives Path.toUri() and then no longer matches
            // the repository base URI that the artifact resolution compares against.
            final Path scenarioPath = scenarioBundle.scenarioPath().toAbsolutePath().normalize();
            assertFileExistance(scenarioPath, "scenario");
            final URI scenarioLocation = scenarioPath.toUri();
            final URI repositoryLocation = findRepository(scenarioLocation, scenarioBundle.repositoryOpt());
            return ScenarioSet.load(scenarioLocation, repositoryLocation).build(processor);
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
