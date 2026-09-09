package org.kosit.validator.impl.conformatron.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.kosit.validator.TestHelper;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.TestScenarioBuilder;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.kosit.validator.testdata.TestResources;

/**
 * Tests the {@link ScenarioMatch} handshake object over a {@link Scenario}.
 */
public class ScenarioMatchTest {

    private static ParseXmlResult parseSimple() {
        return new ParseXmlAction().execute(TestHelper.read(TestResources.Simple.SIMPLE_VALID));
    }

    private static Scenario createNamedScenario() {
        return TestScenarioBuilder.createScenario("simple", "/simple");
    }

    @Test
    public void testWrapsTheScenario() {
        final ParseXmlResult parsed = parseSimple();
        final Scenario scenario = createNamedScenario();
        final ScenarioMatch match = ScenarioMatch.of(scenario, parsed.getParsedSource());

        assertThat(match.getScenario()).isSameAs(scenario);
        assertThat(match.getScenarioID()).isEqualTo("simple");
        assertThat(match.getScenarioName()).isEqualTo("simple");
        assertThat(match.getMatchExpression()).isEqualTo("/simple");
        assertThat(match.isUserSelected()).isFalse();
        // the XPath match does not expose the matched value
        assertThat(match.getMatchedValue()).isNull();
        assertThat(match.getArtifactReferences()).isNotEmpty();
        assertThat(match.getParsedSource()).isSameAs(parsed.getParsedSource());
        // assembled in code, read from no file
        assertThat(match.getDefinitionFile()).isNull();
    }

    @Test
    public void testAUserSelectedScenarioHasNoMatchExpression() {
        final ScenarioMatch match = ScenarioMatch.userSelected(createNamedScenario(), parseSimple().getParsedSource());

        assertThat(match.isUserSelected()).isTrue();
        assertThat(match.getMatchExpression()).isNull();
    }

    @Test
    public void testRejectsNullArguments() {
        final ParseXmlResult parsed = parseSimple();
        assertThrows(IllegalArgumentException.class, () -> ScenarioMatch.of(null, parsed.getParsedSource()));
        assertThrows(IllegalArgumentException.class, () -> ScenarioMatch.of(createNamedScenario(), null));
    }
}
