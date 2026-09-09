package org.kosit.validator.api;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.kosit.validator.config.ConfigurationBuilder;
import org.kosit.validator.config.ConfigurationLoader;
import org.kosit.validator.impl.Scenario;

/**
 * The scenarios of one configuration - a {@code scenarios.xml} file or a configuration assembled through the
 * {@link ConfigurationBuilder builder API} - together with the identity of that configuration: name, author, date and
 * where it was read from.
 * <p>
 * The engine works on the {@link #getScenarios() scenarios} alone (ADR-008: configuration is a construction concern,
 * the scenarios are the construction input). The identity is for whoever assembles the engine and wants to tell the
 * user what was loaded, and for the report metadata.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class ScenarioSet {

    private final String name;

    private final @Nullable String author;

    private final @Nullable String date;

    private final @Nullable String definitionFile;

    private final List<Scenario> scenarios;

    /**
     * @param name the name of the configuration
     * @param author its author; may be {@code null}
     * @param date its date; may be {@code null}
     * @param definitionFile where it was read from; {@code null} if it was assembled in code
     * @param scenarios its scenarios, in configuration order; at least one
     */
    public ScenarioSet(final String name, final @Nullable String author, final @Nullable String date, final @Nullable String definitionFile,
            final List<Scenario> scenarios) {
        this.name = Objects.requireNonNull(name, "name may not be null");
        this.author = author;
        this.date = date;
        this.definitionFile = definitionFile;
        this.scenarios = List.copyOf(scenarios);
        if (this.scenarios.isEmpty()) {
            throw new IllegalArgumentException("A scenario set needs at least one scenario");
        }
    }

    /**
     * Loads a scenario configuration from a {@code scenarios.xml}; the artifact repository is the directory of that
     * file.
     *
     * @param scenarioDefinition URI of the scenarios.xml
     * @return the loader, to be configured and {@link ConfigurationLoader#build(net.sf.saxon.s9api.Processor) built}
     */
    public static ConfigurationLoader load(final URI scenarioDefinition) {
        return load(scenarioDefinition, null);
    }

    /**
     * Loads a scenario configuration from a {@code scenarios.xml} with an explicit artifact repository.
     *
     * @param scenarioDefinition URI of the scenarios.xml
     * @param repository the artifact repository; {@code null} for the directory of the scenarios.xml
     * @return the loader, to be configured and {@link ConfigurationLoader#build(net.sf.saxon.s9api.Processor) built}
     */
    public static ConfigurationLoader load(final URI scenarioDefinition, final @Nullable URI repository) {
        return new ConfigurationLoader(scenarioDefinition, repository);
    }

    /**
     * @return a builder assembling a scenario configuration in code
     */
    public static ConfigurationBuilder create() {
        return new ConfigurationBuilder();
    }

    public String getName() {
        return this.name;
    }

    public @Nullable String getAuthor() {
        return this.author;
    }

    public @Nullable String getDate() {
        return this.date;
    }

    /** @return where the configuration was read from; {@code null} if it was assembled in code */
    public @Nullable String getDefinitionFile() {
        return this.definitionFile;
    }

    /** @return the scenarios, in configuration order */
    public List<Scenario> getScenarios() {
        return this.scenarios;
    }

    @Override
    public String toString() {
        return "ScenarioSet[" + this.name + ", " + this.scenarios.size() + " scenarios]";
    }
}
