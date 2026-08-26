package org.kosit.validator.impl.conformatron.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.kosit.validator.impl.tasks.TestScenarioBuilder;

/**
 * Tests the {@link ScenarioMatch} facade over the legacy {@link Scenario}.
 */
public class ScenarioMatchTest {

    private static ParseXmlResult parseSimple() {
        return new ParseXmlAction().execute(TestHelper.read(Simple.SIMPLE_VALID));
    }

    private static Scenario createNamedScenario() {
        final Scenario scenario = TestScenarioBuilder.createDefault();
        scenario.getConfiguration().setName("simple");
        scenario.getConfiguration().setMatch("/simple");
        return scenario;
    }

    @Test
    public void testWrapsLegacyScenario() {
        final ParseXmlResult parsed = parseSimple();
        final ScenarioMatch match = ScenarioMatch.of(createNamedScenario(), parsed.getParsedSource());

        assertThat(match.getScenarioID()).isEqualTo("simple");
        assertThat(match.getScenarioName()).isEqualTo("simple");
        assertThat(match.getMatchExpression()).isEqualTo("/simple");
        assertThat(match.isUserSelected()).isFalse();
        // the legacy XPath selector does not expose the matched value
        assertThat(match.getMatchedValue()).isNull();
        assertThat(match.getArtifactReferences()).isNotEmpty();
        assertThat(match.getParsedSource()).isSameAs(parsed.getParsedSource());
    }

    @Test
    public void testRejectsFallbackScenario() {
        final Scenario fallback = createNamedScenario();
        fallback.setFallback(true);
        final ParseXmlResult parsed = parseSimple();
        assertThrows(IllegalArgumentException.class, () -> ScenarioMatch.of(fallback, parsed.getParsedSource()));
    }

    @Test
    public void testRejectsNullArguments() {
        final ParseXmlResult parsed = parseSimple();
        assertThrows(IllegalArgumentException.class, () -> ScenarioMatch.of(null, parsed.getParsedSource()));
        assertThrows(IllegalArgumentException.class, () -> ScenarioMatch.of(createNamedScenario(), null));
    }
}
