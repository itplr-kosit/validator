package org.kosit.validator.api;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.kosit.validator.config.ConfigurationBuilder;
import org.kosit.validator.config.ConfigurationLoader;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Scenario;

/**
 * Configuration of the actual {@link VCheck} instance. This is an interface and can be implemented by custom
 * configuration classes. There are two implementations supported out of the box:
 *
 * <ol>
 * <li>{@link ConfigurationLoader} implements loading {@link VCheck} configurations from a scenario.xml file</li>
 * <li>Using a builder style api {@link org.kosit.validator.config.ConfigurationBuilder}to configure the
 * {@link VCheck}</li>
 * </ol>
 * <p>
 * Both methods can be used via convinience methods. See below.
 *
 * @author Andreas Penski
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public interface VConfiguration {

    @Deprecated(since = "2.0.0", forRemoval = true)
    /**
     * Returns a list of configured scenarios.
     *
     * @return the list of scenarios
     */
    List<Scenario> getScenarios();

    @Deprecated(since = "2.0.0", forRemoval = true)
    /**
     * Returns the configured fallback scenario to use, in case no configured scenario match.
     *
     * @return the fallback scenario
     */
    Scenario getFallbackScenario();

    @Deprecated(since = "2.0.0", forRemoval = true)
    /**
     * Returns the author of this configuration.
     *
     * @return the author
     */
    String getAuthor();

    @Deprecated(since = "2.0.0", forRemoval = true)
    /**
     * Returns the name of the specification
     *
     * @return the name
     */
    String getName();

    @Deprecated(since = "2.0.0", forRemoval = true)
    /**
     * The creation date of the config
     *
     * @return the date
     */
    String getDate();

    @Deprecated(since = "2.0.0", forRemoval = true)
    /**
     * Add some additional parameters to the validator configuration. Parameter usage depends on actual implementation
     * of {@link VCheck}
     *
     * @return A Map containing the additional Parameters to be added.
     */
    Map<String, Object> getAdditionalParameters();

    @Deprecated(since = "2.0.0", forRemoval = true)
    /**
     * The content repository including resolving strategies.
     *
     * @return the configured {@link ContentRepository}
     */
    ContentRepository getContentRepository();

    /**
     * Loads an XML based scenario definition from the file specified via URI.
     *
     * @param scenarioDefinition the XML file with scenario definition
     * @return the loaded configuration
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    static ConfigurationLoader load(final URI scenarioDefinition) {
        return load(scenarioDefinition, null);
    }

    /**
     * Loads an XML based scenario definition from the file with an specific repository / source location specified via
     * URIs.
     *
     * @param scenarioDefinition the XML file with scenario definition
     * @return the loaded configuration
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    static ConfigurationLoader load(final URI scenarioDefinition, final URI repository) {
        return new ConfigurationLoader(scenarioDefinition, repository);
    }

    /**
     * Creates a {@link VConfiguration} based on a builder style API using {@link ConfigurationBuilder}
     *
     * @return the Builder
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    static ConfigurationBuilder create() {
        return new ConfigurationBuilder();
    }
}
