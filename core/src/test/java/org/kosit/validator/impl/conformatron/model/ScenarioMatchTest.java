package org.kosit.validator.impl.conformatron.model;

import org.kosit.validator.impl.conformatron.action.ParseXMLAction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kosit.validator.api.InputFactory.read;

import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.conformatron.action.ParseXMLAction.ParseXMLResult;
import org.kosit.validator.impl.tasks.TestScenarioBuilder;

/**
 * Tests the {@link ScenarioMatch} facade over the legacy {@link Scenario}.
 */
public class ScenarioMatchTest {

    private static ParseXMLResult parseSimple() {
        return new ParseXMLAction().execute(read(Simple.SIMPLE_VALID));
    }

    private static Scenario createNamedScenario() {
        final Scenario scenario = TestScenarioBuilder.createDefault();
        scenario.getConfiguration().setName("simple");
        scenario.getConfiguration().setMatch("/simple");
        return scenario;
    }

    @Test
    public void testWrapsLegacyScenario() {
        final ParseXMLResult parsed = parseSimple();
        final ScenarioMatch match = ScenarioMatch.of(createNamedScenario(), parsed.parsedSource());

        assertThat(match.getScenarioID()).isEqualTo("simple");
        assertThat(match.getScenarioName()).isEqualTo("simple");
        assertThat(match.getMatchExpression()).isEqualTo("/simple");
        assertThat(match.isUserSelected()).isFalse();
        // the legacy XPath selector does not expose the matched value
        assertThat(match.getMatchedValue()).isNull();
        assertThat(match.getArtifactReferences()).isNotEmpty();
        assertThat(match.getParsedSource()).isSameAs(parsed.parsedSource());
    }

    @Test
    public void testRejectsFallbackScenario() {
        final Scenario fallback = createNamedScenario();
        fallback.setFallback(true);
        final ParseXMLResult parsed = parseSimple();
        assertThrows(IllegalArgumentException.class, () -> ScenarioMatch.of(fallback, parsed.parsedSource()));
    }

    @Test
    public void testRejectsNullArguments() {
        final ParseXMLResult parsed = parseSimple();
        assertThrows(IllegalArgumentException.class, () -> ScenarioMatch.of(null, parsed.parsedSource()));
        assertThrows(IllegalArgumentException.class, () -> ScenarioMatch.of(createNamedScenario(), null));
    }
}
