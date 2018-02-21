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

import static de.kosit.validationtool.api.InputFactory.read;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;

import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.DocumentParseAction;
import de.kosit.validationtool.model.scenarios.ScenarioType;
import de.kosit.validationtool.model.scenarios.Scenarios;

/**
 * Testet das {@link ScenarioRepository}.
 * 
 * @author Andreas Penski
 */

public class ScenarioRepositoryTest {

    private static final URL SAMPLE = ScenarioRepositoryTest.class.getResource("/valid/scenarios.xml");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    ContentRepository content;

    private ScenarioRepository repository;

    @Before
    public void setup() {
        content = new ContentRepository(ObjectFactory.createProcessor(), new File("src/test/resources/examples/repository").toURI());
        Scenarios def = new Scenarios();
        ScenarioType t = new ScenarioType();
        t.setMatch("//*:name");
        t.setName("Test");
        t.initialize(content, true);
        def.getScenario().add(t);
        repository = new ScenarioRepository(ObjectFactory.createProcessor(), content);
        repository.initialize(def);
    }

    @Test
    public void testHappyCase() throws Exception {
        final Result<ScenarioType, String> scenario = repository.selectScenario(load(SAMPLE));
        assertThat(scenario).isNotNull();
        assertThat(scenario.isValid()).isTrue();
    }

    @Test
    public void testNonMatch() throws Exception {
        repository.getScenarios().getScenario().clear();
        final Result<ScenarioType, String> scenario = repository.selectScenario(load(SAMPLE));
        assertThat(scenario).isNotNull();
        assertThat(scenario.isValid()).isFalse();
    }

    @Test
    public void testMultiMatch() throws Exception {
        ScenarioType t = new ScenarioType();
        t.setMatch("//*:name");
        t.setName("Test");
        t.initialize(content, true);
        repository.getScenarios().getScenario().add(t);
        final Result<ScenarioType, String> scenario = repository.selectScenario(load(SAMPLE));
        assertThat(scenario).isNotNull();
        assertThat(scenario.isValid()).isFalse();
    }

    private Document load(URL url) throws IOException {
        DocumentParseAction p = new DocumentParseAction();
        return p.parseDocument(read(url)).getObject();
    }
}
