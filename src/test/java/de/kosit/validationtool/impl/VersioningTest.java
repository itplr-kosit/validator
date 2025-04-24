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

package de.kosit.validationtool.impl;

import de.kosit.validationtool.model.scenarios.Scenarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testet die Versionierung von Scenario-Dateien aka Konfigurationsdaten.
 *
 * @author Andreas Penski
 */
class VersioningTest {

    private static final URL BASE = VersioningTest.class.getResource("/examples/versioning/scenarios-base.xml");

    private static final URL INCREMENT = VersioningTest.class.getResource("/examples/versioning/scenarios-increment.xml");

    private static final URL NEW_FEATURE = VersioningTest.class.getResource("/examples/versioning/scenarios-newfeature.xml");

    private static final URL NEW_VERSION = VersioningTest.class.getResource("/examples/versioning/scenarios-newversion.xml");

    private ConversionService service;

    @BeforeEach
    void setup() {
        this.service = new ConversionService();
    }

    @Test
    void base() throws URISyntaxException {
        assert BASE != null;
        final Scenarios result = this.service.readXml(BASE.toURI(), Scenarios.class, SchemaProvider.getScenarioSchema());
        assertThat(result).isNotNull();
    }

    @Test
    void frameworkIncrement() throws URISyntaxException {
        assert INCREMENT != null;
        final Scenarios result = this.service.readXml(INCREMENT.toURI(), Scenarios.class, SchemaProvider.getScenarioSchema());
        assertThat(result).isNotNull();
    }

    @Test
    void newFeature() {
        assert NEW_FEATURE != null;
        assertThrows(ConversionService.ConversionException.class,
                () -> this.service.readXml(NEW_FEATURE.toURI(), Scenarios.class, SchemaProvider.getScenarioSchema()));
    }

    @Test
    void newVersion() {
        assert NEW_VERSION != null;
        assertThrows(ConversionService.ConversionException.class,
                () -> this.service.readXml(NEW_VERSION.toURI(), Scenarios.class, SchemaProvider.getScenarioSchema()));
    }
}
