package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.net.URI;

import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.conformance.CTDecision;
import org.junit.jupiter.api.Test;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.cvr.report.CvrProfile;
import org.kosit.validator.TestHelper;
import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kosit.validator.testdata.TestResources;

/**
 * The engine as a whole: every path a run can take ends in a verdict and in a report that satisfies the CVR profile.
 * <p>
 * The step actions have their own tests, and {@code CvrUnhappyPathTest} checks what the report of a cancelled run looks
 * like in detail. What is checked here is the engine's own contribution: that it composes the steps in order, stops at
 * the first one that does not succeed, and still returns a result instead of throwing.
 * </p>
 */
public class ConformanceValidationTest {

    private static ConformanceValidationResult validate(final URI scenarios, final URI document) {
        final ScenarioSet configuration = ScenarioSet.load(scenarios, TestResources.Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
        // the shared test repository lives inside an archive, so this engine is allowed to resolve into one
        return new ConformanceValidation(new TestEngineInformation(), TestHelper.getTestProcessor(), true, configuration)
                .validate(TestHelper.read(document));
    }

    /** Every run must produce a report that is a CVR — a cancelled one included (partial CVR, ADR-004). */
    private static void assertReportIsAValidCvr(final ConformanceValidationResult result) throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        result.writeCvr(out);
        final var validation = CvrProfile.validate(ReadResource.inMemory(Resource.of("run.xml", out.toByteArray())));
        assertThat(validation.containsNoError()).isTrue();
    }

    @Test
    public void testAConformantDocumentIsAccepted() throws Exception {
        final ConformanceValidationResult result = validate(TestResources.Simple.SCENARIOS_WITH_SCH, TestResources.Simple.SIMPLE_VALID);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getCancelledAt()).isNull();
        assertThat(result.isConformant()).isTrue();
        assertThat(result.getDecision()).isEqualTo(CTDecision.ACCEPT);
        assertThat(result.getSelectedScenarioName()).isEqualTo("Simple");
        assertThat(result.getProcessingErrors()).isEmpty();
        // step 7 ran every rule set the scenario declares
        assertThat(result.getFindingsByRuleSet()).isNotEmpty();
        assertReportIsAValidCvr(result);
    }

    @Test
    public void testARuleViolationIsRejectedButTheRunCompletes() throws Exception {
        final ConformanceValidationResult result = validate(TestResources.Simple.SCENARIOS_WITH_SCH,
                TestResources.Simple.SCHEMATRON_INVALID);

        // the rules were applied — the run is complete, the document is not conformant
        assertThat(result.isCompleted()).isTrue();
        assertThat(result.isConformant()).isFalse();
        assertThat(result.getDecision()).isEqualTo(CTDecision.REJECT);
        assertThat(result.getRationale()).contains("non-conformant");
        assertReportIsAValidCvr(result);
    }

    @Test
    public void testAnUnparseableDocumentCancelsAtStepTwo() throws Exception {
        final ConformanceValidationResult result = validate(TestResources.Simple.SCENARIOS_WITH_SCH, TestResources.Simple.NOT_WELLFORMED);

        assertThat(result.isCompleted()).isFalse();
        assertThat(result.getCancelledAt()).isEqualTo(CTActionType.PARSE_DOCUMENT);
        assertThat(result.getDecision()).isEqualTo(CTDecision.REJECT);
        assertThat(result.getRationale()).contains(CTActionType.PARSE_DOCUMENT.getName());
        // the steps after the cancellation did not run, so there is nothing keyed by a rule set
        assertThat(result.getFindingsByRuleSet()).isEmpty();
        assertThat(result.getSelectedScenarioName()).isNull();
        assertReportIsAValidCvr(result);
    }

    @Test
    public void testADocumentWithoutAScenarioCancelsAtStepThree() throws Exception {
        final ConformanceValidationResult result = validate(TestResources.Simple.SCENARIOS_WITH_SCH, TestResources.Simple.UNKNOWN);

        assertThat(result.getCancelledAt()).isEqualTo(CTActionType.DETECT_SCENARIOS);
        assertThat(result.getDecision()).isEqualTo(CTDecision.REJECT);
        assertThat(result.getSelectedScenarioName()).isNull();
        assertReportIsAValidCvr(result);
    }

    @Test
    public void testAnAmbiguousScenarioCancelsAtStepFour() throws Exception {
        final ConformanceValidationResult result = validate(TestResources.Simple.SCENARIOS_AMBIGUOUS, TestResources.Simple.SIMPLE_VALID);

        assertThat(result.getCancelledAt()).isEqualTo(CTActionType.SELECT_SCENARIO);
        assertThat(result.getDecision()).isEqualTo(CTDecision.REJECT);
        assertReportIsAValidCvr(result);
    }

    @Test
    public void testTheCallerCanFixTheScenario() {
        final ScenarioSet configuration = ScenarioSet.load(TestResources.Simple.SCENARIOS_AMBIGUOUS, TestResources.Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
        final ConformanceValidation engine = new ConformanceValidation(new TestEngineInformation(), TestHelper.getTestProcessor(), true,
                configuration);

        // the same document that is ambiguous when detected completes when the caller names the scenario
        final ConformanceValidationResult result = engine.validate(TestHelper.read(TestResources.Simple.SIMPLE_VALID), "Simple");

        assertThat(result.getSelectedScenarioName()).isEqualTo("Simple");
        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    public void testAnUnknownScenarioNameIsAnError() {
        final ConformanceValidationResult result = validateWithScenarioId(TestResources.Simple.SCENARIOS_WITH_SCH,
                TestResources.Simple.SIMPLE_VALID, "does-not-exist");

        assertThat(result.getCancelledAt()).isEqualTo(CTActionType.DETECT_SCENARIOS);
        assertThat(result.getDecision()).isEqualTo(CTDecision.REJECT);
    }

    private static ConformanceValidationResult validateWithScenarioId(final URI scenarios, final URI document, final String scenarioId) {
        final ScenarioSet configuration = ScenarioSet.load(scenarios, TestResources.Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
        return new ConformanceValidation(new TestEngineInformation(), TestHelper.getTestProcessor(), true, configuration)
                .validate(TestHelper.read(document), scenarioId);
    }

    @Test
    public void testTheEngineNeedsAConfiguration() {
        assertThat(org.assertj.core.api.Assertions
                .catchThrowable(() -> new ConformanceValidation(new TestEngineInformation(), TestHelper.getTestProcessor())))
                        .isInstanceOf(IllegalArgumentException.class);
    }
}
