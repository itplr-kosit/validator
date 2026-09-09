package org.kosit.validator.config;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.validation.Schema;

import org.kosit.schematron.ContentRepository;
import org.kosit.schematron.resolve.ResolvingConfigurationStrategy;
import org.kosit.schematron.resolve.ResolvingMode;
import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;

/**
 * Implements a builder style creation of a {@link ScenarioSet}: the scenarios assembled in code instead of read from a
 * scenarios.xml.
 * <p>
 * Unlike the {@link ConfigurationLoader}, the builder checks the artifacts it is given right away - a location that
 * does not resolve or compile is an error of the code that assembles the configuration, and it is reported as such when
 * {@link #build(Processor)} is called. The compiled artifacts stay in the cache of the content repository, so the
 * pipeline does not compile them a second time.
 * </p>
 *
 * @author Andreas Penski
 */
public class ConfigurationBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationBuilder.class);

    private final List<ScenarioBuilder> scenarios = new ArrayList<>();

    private ResolvingConfigurationStrategy resolvingConfigurationStrategy;

    private ResolvingMode resolvingMode = ResolvingMode.STRICT_RELATIVE;

    private ContentRepository contentRepository;

    private String author = "API";

    private String date = LocalDate.now().toString();

    private String name = "Custom";

    private URI repository;

    private String description;

    /**
     * Create a named schematron configuration.
     *
     * @param name the name of the schematron configuration
     * @return new {@link SchematronBuilder}
     */
    public static SchematronBuilder schematron(final String name) {
        return new SchematronBuilder().name(name);
    }

    /**
     * Create a new schema validation configuration.
     *
     * @return a configuration builder for schema
     */
    public static SchemaBuilder schema() {
        return new SchemaBuilder();
    }

    /**
     * Create a new schema validation configuration.
     *
     * @param name the name of the schema
     * @param schema the actual precompiled schema to use
     * @return a configuration builder for schema
     */
    public static SchemaBuilder schema(final String name, final Schema schema) {
        return new SchemaBuilder().name(name).schema(schema);
    }

    /**
     * Create a new schema validation configuration.
     *
     * @param name the name of the schema
     * @return a configuration builder for schema
     */
    public static SchemaBuilder schema(final String name) {
        return new SchemaBuilder().name(name);
    }

    /**
     * Create a new schema validation configuration.
     *
     * @param uri the uri location of the schema
     * @return a configuration builder for schema
     */
    public static SchemaBuilder schema(final URI uri) {
        return new SchemaBuilder().schemaLocation(uri);
    }

    /**
     * Create a new named scenario configuration.
     *
     * @param name the name of the scenario
     * @return the scenario configuration builder
     */
    public static ScenarioBuilder scenario(final String name) {
        return new ScenarioBuilder().name(name);
    }

    /**
     * Create a new scenario configuration.
     *
     * @return the scenario configuration builder
     */
    public static ScenarioBuilder scenario() {
        return scenario(null);
    }

    /**
     * Add a specific author name to this configuration.
     *
     * @param authorName the name of the author
     * @return this
     */
    public ConfigurationBuilder author(final String authorName) {
        this.author = authorName;
        return this;
    }

    /**
     * Add a specific nam to this configuration
     *
     * @param name the name of the configuration
     * @return this
     */
    public ConfigurationBuilder name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the date for this configuration.
     *
     * @param date the date
     * @return this
     */
    public ConfigurationBuilder date(final LocalDate date) {
        if (date != null) {
            this.date = date.toString();
        }
        return this;
    }

    /**
     * Sets the date for this configuration.
     *
     * @param date the date
     * @return this
     */
    public ConfigurationBuilder date(final Date date) {
        return date(date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null);
    }

    /**
     * Adds a {@link Scenario} to this list of know scenarios. Note: order of calling this methods defines order of
     * scenarios when determining the target scenario for a given xml file.
     *
     * @param scenarioBuilder the {@link ScenarioBuilder} building the {@link Scenario}
     * @return this
     */
    public ConfigurationBuilder with(final ScenarioBuilder scenarioBuilder) {
        this.scenarios.add(scenarioBuilder);
        return this;
    }

    /**
     * Adds a description to this configuration.
     *
     * @param description the descriptioin
     * @return this
     */
    public ConfigurationBuilder description(final String description) {
        this.description = description;
        return this;
    }

    /**
     * Builds the actual {@link ScenarioSet} by validating all builder inputs and constructing necessary objects.
     *
     * @param processor the Saxon processor the scenarios are compiled with - the same the engine runs with
     * @return a valid configuration
     * @throws IllegalStateException when the configuration is not valid/complete
     */
    public ScenarioSet build(final Processor processor) {
        final ContentRepository contentRepository = resolveContentRepository(processor);
        final List<Scenario> list = initializeScenarios(contentRepository);
        return new ScenarioSet(this.name, this.author, this.date, null, list);
    }

    private ContentRepository resolveContentRepository(final Processor processor) {
        if (this.contentRepository == null) {
            if (this.repository == null) {
                this.repository = Paths.get("").toAbsolutePath().toUri();
                LOGGER.warn("No repository configured, resolving artifacts relative to the working directory {}", this.repository);
            }
            final ResolvingConfigurationStrategy resolving = getResolvingConfigurationStrategy();
            this.contentRepository = new ContentRepository(processor, resolving, this.repository);
        } else if (this.resolvingConfigurationStrategy != null) {
            LOGGER.warn("Ignore definition of resolve strategy since a custom ContentRepository is supplied");
        }
        return this.contentRepository;
    }

    private List<Scenario> initializeScenarios(final ContentRepository contentRepository) {
        if (this.scenarios.isEmpty()) {
            throw new IllegalStateException("No scenario specified");
        }
        return this.scenarios.stream().map(s -> {
            final SingleProcessingResult<Scenario, String> result = s.build(contentRepository);
            if (result.isInvalid()) {
                final String msg = String.join(",", result.getErrors());
                throw new IllegalStateException("Invalid configuration for scenario " + s.getName() + " found: " + msg);
            }
            return result.getObject();
        }).toList();
    }

    private ResolvingConfigurationStrategy getResolvingConfigurationStrategy() {
        if (this.resolvingConfigurationStrategy != null) {
            LOGGER.info("Custom resolving strategy supplied. Please take care of xml security!");
            return this.resolvingConfigurationStrategy;
        }
        LOGGER.info("Using resolving strategy {}", this.resolvingMode);
        return this.resolvingMode.getStrategy();
    }

    /**
     * Sets a specific resolving mode, for resolving xml artifacts for this configuration. See {@link ResolvingMode} for
     * details.
     *
     * @param mode the mode
     * @return this
     */
    public ConfigurationBuilder resolvingMode(final ResolvingMode mode) {
        this.resolvingMode = mode;
        return this;
    }

    /**
     * Sets a specific strategy to use for resolving artefacts for scenarios.
     *
     * @param strategy the strategy
     * @return this
     */
    public ConfigurationBuilder resolvingStrategy(final ResolvingConfigurationStrategy strategy) {
        this.resolvingConfigurationStrategy = strategy;
        return this;
    }

    /**
     * Set a specific repository location for resolving artifacts for scenarios.
     *
     * @param repository the repository location
     * @return this
     */
    public ConfigurationBuilder useRepository(final URI repository) {
        this.repository = repository;
        return this;
    }

    /**
     * Set a specific, pre-configured {@link ContentRepository} for resolving artifacts for scenarios.
     *
     * @param repository the repository location
     * @return this
     */
    public ConfigurationBuilder useRepository(final ContentRepository repository) {
        this.contentRepository = repository;
        return this;
    }

    /**
     * Set a specific repository location for resolving artifacts for scenarios.
     *
     * @param repository the repository location
     * @return this
     */
    public ConfigurationBuilder useRepository(final Path repository) {
        return useRepository(repository.toUri());
    }

    List<ScenarioBuilder> getScenarios() {
        return this.scenarios;
    }

    ResolvingMode getResolvingMode() {
        return this.resolvingMode;
    }

    ContentRepository getContentRepository() {
        return this.contentRepository;
    }

    String getAuthor() {
        return this.author;
    }

    String getDate() {
        return this.date;
    }

    String getName() {
        return this.name;
    }

    URI getRepository() {
        return this.repository;
    }

    String getDescription() {
        return this.description;
    }
}
