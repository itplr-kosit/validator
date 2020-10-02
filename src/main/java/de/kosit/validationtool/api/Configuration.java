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

package de.kosit.validationtool.api;

import java.net.URI;
import java.util.List;
import java.util.Map;

import de.kosit.validationtool.config.ConfigurationBuilder;
import de.kosit.validationtool.config.ConfigurationLoader;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Scenario;

/**
 * Configuration of the actual {@link Check} instance. This is an interface and can be implemented by custom
 * configuration classes. There are two implementations supported out of the box:
 *
 * <ol>
 * <li>{@link ConfigurationLoader} implements loading {@link Check} configurations from a scenario.xml file</li>
 * <li>Using a builder style api {@link de.kosit.validationtool.config.ConfigurationBuilder}to configure the
 * {@link Check}</li>
 * </ol>
 * <p>
 * Both methods can be used via convinience methods. See below.
 *
 * @author Andreas Penski
 */

public interface Configuration {

    /**
     * Returns a list of configured scenarios.
     *
     * @return the list of scenarios
     */
    List<Scenario> getScenarios();

    /**
     * Returns the configured fallback scenario to use, in case no configured scenario match.
     *
     * @return the fallback scenario
     */
    Scenario getFallbackScenario();

    /**
     * Returns the author of this configuration.
     *
     * @return the author
     */
    String getAuthor();

    /**
     * Returns the name of the specification
     *
     * @return the name
     */
    String getName();

    /**
     * The creation date of the config
     *
     * @return the date
     */
    String getDate();

    /**
     * Add some additional parameters to the validator configuration. Parameter usage depends on actual implementation
     * of {@link Check}
     *
     * @return
     */
    Map<String, Object> getAdditionalParameters();

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
    static ConfigurationLoader load(final URI scenarioDefinition, final URI repository) {
        return new ConfigurationLoader(scenarioDefinition, repository);
    }

    /**
     * Creates a {@link Configuration} based on a builder style API using {@link ConfigurationBuilder}
     *
     * @return the Builder
     */
    static ConfigurationBuilder create() {
        return new ConfigurationBuilder();
    }
}
