package org.kosit.validator.impl.conformatron;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.api.InputFactory.read;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.conformatron.api.model.conformance.ECTConformanceResult;
import org.conformatron.api.model.detection.ICTDetection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.Configuration;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.ScenarioRepository;
import org.kosit.validator.impl.conformatron.ApplyRulesAction.ApplyRulesActionResult;
import org.kosit.validator.impl.conformatron.ComputeConformanceAction.ComputeConformanceActionResult;
import org.kosit.validator.impl.conformatron.DetectScenariosAction.DetectScenariosResult;
import org.kosit.validator.impl.conformatron.ParseDocumentAction.ParseDocumentResult;
import org.kosit.validator.impl.conformatron.PrepareRulesAction.PrepareRulesResult;
import org.kosit.validator.impl.conformatron.RetrieveArtifactsAction.RetrieveArtifactsResult;
import org.kosit.validator.impl.conformatron.SelectScenarioAction.SelectScenarioResult;

/**
 * <b>End-to-end walkthrough of the canonical pipeline, steps 2–8</b>, composed exclusively from the new-API actions —
 * no legacy {@code CheckAction} involved:
 *
 * <pre>
 * 2 PARSE_DOCUMENT → 3 DETECT_SCENARIOS → 4 SELECT_SCENARIO → 5 RETRIEVE_ARTIFACTS
 *                  → 6 PREPARE_RULES    → 7 APPLY_RULES     → 8 COMPUTE_CONFORMANCE
 * </pre>
 *
 * Step 1 (DETECT_SYNTAX) is not implemented yet; step 9 (DECISION_RECOMMENDATION) is pending. The handshake objects
 * cross every step boundary exactly as specified: {@code ICTParsedValidationSource} → {@code ICTScenarioMatch} →
 * {@code ICTResolvedValidationArtifact} → {@code ICTPreparedRuleSet} → {@code ICTApplyRulesResult} →
 * {@code ICTComputeConformanceResult}.
 */
public class CanonicalPipelineTest {

    private ScenarioRepository scenarioRepository;

    private Configuration configuration;

    @BeforeEach
    public void setup() {
        this.configuration = Configuration.load(Simple.SCENARIOS_WITH_SCH, Simple.REPOSITORY_URI).build(Helper.getTestProcessor());
        this.scenarioRepository = new ScenarioRepository(this.configuration);
    }

    /** Runs the full chain 2–8 and returns the step-8 result; asserts every intermediate step succeeded. */
    private ComputeConformanceActionResult runPipeline(final URI document, final List<String> trace) {
        // step 2: PARSE_DOCUMENT — DOM-based reference action, retains bytes + hash
        final ParseDocumentResult parsed = new ParseDocumentAction().execute(read(document));
        assertThat(parsed.isSuccess()).isTrue();
        trace.addAll(codes(parsed.detections().getAll()));

        // step 3: DETECT_SCENARIOS — the DOM is wrapped into the Saxon model for the XPath matching
        final DetectScenariosResult detected = new DetectScenariosAction(this.scenarioRepository, Helper.getTestProcessor())
                .execute(parsed.parsedSource());
        assertThat(detected.isSuccess()).isTrue();
        trace.addAll(codes(detected.detections().getAll()));

        // step 4: SELECT_SCENARIO — strict: exactly one candidate
        final SelectScenarioResult selected = new SelectScenarioAction().execute(detected.matches());
        assertThat(selected.isSuccess()).isTrue();
        trace.addAll(codes(selected.detections().getAll()));

        // step 5: RETRIEVE_ARTIFACTS — repository-confined resolution of the scenario's references
        final RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(Simple.REPOSITORY_URI).execute(selected.selected());
        assertThat(retrieved.isSuccess()).isTrue();
        trace.addAll(codes(retrieved.detections().getAll()));

        // step 6: PREPARE_RULES — transpile + compile into engine-ready rule sets
        final PrepareRulesResult prepared = new PrepareRulesAction(this.configuration.getContentRepository()).execute(retrieved.artifacts(),
                selected.selected().getParsedSource().getSource().getName());
        assertThat(prepared.isSuccess()).isTrue();
        trace.addAll(codes(prepared.detections().getAll()));

        // step 7: APPLY_RULES — on the retained bytes; findings do not fail the step
        final ApplyRulesActionResult applied = new ApplyRulesAction().execute(parsed.parsedSource(), prepared.ruleSets());
        assertThat(applied.isSuccess()).isTrue();
        trace.addAll(codes(applied.detections().getAll()));

        // step 8: COMPUTE_CONFORMANCE — scenario-wide default target derived from the selected scenario
        final ComputeConformanceActionResult conformance = new ComputeConformanceAction().execute(applied.result(),
                List.of(ConformanceTarget.ofScenario(selected.selected())));
        assertThat(conformance.isSuccess()).isTrue();
        trace.addAll(codes(conformance.detections().getAll()));
        return conformance;
    }

    private static List<String> codes(final List<ICTDetection> detections) {
        return detections.stream().map(ICTDetection::getCode).toList();
    }

    @Test
    public void testConformantDocumentPassesAllSteps() {
        final List<String> trace = new ArrayList<>();
        final ComputeConformanceActionResult conformance = runPipeline(Simple.SIMPLE_VALID, trace);

        assertThat(conformance.result().hasNonConformantTarget()).isFalse();
        assertThat(conformance.result().getAllStatements()).extracting("result").containsOnly(ECTConformanceResult.CONFORMANT);

        // the full audit trail across all steps, in pipeline order
        assertThat(trace).containsExactly(//
                ParseDocumentAction.CODE_DOCUMENT_PARSED, // step 2
                DetectScenariosAction.CODE_SCENARIO_MATCHED, // step 3
                SelectScenarioAction.CODE_SCENARIO_SELECTED, // step 4
                RetrieveArtifactsAction.CODE_ARTIFACTS_RETRIEVED, RetrieveArtifactsAction.CODE_ARTIFACTS_RETRIEVED, // step
                                                                                                                    // 5
                PrepareRulesAction.CODE_RULE_COMPILED, PrepareRulesAction.CODE_RULE_COMPILED, // step 6
                ApplyRulesAction.CODE_RULES_APPLIED, ApplyRulesAction.CODE_RULES_APPLIED, // step 7
                ComputeConformanceAction.CODE_TARGET_CONFORMANT, ComputeConformanceAction.CODE_TARGET_CONFORMANT); // step
                                                                                                                   // 8
    }

    @Test
    public void testNonConformantDocumentIsTraceableToTheDrivingRuleSet() {
        final List<String> trace = new ArrayList<>();
        final ComputeConformanceActionResult conformance = runPipeline(Simple.SCHEMATRON_INVALID, trace);

        assertThat(conformance.result().hasNonConformantTarget()).isTrue();
        // XSD passed, the schematron drove the non-conformance — per-rule-set traceability
        assertThat(conformance.result().getAllStatements()).extracting("result").containsExactly(ECTConformanceResult.CONFORMANT,
                ECTConformanceResult.NON_CONFORMANT);
        // the violated assert id travels through as detection code (step-07 spec)
        assertThat(trace).contains("content-1", ComputeConformanceAction.CODE_TARGET_NON_CONFORMANT);
    }
}
