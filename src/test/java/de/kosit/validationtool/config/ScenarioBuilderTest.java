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

package de.kosit.validationtool.config;

import static de.kosit.validationtool.config.TestConfigurationFactory.createScenario;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.scenarios.NamespaceType;
import de.kosit.validationtool.model.scenarios.ScenarioType;

import net.sf.saxon.s9api.XPathExecutable;

/**
 * Test {@link ScenarioBuilder}.
 * 
 * @author Andreas Penski
 */
public class ScenarioBuilderTest {

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @Test
    public void simpleValid() {
        final Result<Scenario, String> result = createScenario().build(Simple.createContentRepository());
        assertThat(result.isValid()).isTrue();
        assertThat(result.getObject().getConfiguration()).isNotNull();
    }

    @Test
    public void testNoSchema() {
        final ScenarioBuilder builder = createScenario();
        builder.validate((SchemaBuilder) null);
        final Result<Scenario, String> result = builder.build(Simple.createContentRepository());
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("schema"));
    }

    @Test
    public void testNoMatch() {
        final ScenarioBuilder builder = createScenario();
        builder.match((String) null);
        final Result<Scenario, String> result = builder.build(Simple.createContentRepository());
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("match"));
    }

    @Test
    public void testInvalidMatch() {
        final ScenarioBuilder builder = createScenario();
        builder.match("/////");
        final Result<Scenario, String> result = builder.build(Simple.createContentRepository());
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("match"));
    }

    @Test
    public void testNoAccept() {
        final ScenarioBuilder builder = createScenario();
        builder.acceptWith((String) null);
        final Result<Scenario, String> result = builder.build(Simple.createContentRepository());
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testInvalidAccept() {
        final ScenarioBuilder builder = createScenario();
        builder.acceptWith("/////");
        final Result<Scenario, String> result = builder.build(Simple.createContentRepository());
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("accept"));
    }

    @Test
    public void testCombinedNamespaces() {
        final ContentRepository repository = Simple.createContentRepository();
        final Map<String, String> ns1 = new HashMap<>();
        ns1.put("n1", "http://n1.org");
        final XPathExecutable match = repository.createXPath("//n1:*", ns1);

        final Map<String, String> ns2 = new HashMap<>();
        ns2.put("n2", "http://n2.org");
        final XPathExecutable accept = repository.createXPath("//n2:*", ns2);

        final ScenarioBuilder builder = createScenario();
        builder.getNamespaces().clear();

        builder.match(match).acceptWith(accept).declareNamespace("n3", "http://n3.org");
        final Result<Scenario, String> result = builder.build(repository);

        assertThat(result.isValid()).isTrue();
        final Scenario scenario = result.getObject();
        final List<NamespaceType> namespaces = scenario.getConfiguration().getNamespace();
        assertThat(namespaces.stream().map(NamespaceType::getPrefix)).containsExactly("n1", "n2", "n3");
        assertThat(namespaces).hasSize(3);
    }

    @Test
    public void testConfigureWithExecutable() {
        final ContentRepository repository = Simple.createContentRepository();
        final XPathExecutable match = repository.createXPath("//*", null);
        final XPathExecutable accept = repository.createXPath("//*", null);
        final ScenarioBuilder builder = createScenario();
        builder.getNamespaces().clear();

        builder.match(match);
        builder.acceptWith(accept);
        final Result<Scenario, String> result = builder.build(repository);
        assertThat(result.isValid()).isTrue();
        final ScenarioType configuration = result.getObject().getConfiguration();
        assertThat(configuration.getMatch()).isNotEmpty();
        assertThat(configuration.getAcceptMatch()).isNotEmpty();
        assertThat(configuration.getNamespace()).isEmpty();
    }

    @Test
    public void testBasicAttributes() {
        final ContentRepository repository = Simple.createContentRepository();
        final String random = RandomStringUtils.random(5);
        final ScenarioBuilder builder = createScenario();
        builder.name(random).description(random);
        final Result<Scenario, String> result = builder.build(repository);
        assertThat(result.isValid()).isTrue();
        final ScenarioType config = result.getObject().getConfiguration();
        assertThat(config.getName()).isEqualTo(random);
        assertThat(config.getDescription()).isNotNull();
        assertThat(config.getDescription().getPOrOlOrUl()).isNotEmpty();
    }

    @Test
    public void testNoBasicAttributes() {
        final ContentRepository repository = Simple.createContentRepository();
        final ScenarioBuilder builder = createScenario();
        builder.name(null);
        final Result<Scenario, String> result = builder.build(repository);
        assertThat(result.isValid()).isTrue();
        final ScenarioType config = result.getObject().getConfiguration();
        assertThat(config.getName()).contains("manually");
        assertThat(config.getDescription()).isNotNull();
        assertThat(config.getDescription().getPOrOlOrUl()).isNotEmpty();
    }
}
