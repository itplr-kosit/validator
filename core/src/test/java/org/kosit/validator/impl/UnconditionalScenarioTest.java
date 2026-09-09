package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.net.URI;

import org.conformatron.api.model.conformance.CTDecision;
import org.junit.jupiter.api.Test;
import org.kosit.validator.TestHelper;
import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kosit.validator.impl.conformatron.report.CvrAssert;
import org.kosit.validator.testdata.TestResources;

/**
 * A configuration with a scenario matched by expression and a scenario without match: the second one applies
 * unconditionally, in addition to the first - one run, two conformance targets, one decision over both.
 */
public class UnconditionalScenarioTest {

    private static ConformanceValidationResult validate(final URI document) {
        final ScenarioSet configuration = ScenarioSet
                .load(TestResources.Simple.SCENARIOS_WITH_UNCONDITIONAL, TestResources.Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
        // the shared test repository lives inside an archive, so this engine is allowed to resolve into one
        return new ConformanceValidation(new TestEngineInformation(), TestHelper.getTestProcessor(), true, configuration)
                .validate(TestHelper.read(document));
    }

    @Test
    public void testBothScenariosAreApplied() throws Exception {
        final ConformanceValidationResult result = validate(TestResources.Simple.SIMPLE_VALID);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getSelectedScenarioName()).isEqualTo("Simple");
        assertThat(result.getAppliedScenarioNames()).containsExactly("Simple", "Always");
        // the rule sets of both scenarios ran: schema and schematron of "Simple", the schematron of "Always"
        assertThat(result.getFindingsByRuleSet()).hasSize(3);
        assertThat(result.isConformant()).isTrue();
        assertThat(result.getDecision()).isEqualTo(CTDecision.ACCEPT);
        // and the report is a CVR
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        result.writeCvr(out);
        CvrAssert.assertValidCvr("unconditional-both", out.toByteArray());
    }

    @Test
    public void testAFindingInEitherScenarioRejects() {
        final ConformanceValidationResult result = validate(TestResources.Simple.SCHEMATRON_INVALID);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getAppliedScenarioNames()).containsExactly("Simple", "Always");
        assertThat(result.isConformant()).isFalse();
        assertThat(result.getDecision()).isEqualTo(CTDecision.REJECT);
    }
}
