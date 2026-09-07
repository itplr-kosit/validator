package org.kosit.validator.impl.conformatron;

import java.util.ArrayList;
import java.util.List;

import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.kosit.conformatron.detection.DetectionList;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction;
import org.kosit.validator.impl.conformatron.action.DecisionRecommendationAction;
import org.kosit.validator.impl.conformatron.action.PrepareRulesAction;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;

/**
 * The state of one run of the canonical pipeline: the result of every step it reached, in pipeline order. Fields from
 * the cancellation point onwards are {@code null} — the run then covers only the executed steps and is reported as
 * {@code cvr:status="CANCELLED"} (partial CVR, ADR-004).
 * <p>
 * This is the handshake between the engine that produces a run and everything that reads one — the report writer, the
 * result mapping, the E2E comparison. It carries no behavior of its own beyond the one invariant it must not let
 * anybody break: <b>step 9 always runs</b>, also for a cancelled run, so a run without a decision cannot be assembled
 * from step results (step-09 spec).
 * </p>
 *
 * @param parse step 2, {@code PARSE_DOCUMENT}; never {@code null}
 * @param detect step 3, {@code DETECT_SCENARIOS}
 * @param select step 4, {@code SELECT_SCENARIO}
 * @param retrieve step 5, {@code RETRIEVE_ARTIFACTS}
 * @param prepare step 6, {@code PREPARE_RULES}
 * @param apply step 7, {@code APPLY_RULES}
 * @param conformance step 8, {@code COMPUTE_CONFORMANCE}
 * @param decision step 9, {@code DECISION_RECOMMENDATION}; never {@code null}
 *
 * @author Andreas Schmitz
 */
public record PipelineResults(ParseXmlResult parse, DetectScenariosResult detect, SelectScenarioAction.SelectScenarioResult select,
        RetrieveArtifactsAction.RetrieveArtifactsResult retrieve, PrepareRulesAction.PrepareRulesResult prepare,
        ApplyRulesAction.ApplyRulesActionResult apply, ComputeConformanceAction.ComputeConformanceActionResult conformance,
        DecisionRecommendationAction.DecisionRecommendationResult decision) {

    /**
     * Assembles the run from the results of steps 2–8 and lets step 9 decide it. Step 9 always runs (step-09 spec),
     * also for a cancelled run — so a run without a decision cannot be assembled from step results.
     */
    public PipelineResults(final ParseXmlResult parse, final DetectScenariosResult detect,
            final SelectScenarioAction.SelectScenarioResult select, final RetrieveArtifactsAction.RetrieveArtifactsResult retrieve,
            final PrepareRulesAction.PrepareRulesResult prepare, final ApplyRulesAction.ApplyRulesActionResult apply,
            final ComputeConformanceAction.ComputeConformanceActionResult conformance) {
        this(parse, detect, select, retrieve, prepare, apply, conformance,
                decide(parse, detect, select, retrieve, prepare, apply, conformance));
    }

    private static DecisionRecommendationAction.DecisionRecommendationResult decide(final ParseXmlResult parse,
            final DetectScenariosResult detect, final SelectScenarioAction.SelectScenarioResult select,
            final RetrieveArtifactsAction.RetrieveArtifactsResult retrieve, final PrepareRulesAction.PrepareRulesResult prepare,
            final ApplyRulesAction.ApplyRulesActionResult apply,
            final ComputeConformanceAction.ComputeConformanceActionResult conformance) {
        final DecisionRecommendationAction action = new DecisionRecommendationAction();
        if (conformance != null) {
            return action.execute(conformance.result());
        }
        final String resourceId = parse != null && parse.getParsedSource() != null ? parse.getParsedSource().getSource().getName() : null;
        final CTActionType at = cancelledAt(parse, detect, select, retrieve, prepare, apply, conformance);
        return action.executeCancelled(at, detectionsAt(at, parse, detect, select, retrieve, prepare, apply), resourceId);
    }

    /**
     * The detections of the step that cancelled the run — what a caller shows when it reports the failure, without
     * repeating the verdict of step 9 that merely restates it.
     *
     * @return those detections, or an empty list when the run completed
     */
    public CTDetectionList cancelDetections() {
        return isCompleted() ? DetectionList.empty()
                : detectionsAt(cancelledAt(), this.parse, this.detect, this.select, this.retrieve, this.prepare, this.apply);
    }

    private static CTDetectionList detectionsAt(final CTActionType at, final ParseXmlResult parse, final DetectScenariosResult detect,
            final SelectScenarioAction.SelectScenarioResult select, final RetrieveArtifactsAction.RetrieveArtifactsResult retrieve,
            final PrepareRulesAction.PrepareRulesResult prepare, final ApplyRulesAction.ApplyRulesActionResult apply) {
        return switch (at) {
            case PARSE_DOCUMENT -> parse.getDetectionList();
            case DETECT_SCENARIOS -> detect.detections();
            case SELECT_SCENARIO -> select.detections();
            case RETRIEVE_ARTIFACTS -> retrieve.detections();
            case PREPARE_RULES -> prepare.detections();
            case APPLY_RULES -> apply.detections();
            // steps 2–7 succeeded but step 8 is missing: the run stopped without a failing step
            default -> DetectionList.empty();
        };
    }

    /**
     * The first step that did not succeed — the one that cancelled the run. Deliberately derived rather than recorded:
     * a run is a sequence of step results, and which of them stopped it is a property of that sequence, not an extra
     * field somebody could forget to set.
     *
     * @return the cancelling step, or {@code null} when the run completed
     */
    public CTActionType cancelledAt() {
        return isCompleted() ? null
                : cancelledAt(this.parse, this.detect, this.select, this.retrieve, this.prepare, this.apply, this.conformance);
    }

    private static CTActionType cancelledAt(final ParseXmlResult parse, final DetectScenariosResult detect,
            final SelectScenarioAction.SelectScenarioResult select, final RetrieveArtifactsAction.RetrieveArtifactsResult retrieve,
            final PrepareRulesAction.PrepareRulesResult prepare, final ApplyRulesAction.ApplyRulesActionResult apply,
            final ComputeConformanceAction.ComputeConformanceActionResult conformance) {
        if (conformance != null) {
            return null;
        }
        if (parse != null && !parse.isSuccess()) {
            return CTActionType.PARSE_DOCUMENT;
        }
        if (detect != null && !detect.isSuccess()) {
            return CTActionType.DETECT_SCENARIOS;
        }
        if (select != null && !select.isSuccess()) {
            return CTActionType.SELECT_SCENARIO;
        }
        if (retrieve != null && !retrieve.isSuccess()) {
            return CTActionType.RETRIEVE_ARTIFACTS;
        }
        if (prepare != null && !prepare.isSuccess()) {
            return CTActionType.PREPARE_RULES;
        }
        if (apply != null && !apply.isSuccess()) {
            return CTActionType.APPLY_RULES;
        }
        return CTActionType.COMPUTE_CONFORMANCE;
    }

    /**
     * @return every detection the run produced, in pipeline order — the complete trace, including the decision of step
     *         9. Never {@code null}.
     */
    public List<CTDetection> allDetections() {
        final List<CTDetection> ret = new ArrayList<>();
        add(ret, this.parse == null ? null : this.parse.getDetectionList());
        add(ret, this.detect == null ? null : this.detect.detections());
        add(ret, this.select == null ? null : this.select.detections());
        add(ret, this.retrieve == null ? null : this.retrieve.detections());
        add(ret, this.prepare == null ? null : this.prepare.detections());
        add(ret, this.apply == null ? null : this.apply.detections());
        add(ret, this.conformance == null ? null : this.conformance.detections());
        add(ret, this.decision == null ? null : this.decision.detections());
        return ret;
    }

    private static void add(final List<CTDetection> target, final CTDetectionList detections) {
        if (detections != null) {
            target.addAll(detections.getAll());
        }
    }

    /** Whether the run reached step 8; the decision of step 9 exists in every case. */
    public boolean isCompleted() {
        return this.conformance != null;
    }

    public boolean isConformant() {
        return isCompleted() && !this.conformance.result().hasNonConformantTarget();
    }
}
