package org.kosit.validator.impl.conformatron.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.conformatron.api.model.action.CTAction;
import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.conformance.CTComputeConformanceResult;
import org.conformatron.api.model.conformance.CTConformanceResult;
import org.conformatron.api.model.conformance.CTConformanceStatement;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.rule.CTApplyRulesResult;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.scenario.CTConformanceTarget;
import org.kosit.validator.impl.conformatron.model.ComputeConformanceResult;
import org.kosit.validator.impl.conformatron.model.ConformanceStatement;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.kosit.validator.impl.conformatron.model.SubjectDetection;

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
public class ComputeConformanceAction implements CTAction {

    /** Detection code per conformant target (NONE). */
    public static final String CODE_TARGET_CONFORMANT = "target-conformant";

    /** Detection code per non-conformant target (ERROR). */
    public static final String CODE_TARGET_NON_CONFORMANT = "target-non-conformant";

    /** Detection code when the step is skipped because step 7 produced no results (NONE). */
    public static final String CODE_STEP_SKIPPED = "step-skipped";

    /**
     * Result of a single execution of this action.
     *
     * @param status success or skipped (no rule results); the computation itself does not fail — {@code NON_CONFORMANT}
     *            is a valid outcome
     * @param result the per-rule-set statements plus provenance; forwarded to step 9 even when skipped (empty)
     * @param detections this execution's contribution to the report; never {@code null}
     */
    public record ComputeConformanceActionResult(CTStepResult status, CTComputeConformanceResult result, CTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == CTStepResult.SUCCESS;
        }
    }

    @Override
    public String getName() {
        return CTActionType.COMPUTE_CONFORMANCE.getName();
    }

    @Override
    public CTActionType getType() {
        return CTActionType.COMPUTE_CONFORMANCE;
    }

    /**
     * Evaluates the conformance statements for the given targets against the step-7 results.
     *
     * @param applyRulesResult the step-7 result; an empty result skips the step (an empty
     *            {@link CTComputeConformanceResult} is still forwarded per spec)
     * @param targets the conformance targets declared by the selected scenario; targets with an {@code acceptSelector}
     *            are rejected (see class Javadoc)
     * @return the result including one statement per rule set
     */
    public ComputeConformanceActionResult execute(final CTApplyRulesResult applyRulesResult, final List<CTConformanceTarget> targets) {
        if (applyRulesResult == null) {
            throw new IllegalArgumentException("applyRulesResult may not be null");
        }
        if (targets == null || targets.isEmpty()) {
            throw new IllegalArgumentException("targets may not be null or empty");
        }
        targets.stream().filter(CTConformanceTarget::hasAcceptSelector).findFirst().ifPresent(t -> {
            throw new IllegalArgumentException("Target '" + t.getTargetID()
                    + "' declares an acceptSelector, which is not evaluable before the report model exists (ADR-004 follow-up)");
        });
        final String resourceId = applyRulesResult.getParsedSource().getSource().getName();
        if (applyRulesResult.isEmpty()) {
            final CTDetection skipped = Detection.of(CTStandardSeverity.NONE, CODE_STEP_SKIPPED, DetectionLocation.of(resourceId),
                    "No rule results to evaluate (reason: no-rule-results)");
            return new ComputeConformanceActionResult(CTStepResult.SKIPPED, ComputeConformanceResult.empty(applyRulesResult),
                    DetectionList.of(skipped));
        }
        final LinkedHashMap<CTPreparedRuleSet, CTConformanceStatement> statements = new LinkedHashMap<>();
        final List<CTDetection> detections = new ArrayList<>();
        for (final Map.Entry<CTPreparedRuleSet, CTDetectionList> entry : applyRulesResult.getResultsByRuleSet().entrySet()) {
            final CTConformanceTarget target = targetFor(entry.getKey(), targets);
            final CTConformanceStatement statement = evaluate(target, entry.getKey(), entry.getValue());
            statements.put(entry.getKey(), statement);
            detections.add(toDetection(statement, entry.getKey(), entry.getValue(), resourceId));
        }
        return new ComputeConformanceActionResult(CTStepResult.SUCCESS, new ComputeConformanceResult(applyRulesResult, statements),
                new DetectionList(detections));
    }

    private static CTConformanceTarget targetFor(final CTPreparedRuleSet ruleSet, final List<CTConformanceTarget> targets) {
        final String href = ruleSet.getArtifactReference().getValidationArtifactReference().toString();
        return targets.stream().filter(t -> t.getRuleSetReferences().contains(href)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No conformance target declares rule set '" + href + "'"));
    }

    private static CTConformanceStatement evaluate(final CTConformanceTarget target, final CTPreparedRuleSet ruleSet,
            final CTDetectionList detections) {
        final String href = ruleSet.getArtifactReference().getValidationArtifactReference().toString();
        if (wasSkipped(detections)) {
            // a rule set that never ran must not look conformant (spec: empty result -> NON_CONFORMANT)
            return ConformanceStatement.of(target, CTConformanceResult.NON_CONFORMANT, "Rule set '" + href + "' was not executed");
        }
        final long errors = detections.getCount(d -> d.getSeverity().isError());
        if (errors > 0) {
            return ConformanceStatement.of(target, CTConformanceResult.NON_CONFORMANT,
                    errors + " error detection(s) from rule set '" + href + "'");
        }
        return ConformanceStatement.of(target, CTConformanceResult.CONFORMANT, "Rule set '" + href + "' passed");
    }

    private static boolean wasSkipped(final CTDetectionList detections) {
        // an engine error is NOT "skipped": it ran and crashed — its FATAL detection drives NON_CONFORMANT directly
        return detections.getAll().stream().anyMatch(d -> ApplyRulesAction.CODE_STEP_SKIPPED.equals(d.getCode()));
    }

    /**
     * The verdict is what a consumer acts on, so it travels as an attribute rather than being derivable only from the
     * detection code; target and rule set are named and located the same way step 5 names its artifacts.
     */
    private static CTDetection toDetection(final CTConformanceStatement statement, final CTPreparedRuleSet ruleSet,
            final CTDetectionList ruleDetections, final String resourceId) {
        final String targetName = statement.getTarget().getTargetName();
        final String href = ruleSet.getArtifactReference().getValidationArtifactReference().toString();
        final boolean conformant = statement.getResult().isConformant();
        final Detection plain = conformant
                ? Detection.of(CTStandardSeverity.NONE, CODE_TARGET_CONFORMANT, DetectionLocation.of(resourceId),
                        "Target '" + targetName + "' conformant")
                : Detection.of(CTStandardSeverity.ERROR, CODE_TARGET_NON_CONFORMANT, DetectionLocation.of(resourceId),
                        "Target '" + targetName + "' non-conformant: " + statement.getRationale());
        return SubjectDetection.about(plain).identifiedBy(SubjectDetection.ATTR_TARGET_ID, statement.getTarget().getTargetID())
                .locatedAt(href).with(SubjectDetection.ATTR_CONFORMANCE, statement.getResult().name()).build();
    }
}
