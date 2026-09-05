package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.config.TestConfiguration;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.scenario.v1.ScenarioType;

import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XdmNode;

/**
 * Tests the {@link ScenarioRepository}.
 *
 * @author Andreas Penski
 */

public class ScenarioRepositoryTest {

    private ScenarioRepository repository;

    private TestConfiguration configInstance;

    @BeforeEach
    public void setup() {
        this.configInstance = new TestConfiguration();
        this.configInstance
                .setContentRepository(new ContentRepository(TestHelper.getTestProcessor(), TestHelper.getTestResolvingStrategy(), null));

        final Scenario s = createScenario();
        this.configInstance.setScenarios(new ArrayList<>());
        this.configInstance.getScenarios().add(s);
        this.repository = new ScenarioRepository(this.configInstance);
    }

    private Scenario createScenario() {
        final Scenario s = new Scenario(new ScenarioType());
        s.setMatchExecutable(createXpath("//*:name"));
        return s;
    }

    @Test
    public void testHappyCase() throws Exception {
        final SingleProcessingResult<Scenario, String> scenario = this.repository.selectScenario(load(Simple.SCENARIOS));
        assertThat(scenario).isNotNull();
        assertThat(scenario.isValid()).isTrue();
    }

    @Test
    public void testNonMatch() throws Exception {
        this.configInstance.setScenarios(new ArrayList<>());
        final Scenario fallback = createFallback();
        this.configInstance.setFallbackScenario(fallback);
        final SingleProcessingResult<Scenario, String> scenario = this.repository.selectScenario(load(Simple.SCENARIOS));
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
        final SingleProcessingResult<Scenario, String> scenario = this.repository.selectScenario(load(Simple.SCENARIOS));
        assertThat(scenario).isNotNull();
        assertThat(scenario.isValid()).isFalse();
        assertThat(scenario.getObject().getName()).isEqualTo("fallback");
    }

    @Test
    public void testNoConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> this.repository = new ScenarioRepository());
    }

    @Test
    public void testFallbackOnMultipleConfigurations() {
        final TestConfiguration first = this.configInstance;
        first.setFallbackScenario(createFallback());
        setup();// create new one;
        final TestConfiguration second = this.configInstance;
        second.setFallbackScenario(createFallback());
        this.repository = new ScenarioRepository(first, second);
        final Scenario fallback = this.repository.getFallbackScenario();
        assertThat(fallback).isSameAs(first.getFallbackScenario()).isNotSameAs(second.getFallbackScenario());
    }

    private XdmNode load(final URI uri) {
        return TestHelper.parseDocument(this.configInstance.getContentRepository().getProcessor(), TestHelper.read(uri)).getObject();
    }

    private XPathExecutable createXpath(final String expression) {
        return this.configInstance.getContentRepository().createXPath(expression, new HashMap<>());
    }
}
