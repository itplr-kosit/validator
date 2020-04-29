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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import lombok.Data;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.scenarios.ScenarioType;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XdmNode;

/**
 * Testet das {@link ScenarioRepository}.
 * 
 * @author Andreas Penski
 */

public class ScenarioRepositoryTest {

    @Data
    private static class DummyConfiguration implements Configuration {

        private List<Scenario> scenarios;

        private Scenario fallbackScenario;

        private String author;

        private String name;

        private String date;

        private Processor processor;

        private ContentRepository contentRepository;

        private Map<String, Object> additionalParameters;

    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ScenarioRepository repository;

    private DummyConfiguration configInstance;

    @Before
    public void setup() {
        final Scenario s = createScenario();

        this.configInstance = new DummyConfiguration();
        this.configInstance.setScenarios(new ArrayList<>());
        this.configInstance.getScenarios().add(s);
        this.repository = new ScenarioRepository(this.configInstance);
    }

    private static Scenario createScenario() {
        final Scenario s = new Scenario(new ScenarioType());
        s.setMatchExecutable(createXpath("//*:name"));
        return s;
    }

    @Test
    public void testHappyCase() throws Exception {
        final Result<Scenario, String> scenario = this.repository.selectScenario(load(Simple.SCENARIOS));
        assertThat(scenario).isNotNull();
        assertThat(scenario.isValid()).isTrue();
    }

    @Test
    public void testNonMatch() throws Exception {
        this.configInstance.setScenarios(new ArrayList<>());
        final Scenario fallback = createFallback();
        this.configInstance.setFallbackScenario(fallback);
        final Result<Scenario, String> scenario = this.repository.selectScenario(load(Simple.SCENARIOS));
        assertThat(scenario).isNotNull();
        assertThat(scenario.isValid()).isFalse();
        assertThat(scenario.getObject().getName()).isEqualTo("fallback");

    }

    private static Scenario createFallback() {
        final ScenarioType t = new ScenarioType();
        t.setName("fallback");
        final Scenario fallback = new Scenario(t);
        fallback.setFallback(true);
        return fallback;
    }

    @Test
    public void testMultiMatch() throws Exception {
        this.configInstance.getScenarios().add(createScenario());
        this.configInstance.setFallbackScenario(createFallback());
        final Result<Scenario, String> scenario = this.repository.selectScenario(load(Simple.SCENARIOS));
        assertThat(scenario).isNotNull();
        assertThat(scenario.isValid()).isFalse();
        assertThat(scenario.getObject().getName()).isEqualTo("fallback");
    }

    private static XdmNode load(final URI uri) throws IOException {
        return Helper.parseDocument(read(uri.toURL())).getObject();
    }

    private static XPathExecutable createXpath(final String expression) {
        return new ContentRepository(ResolvingMode.STRICT_RELATIVE.getStrategy(), null).createXPath(expression, new HashMap<>());
    }
}
