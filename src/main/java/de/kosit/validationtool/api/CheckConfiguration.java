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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Scenario;

/**
 * Zentrale Konfigration einer Prüf-Instanz.
 * 
 * @author Andreas Penski
 * @deprecated since 1.3.0 use {@link Configuration} instead
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
@Deprecated
public class CheckConfiguration implements Configuration {

    /**
     * URL, die auf die scenerio.xml Datei zeigt.
     */
    private final URI scenarioDefinition;

    /**
     * Root-Ordner mit den von den einzelnen Szenarien benötigten Dateien
     */
    private URI scenarioRepository;

    private Configuration delegate;

    private Configuration getDelegate() {
        if (this.delegate == null) {
            this.delegate = Configuration.load(this.scenarioDefinition, this.scenarioRepository).build();
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
}
