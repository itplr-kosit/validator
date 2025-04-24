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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.model.scenarios.Scenarios;

/**
 * Testet die Versionierung von Scenario-Dateien aka Konfigurationsdaten.
 *
 * @author Andreas Penski
 */
public class VersioningTest {

    private static final URL BASE = VersioningTest.class.getResource("/examples/versioning/scenarios-base.xml");

    private static final URL INCREMENT = VersioningTest.class.getResource("/examples/versioning/scenarios-increment.xml");

    private static final URL NEW_FEATURE = VersioningTest.class.getResource("/examples/versioning/scenarios-newfeature.xml");

    private static final URL NEW_VERSION = VersioningTest.class.getResource("/examples/versioning/scenarios-newversion.xml");

    private ConversionService service;

    private ContentRepository repository;

    @Before
    public void setup() {
        this.repository = Simple.createContentRepository();
        this.service = new ConversionService();
    }

    @Test
    public void testBase() throws URISyntaxException {
        final Scenarios result = this.service.readXml(BASE.toURI(), Scenarios.class, SchemaProvider.getScenarioSchema());
        assertThat(result).isNotNull();
    }

    @Test
    public void testFrameworkIncrement() throws URISyntaxException {
        final Scenarios result = this.service.readXml(INCREMENT.toURI(), Scenarios.class, SchemaProvider.getScenarioSchema());
        assertThat(result).isNotNull();
    }

    @Test
    public void testNewFeature() throws URISyntaxException {
        assertThrows(ConversionService.ConversionException.class,
                () -> this.service.readXml(NEW_FEATURE.toURI(), Scenarios.class, SchemaProvider.getScenarioSchema()));
    }

    @Test
    public void testNewVersion() throws URISyntaxException {
        assertThrows(ConversionService.ConversionException.class,
                () -> this.service.readXml(NEW_VERSION.toURI(), Scenarios.class, SchemaProvider.getScenarioSchema()));
    }
}
