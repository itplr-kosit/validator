package de.kosit.validationtool.api;

import java.net.URI;
import java.util.List;

import de.kosit.validationtool.config.ConfigurationBuilder;
import de.kosit.validationtool.config.LoadConfiguration;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Scenario;

import net.sf.saxon.s9api.Processor;

/**
 * Configuration of the actual {@link Check} instance. This is a contruct and can be used implemented by custom
 * configuration classes. There are two implementations supported out of the box:
 * 
 * <ol>
 * <li>{@link LoadConfiguration} implements loading {@link Check} configurations from a scenario.xml file</li>
 * <li>Using a builder style api {@link de.kosit.validationtool.config.ConfigurationBuilder}to configure the
 * {@link Check}</li>
 * </ol>
 * 
 * Both methods can be used via convinience methods. See below.
 * 
 * @author Andreas Penski
 */

public interface Configuration {

    List<Scenario> getScenarios();

    static LoadConfiguration load(final URI scenarioDefinition) {
        return load(scenarioDefinition, null);
    }

    static LoadConfiguration load(final URI scenarioDefinition, final URI repository) {
        final LoadConfiguration config = new LoadConfiguration(scenarioDefinition, repository);
        config.build();
        return config;
    }

    static ConfigurationBuilder create() {
        return new ConfigurationBuilder();
    }

    Scenario getFallbackScenario();

    void build();

    String getAuthor();

    String getName();

    String getDate();

    Processor getProcessor();

    ContentRepository getContentRepository();
}
