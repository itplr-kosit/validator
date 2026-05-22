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
package de.kosit.validationtool.config;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Scenario;

/**
 * Default implementation class for {@link Configuration}. This class contains all information to run a
 * {@link de.kosit.validationtool.impl.DefaultCheck}.
 * 
 * @author Andreas Penski
 */
public class DefaultConfiguration implements Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfiguration.class);

    private final List<Scenario> scenarios;

    private final Scenario fallbackScenario;

    private ContentRepository contentRepository;

    private String name;

    private String author;

    private String date;

    private Map<String, Object> additionalParameters;

    public DefaultConfiguration(final List<Scenario> scenarios, final Scenario fallbackScenario) {
        this.scenarios = scenarios;
        this.fallbackScenario = fallbackScenario;
    }

    public List<Scenario> getScenarios() {
        return this.scenarios;
    }

    public Scenario getFallbackScenario() {
        return this.fallbackScenario;
    }

    public ContentRepository getContentRepository() {
        return this.contentRepository;
    }

    public String getName() {
        return this.name;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getDate() {
        return this.date;
    }

    public Map<String, Object> getAdditionalParameters() {
        return this.additionalParameters;
    }

    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public void setDate(final String date) {
        this.date = date;
    }

    public void setAdditionalParameters(final Map<String, Object> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }
}
