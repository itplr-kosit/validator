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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.kosit.validationtool.api.CheckConfiguration;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.DocumentParseAction;
import de.kosit.validationtool.model.scenarios.ScenarioType;
import de.kosit.validationtool.model.scenarios.Scenarios;

import net.sf.saxon.s9api.XdmNode;

/**
 * Testet das {@link ScenarioRepository}.
 * 
 * @author Andreas Penski
 */

public class ScenarioRepositoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    ContentRepository content;

    private ScenarioRepository repository;

    @Before
    public void setup() {
        this.content = new ContentRepository(ObjectFactory.createProcessor(), Simple.REPOSITORY);
        final Scenarios def = new Scenarios();
        final ScenarioType t = new ScenarioType();
        t.setMatch("//*:name");
        t.setName("Test");
        t.initialize(this.content, true);
        def.getScenario().add(t);
        this.repository = new ScenarioRepository(this.content);
        this.repository.initialize(def);
    }

    @Test
    public void testHappyCase() throws Exception {
        final Result<ScenarioType, String> scenario = this.repository.selectScenario(load(Simple.SCENARIOS));
        assertThat(scenario).isNotNull();
        assertThat(scenario.isValid()).isTrue();
    }

    @Test
    public void testNonMatch() throws Exception {
        this.repository.getScenarios().getScenario().clear();
        final ScenarioType fallback = new ScenarioType();
        fallback.setName("fallback");
        this.repository.setFallbackScenario(fallback);
        final Result<ScenarioType, String> scenario = this.repository.selectScenario(load(Simple.SCENARIOS));
        assertThat(scenario).isNotNull();
        assertThat(scenario.isValid()).isFalse();
        assertThat(scenario.getObject().getName()).isEqualTo("fallback");

    }

    @Test
    public void testMultiMatch() throws Exception {
        final ScenarioType t = new ScenarioType();
        t.setMatch("//*:name");
        t.setName("Test");
        t.initialize(this.content, true);
        this.repository.getScenarios().getScenario().add(t);
        final ScenarioType fallback = new ScenarioType();
        fallback.setName("fallback");
        this.repository.setFallbackScenario(fallback);
        final Result<ScenarioType, String> scenario = this.repository.selectScenario(load(Simple.SCENARIOS));
        assertThat(scenario).isNotNull();
        assertThat(scenario.isValid()).isFalse();
        assertThat(scenario.getObject().getName()).isEqualTo("fallback");
    }

    private static XdmNode load(final URI uri) throws IOException {
        final DocumentParseAction p = new DocumentParseAction();
        return DocumentParseAction.parseDocument(read(uri.toURL())).getObject();
    }

    @Test
    public void loadFromJar() throws URISyntaxException {
        this.content = new ContentRepository(ObjectFactory.createProcessor(), Helper.JAR_REPOSITORY.toURI());
        this.repository = new ScenarioRepository(this.content);
        final CheckConfiguration conf = new CheckConfiguration(
                ScenarioRepository.class.getClassLoader().getResource("xrechnung/scenarios.xml").toURI());
        this.repository.initialize(conf);
        assertThat(this.repository.getScenarios()).isNotNull();
    }

}
