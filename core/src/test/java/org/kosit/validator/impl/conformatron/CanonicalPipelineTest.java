package org.kosit.validator.impl.conformatron;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.conformatron.api.model.conformance.CTConformanceResult;
import org.conformatron.api.model.detection.CTDetection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.TestHelper;
import org.kosit.validator.testdata.TestResources;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction.ApplyRulesActionResult;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction.ComputeConformanceActionResult;
import org.kosit.validator.impl.conformatron.action.DecisionRecommendationAction;
import org.kosit.validator.impl.conformatron.action.DecisionRecommendationAction.DecisionRecommendationResult;
import org.kosit.validator.impl.conformatron.action.PrepareRulesAction;
import org.kosit.validator.impl.conformatron.action.PrepareRulesAction.PrepareRulesResult;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction.RetrieveArtifactsResult;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction.SelectScenarioResult;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.kost.validator.api.xml.XmlDetection;
import org.kosit.validator.impl.conformatron.model.ConformanceTarget;
import org.kosit.validator.impl.conformatron.model.ScenarioSeverityOverrides;

/**
 * <b>End-to-end walkthrough of the canonical pipeline, steps 2–9</b>, composed exclusively from the new-API actions —
 * no legacy {@code CheckAction} involved:
 *
 * <pre>
 * 2 PARSE_DOCUMENT → 3 DETECT_SCENARIOS → 4 SELECT_SCENARIO → 5 RETRIEVE_ARTIFACTS
 *                  → 6 PREPARE_RULES    → 7 APPLY_RULES     → 8 COMPUTE_CONFORMANCE → 9 DECISION_RECOMMENDATION
 * </pre>
 *
 * Step 1 (DETECT_SYNTAX) is not implemented yet. The handshake objects cross every step boundary exactly as specified:
 * {@code ICTParsedValidationSource} → {@code ICTScenarioMatch} → {@code ICTResolvedValidationArtifact} →
 * {@code ICTPreparedRuleSet} → {@code ICTApplyRulesResult} → {@code ICTComputeConformanceResult}.
 */
public class CanonicalPipelineTest {

    private ScenarioSet configuration;

    @BeforeEach
    public void setup() {
        this.configuration = ScenarioSet.load(TestResources.Simple.SCENARIOS_WITH_SCH, TestResources.Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
    }

    /** Runs the full chain 2–9 and returns the step-8 result; asserts every intermediate step succeeded. */
    private ComputeConformanceActionResult runPipeline(final URI document, final List<String> trace) {
        // step 2: PARSE_DOCUMENT — DOM-based reference action, retains bytes + hash
        final ParseXmlResult parsed = new ParseXmlAction().execute(TestHelper.read(document));
        assertThat(parsed.isSuccess()).isTrue();
        trace.addAll(codes(parsed.getDetectionList().getAll()));

        // step 3: DETECT_SCENARIOS — the DOM is wrapped into the Saxon model for the XPath matching
        final DetectScenariosResult detected = new DetectScenariosAction(this.configuration.getScenarios(), TestHelper.getTestProcessor())
                .execute(parsed.getParsedSource());
        assertThat(detected.isSuccess()).isTrue();
        trace.addAll(codes(detected.detections().getAll()));

        // step 4: SELECT_SCENARIO — strict: exactly one candidate
        final SelectScenarioResult selected = new SelectScenarioAction().execute(detected.matches());
        assertThat(selected.isSuccess()).isTrue();
        trace.addAll(codes(selected.detections().getAll()));

        // step 5: RETRIEVE_ARTIFACTS — repository-confined resolution of the scenario's references
        final RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(TestResources.Simple.REPOSITORY_URI, true)
                .execute(selected.selected());
        assertThat(retrieved.isSuccess()).isTrue();
        trace.addAll(codes(retrieved.detections().getAll()));

        // step 6: PREPARE_RULES — transpile + compile into engine-ready rule sets
        final PrepareRulesResult prepared = new PrepareRulesAction(this.configuration.getScenarios().get(0).getRepository())
                .execute(retrieved.artifacts(), selected.selected().getParsedSource().getSource().getName());
        assertThat(prepared.isSuccess()).isTrue();
        trace.addAll(codes(prepared.detections().getAll()));

        // step 7: APPLY_RULES — on the retained bytes; findings do not fail the step; scenario overrides applied
        final ApplyRulesActionResult applied = new ApplyRulesAction().execute(parsed.getParsedSource(), prepared.ruleSets(),
                ScenarioSeverityOverrides.of(selected.selected()));
        assertThat(applied.isSuccess()).isTrue();
        trace.addAll(codes(applied.detections().getAll()));

        // step 8: COMPUTE_CONFORMANCE — scenario-wide default target derived from the selected scenario
        final ComputeConformanceActionResult conformance = new ComputeConformanceAction().execute(applied.result(),
                List.of(ConformanceTarget.ofScenario(selected.selected())));
        assertThat(conformance.isSuccess()).isTrue();
        trace.addAll(codes(conformance.detections().getAll()));

        // step 9: DECISION_RECOMMENDATION — the terminal verdict over all conformance statements
        final DecisionRecommendationResult decision = new DecisionRecommendationAction().execute(conformance.result());
        assertThat(decision.isSuccess()).isTrue();
        trace.addAll(codes(decision.detections().getAll()));
        return conformance;
    }

    private static List<String> codes(final List<CTDetection> detections) {
        return detections.stream().map(CTDetection::getCode).toList();
    }

    @Test
    public void testConformantDocumentPassesAllSteps() {
        final List<String> trace = new ArrayList<>();
        final ComputeConformanceActionResult conformance = runPipeline(TestResources.Simple.SIMPLE_VALID, trace);

        assertThat(conformance.result().hasNonConformantTarget()).isFalse();
        assertThat(conformance.result().getAllStatements()).extracting("result").containsOnly(CTConformanceResult.CONFORMANT);

        // the full audit trail across all steps, in pipeline order
        assertThat(trace).containsExactly(//
                XmlDetection.CODE_DOCUMENT_PARSED, // step 2
                DetectScenariosAction.CODE_SCENARIO_MATCHED, // step 3
                SelectScenarioAction.CODE_SCENARIO_SELECTED, // step 4
                RetrieveArtifactsAction.CODE_ARTIFACTS_RETRIEVED, RetrieveArtifactsAction.CODE_ARTIFACTS_RETRIEVED, // step
                                                                                                                    // 5
                PrepareRulesAction.CODE_RULE_COMPILED, PrepareRulesAction.CODE_RULE_COMPILED, // step 6
                ApplyRulesAction.CODE_RULES_APPLIED, ApplyRulesAction.CODE_RULES_APPLIED, // step 7
                ComputeConformanceAction.CODE_TARGET_CONFORMANT, ComputeConformanceAction.CODE_TARGET_CONFORMANT, // step
                                                                                                                  // 8
                DecisionRecommendationAction.CODE_ACCEPT); // step 9
    }

    @Test
    public void testNonConformantDocumentIsTraceableToTheDrivingRuleSet() {
        final List<String> trace = new ArrayList<>();
        final ComputeConformanceActionResult conformance = runPipeline(TestResources.Simple.SCHEMATRON_INVALID, trace);

        assertThat(conformance.result().hasNonConformantTarget()).isTrue();
        // XSD passed, the schematron drove the non-conformance — per-rule-set traceability
        assertThat(conformance.result().getAllStatements()).extracting("result").containsExactly(CTConformanceResult.CONFORMANT,
                CTConformanceResult.NON_CONFORMANT);
        // the violated assert id travels through as detection code (step-07 spec)
        assertThat(trace).contains("content-1", ComputeConformanceAction.CODE_TARGET_NON_CONFORMANT,
                DecisionRecommendationAction.CODE_REJECT);
    }
}
