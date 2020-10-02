/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kosit.validationtool.config;

import static de.kosit.validationtool.impl.DateFactory.createTimestamp;

import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.validation.Schema;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.ResolvingConfigurationStrategy;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.ResolvingMode;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.scenarios.DescriptionType;
import de.kosit.validationtool.model.scenarios.NoScenarioReportType;
import de.kosit.validationtool.model.scenarios.ObjectFactory;
import de.kosit.validationtool.model.scenarios.Scenarios;

import net.sf.saxon.s9api.Processor;

/**
 * Implements a builder style creation of a {@link Configuration}.
 * 
 * @author Andreas Penski
 */
@Slf4j
@Getter(AccessLevel.PACKAGE)
public class ConfigurationBuilder {

    private final List<ScenarioBuilder> scenarios = new ArrayList<>();

    private FallbackBuilder fallbackBuilder;

    private ResolvingConfigurationStrategy resolvingConfigurationStrategy;

    private ResolvingMode resolvingMode = ResolvingMode.STRICT_RELATIVE;

    private Processor processor;

    private String author = "API";

    private String date = LocalDate.now().toString();

    private String name = "Custom";

    private final Map<String, Object> parameters = new HashMap<>();

    private URI repository;

    private String description;

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
     * Sets a specific fallback scenario configuration. Note: calling this more than once is possible, but the last call
     * will define the actual fallback scenario used. There can be only one
     * 
     * @param builder the {@link FallbackBuilder}
     * @return this
     */
    public ConfigurationBuilder with(final FallbackBuilder builder) {
        if (this.fallbackBuilder != null) {
            log.warn("Overriding previously created fallback scenario");
        }
        this.fallbackBuilder = builder;
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
     * Create a fallback scenario configuration.
     *
     * @return the builder
     */
    public static FallbackBuilder fallback() {
        return new FallbackBuilder();
    }

    /**
     * Create the default fallback configuration if new scenario match. Note: this is public for explicit usage. If no
     * fallback is configured, this is the still default fallback.
     *
     * @return a fallback configuration
     */
    public static FallbackBuilder defaultFallback() {
        throw new NotImplementedException("Not yet defined");
    }

    /**
     * Create a named schematron configuration.
     * 
     * @param name the name of the schematron configuration
     * @return new {@link SchemaBuilder}
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
     * Create named report configuration.
     * 
     * @param name the name of the report
     * @return the report configuration builder
     */
    public static ReportBuilder report(final String name) {
        return new ReportBuilder().name(name);
    }

    /**
     * Builds the actual {@link Configuration} by validating all builder inputs and constructing neccessary objects.
     * 
     * @return a valid configuration
     * @throws IllegalStateException when the configuration is not valid/complete
     */
    public Configuration build() {
        final ResolvingConfigurationStrategy resolving = getResolvingConfigurationStrategy();
        if (this.processor == null) {
            this.processor = resolving.getProcessor();
        }
        final ContentRepository contentRepository = new ContentRepository(resolving, this.repository);

        final List<Scenario> list = initializeScenarios(contentRepository);
        final Scenario fallbackScenario = initializeFallback(contentRepository);
        final DefaultConfiguration configuration = new DefaultConfiguration(list, fallbackScenario);
        configuration.setAdditionalParameters(this.parameters);
        configuration.setAuthor(this.author);
        configuration.setDate(this.date);
        configuration.setName(this.name);
        configuration.setContentRepository(contentRepository);
        configuration.getAdditionalParameters().put(Keys.SCENARIO_DEFINITION, createDefinition(configuration));
        return (configuration);
    }

    private Scenarios createDefinition(final DefaultConfiguration configuration) {
        final Scenarios s = new Scenarios();
        s.setAuthor(configuration.getAuthor());
        s.setDate(createTimestamp());
        final DescriptionType d = new DescriptionType();
        d.getPOrOlOrUl().add(new ObjectFactory().createDescriptionTypeP(StringUtils.defaultIfBlank(this.description, "")));
        s.setDescription(d);
        s.setName(configuration.getName());
        s.getScenario().addAll(configuration.getScenarios().stream().map(Scenario::getConfiguration).collect(Collectors.toList()));
        s.setNoScenarioReport(createNoScenarioReportType(configuration.getFallbackScenario()));
        return s;
    }

    private static NoScenarioReportType createNoScenarioReportType(final Scenario fallbackScenario) {
        final NoScenarioReportType no = new NoScenarioReportType();
        no.setResource(fallbackScenario.getConfiguration().getCreateReport().getResource());
        return no;
    }

    private Scenario initializeFallback(final ContentRepository contentRepository) {
        if (this.fallbackBuilder == null) {
            throw new IllegalStateException("No fallback configuration specified");
        }
        final Result<Scenario, String> result = this.fallbackBuilder.build(contentRepository);
        if (result.isInvalid()) {
            throw new IllegalStateException("Invalid fallback configuration: " + String.join(",", result.getErrors()));
        }
        return result.getObject();
    }

    private List<Scenario> initializeScenarios(final ContentRepository contentRepository) {
        if (this.scenarios.isEmpty()) {
            throw new IllegalStateException("No scenario specified");
        }
        return this.scenarios.stream().map(s -> {
            final Result<Scenario, String> result = s.build(contentRepository);
            if (result.isInvalid()) {
                final String msg = String.join(",", result.getErrors());
                throw new IllegalStateException(String.format("Invalid configuration for scenario %s found: %s", s.getName(), msg));
            }
            return result.getObject();
        }).collect(Collectors.toList());
    }

    private ResolvingConfigurationStrategy getResolvingConfigurationStrategy() {
        if (this.resolvingConfigurationStrategy != null) {
            log.info("Custom resolving strategy supplied. Please take care of xml security!");
            return this.resolvingConfigurationStrategy;
        }
        log.info("Using resolving strategy {}", this.resolvingMode);
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
     * Set a specific repository location for resolving artifacts for scenarios.
     *
     * @param repository the repository location
     * @return this
     */
    public ConfigurationBuilder useRepository(final Path repository) {
        return useRepository(repository.toUri());
    }
}
