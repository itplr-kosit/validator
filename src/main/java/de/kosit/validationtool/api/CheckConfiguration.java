/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.xml.ProcessorProvider;

/**
 * Central configuration of a validation instance.
 *
 * @author Andreas Penski
 * @deprecated since 1.3.0 use {@link Configuration} instead. Will be removed in 2.0
 */
@Deprecated
public class CheckConfiguration implements Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckConfiguration.class);

    /**
     * URL pointing to the scenario.xml file.
     */
    private final URI scenarioDefinition;

    /**
     * Root folder containing the files required by the individual scenarios.
     */
    private URI scenarioRepository;

    private Configuration delegate;

    private Configuration getDelegate() {
        if (this.delegate == null) {
            this.delegate = Configuration.load(this.scenarioDefinition, this.scenarioRepository).build(ProcessorProvider.getProcessor());
        }
        return this.delegate;
    }

    @Override
    public List<Scenario> getScenarios() {
        return getDelegate().getScenarios();
    }

    @Override
    public Scenario getFallbackScenario() {
        return getDelegate().getFallbackScenario();
    }

    @Override
    public String getDate() {
        return getDelegate().getDate();
    }

    @Override
    public Map<String, Object> getAdditionalParameters() {
        return this.delegate.getAdditionalParameters();
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    public String getAuthor() {
        return getDelegate().getAuthor();
    }

    @Override
    public ContentRepository getContentRepository() {
        return getDelegate().getContentRepository();
    }

    /**
     * URL pointing to the scenario.xml file.
     */
    public URI getScenarioDefinition() {
        return this.scenarioDefinition;
    }

    /**
     * Root folder containing the files required by the individual scenarios.
     */
    public URI getScenarioRepository() {
        return this.scenarioRepository;
    }

    /**
     * Root folder containing the files required by the individual scenarios.
     */
    public void setScenarioRepository(final URI scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }

    public void setDelegate(final Configuration delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a new {@code CheckConfiguration} instance.
     *
     * @param scenarioDefinition URL pointing to the scenario.xml file.
     */
    public CheckConfiguration(final URI scenarioDefinition) {
        this.scenarioDefinition = scenarioDefinition;
    }
}
