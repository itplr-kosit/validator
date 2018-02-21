/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

    private static final URL NEW_VERSION = VersioningTest.class.getResource("/examples/versioning/scenarios-newVersion.xml");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ConversionService service;

    private ContentRepository repository;

    @Before
    public void setup() {
        service = new ConversionService();
        repository = new ContentRepository(ObjectFactory.createProcessor(), new File("src/test/resources/examples/repository").toURI());
    }

    @Test
    public void testBase() throws URISyntaxException {
        final Scenarios result = service.readXml(BASE.toURI(), Scenarios.class, repository.getScenarioSchema());
        assertThat(result).isNotNull();
    }

    @Test
    public void testFrameworkIncrement() throws URISyntaxException {
        final Scenarios result = service.readXml(INCREMENT.toURI(), Scenarios.class, repository.getScenarioSchema());
        assertThat(result).isNotNull();
    }

    @Test
    public void testNewFeature() throws URISyntaxException {
        exception.expect(ConversionService.ConversionExeption.class);
        service.readXml(NEW_FEATURE.toURI(), Scenarios.class, repository.getScenarioSchema());
    }

    @Test
    public void testNewVersion() throws URISyntaxException {
        exception.expect(ConversionService.ConversionExeption.class);
        service.readXml(NEW_VERSION.toURI(), Scenarios.class, repository.getScenarioSchema());
    }
}
