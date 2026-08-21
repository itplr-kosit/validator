package org.kosit.validator.impl.conformatron.action;

import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.kosit.validator.impl.conformatron.model.ComputeConformanceResult;
import org.kosit.validator.impl.conformatron.model.ConformanceStatement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.conformatron.api.model.action.ECTActionType;
import org.conformatron.api.model.action.ECTActionType;
import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.action.ICTAction;
import org.conformatron.api.model.conformance.ECTConformanceResult;
import org.conformatron.api.model.conformance.ICTComputeConformanceResult;
import org.conformatron.api.model.conformance.ICTConformanceStatement;
import org.conformatron.api.model.detection.ECTSeverity;
import org.conformatron.api.model.detection.ICTDetection;
import org.conformatron.api.model.detection.ICTDetectionList;
import org.conformatron.api.model.rule.ICTApplyRulesResult;
import org.conformatron.api.model.rule.ICTPreparedRuleSet;
import org.conformatron.api.model.scenario.ICTConformanceTarget;

/**
 * Step 8 of the canonical pipeline, {@code COMPUTE_CONFORMANCE} (see
 * {@code conformatron-api/doc/steps/step-08-compute-conformance.md}): maps the per-rule-set findings from step 7 onto
 * per-target conformance statements. Purely computational — no document access, no repository access.
 * <p>
 * Granularity per spec: one statement per {@code (rule set, detections)} pair, preserving traceability of which rule
 * set drove which outcome. Verdicts: error-band detections → {@code NON_CONFORMANT}; a rule set that was never executed
 * ({@code step-skipped} marker from step 7) → {@code NON_CONFORMANT} with rationale ("not executed" must not look
 * conformant); otherwise {@code CONFORMANT}. {@code INCONCLUSIVE} is blocked on issue 04a (severity model gap) and not
 * produced yet.
 * </p>
 * <p>
 * <b>Deferred</b>: targets with an {@code acceptSelector} are rejected — the selector is evaluated against the rendered
 * report, which does not exist in the canonical pipeline yet (ADR-004 follow-up).
 * </p>
 *
 * @author Andreas Schmitz
 */
public class ComputeConformanceAction implements ICTAction {

    /** Detection code per conformant target (INFO). */
    public static final String CODE_TARGET_CONFORMANT = "target-conformant";

    /** Detection code per non-conformant target (ERROR). */
    public static final String CODE_TARGET_NON_CONFORMANT = "target-non-conformant";

    /** Detection code when the step is skipped because step 7 produced no results (INFO). */
    public static final String CODE_STEP_SKIPPED = "step-skipped";

    /**
     * Result of a single execution of this action.
     *
     * @param status success or skipped (no rule results); the computation itself does not fail — {@code NON_CONFORMANT}
     *            is a valid outcome
     * @param result the per-rule-set statements plus provenance; forwarded to step 9 even when skipped (empty)
     * @param detections this execution's contribution to the report; never {@code null}
     */
    public record ComputeConformanceActionResult(ECTStepResult status, ICTComputeConformanceResult result, ICTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == ECTStepResult.SUCCESS;
        }
    }

    @Override
    public String getName() {
        return ECTActionType.COMPUTE_CONFORMANCE.getName();
    }

    @Override
    public ECTActionType getType() {
        return ECTActionType.COMPUTE_CONFORMANCE;
    }

    /**
     * Evaluates the conformance statements for the given targets against the step-7 results.
     *
     * @param applyRulesResult the step-7 result; an empty result skips the step (an empty
     *            {@link ICTComputeConformanceResult} is still forwarded per spec)
     * @param targets the conformance targets declared by the selected scenario; targets with an {@code acceptSelector}
     *            are rejected (see class Javadoc)
     * @return the result including one statement per rule set
     */
    public ComputeConformanceActionResult execute(final ICTApplyRulesResult applyRulesResult, final List<ICTConformanceTarget> targets) {
        if (applyRulesResult == null) {
            throw new IllegalArgumentException("applyRulesResult may not be null");
        }
        if (targets == null || targets.isEmpty()) {
            throw new IllegalArgumentException("targets may not be null or empty");
        }
        targets.stream().filter(ICTConformanceTarget::hasAcceptSelector).findFirst().ifPresent(t -> {
            throw new IllegalArgumentException("Target '" + t.getTargetID()
                    + "' declares an acceptSelector, which is not evaluable before the report model exists (ADR-004 follow-up)");
        });
        final String resourceId = applyRulesResult.getParsedSource().getSource().getName();
        if (applyRulesResult.isEmpty()) {
            final ICTDetection skipped = Detection.of(ECTSeverity.INFO, CODE_STEP_SKIPPED, DetectionLocation.ofResource(resourceId),
                    "No rule results to evaluate (reason: no-rule-results)");
            return new ComputeConformanceActionResult(ECTStepResult.SKIPPED, ComputeConformanceResult.empty(applyRulesResult),
                    DetectionList.of(skipped));
        }
        final LinkedHashMap<ICTPreparedRuleSet, ICTConformanceStatement> statements = new LinkedHashMap<>();
        final List<ICTDetection> detections = new ArrayList<>();
        for (final Map.Entry<ICTPreparedRuleSet, ICTDetectionList> entry : applyRulesResult.getResultsByRuleSet().entrySet()) {
            final ICTConformanceTarget target = targetFor(entry.getKey(), targets);
            final ICTConformanceStatement statement = evaluate(target, entry.getKey(), entry.getValue());
            statements.put(entry.getKey(), statement);
            detections.add(toDetection(statement, entry.getKey(), entry.getValue(), resourceId));
        }
        return new ComputeConformanceActionResult(ECTStepResult.SUCCESS, new ComputeConformanceResult(applyRulesResult, statements),
                new DetectionList(detections));
    }

    private static ICTConformanceTarget targetFor(final ICTPreparedRuleSet ruleSet, final List<ICTConformanceTarget> targets) {
        final String href = ruleSet.getArtifactReference().getValidationArtifactReference().toString();
        return targets.stream().filter(t -> t.getRuleSetReferences().contains(href)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No conformance target declares rule set '" + href + "'"));
    }

    private static ICTConformanceStatement evaluate(final ICTConformanceTarget target, final ICTPreparedRuleSet ruleSet,
            final ICTDetectionList detections) {
        final String href = ruleSet.getArtifactReference().getValidationArtifactReference().toString();
        if (wasSkipped(detections)) {
            // a rule set that never ran must not look conformant (spec: empty result -> NON_CONFORMANT)
            return ConformanceStatement.of(target, ECTConformanceResult.NON_CONFORMANT, "Rule set '" + href + "' was not executed");
        }
        final long errors = detections.getCount(d -> d.getSeverity().isError());
        if (errors > 0) {
            return ConformanceStatement.of(target, ECTConformanceResult.NON_CONFORMANT,
                    errors + " error detection(s) from rule set '" + href + "'");
        }
        return ConformanceStatement.of(target, ECTConformanceResult.CONFORMANT, "Rule set '" + href + "' passed");
    }

    private static boolean wasSkipped(final ICTDetectionList detections) {
        // an engine error is NOT "skipped": it ran and crashed — its FATAL detection drives NON_CONFORMANT directly
        return detections.getAll().stream().anyMatch(d -> ApplyRulesAction.CODE_STEP_SKIPPED.equals(d.getCode()));
    }

    private static ICTDetection toDetection(final ICTConformanceStatement statement, final ICTPreparedRuleSet ruleSet,
            final ICTDetectionList ruleDetections, final String resourceId) {
        final String targetName = statement.getTarget().getTargetName();
        final String href = ruleSet.getArtifactReference().getValidationArtifactReference().toString();
        if (statement.getResult() == ECTConformanceResult.CONFORMANT) {
            return Detection.of(ECTSeverity.INFO, CODE_TARGET_CONFORMANT, DetectionLocation.ofResource(resourceId),
                    "Target '" + targetName + "' conformant (rule set '" + href + "')");
        }
        return Detection.of(ECTSeverity.ERROR, CODE_TARGET_NON_CONFORMANT, DetectionLocation.ofResource(resourceId),
                "Target '" + targetName + "' non-conformant: " + statement.getRationale());
    }
}
