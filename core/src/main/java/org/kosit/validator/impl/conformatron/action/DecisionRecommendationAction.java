package org.kosit.validator.impl.conformatron.action;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.conformatron.api.model.action.CTAction;
import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.conformance.CTComputeConformanceResult;
import org.conformatron.api.model.conformance.CTConformanceResult;
import org.conformatron.api.model.conformance.CTConformanceStatement;
import org.conformatron.api.model.conformance.CTDecision;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.kosit.validator.impl.conformatron.model.SubjectDetection;

/**
 * Step 9 of the canonical pipeline, {@code DECISION_RECOMMENDATION}: turns the per-target conformance statements of
 * step 8 into one {@link CTDecision} for the whole run. This is the terminal step and it <b>always runs</b> — also when
 * the process was cancelled earlier, so that every run ends with an explicit, machine-readable outcome (step-09 spec).
 * <p>
 * Paths:
 * <ul>
 * <li>all targets {@code CONFORMANT} → {@link CTDecision#ACCEPT}, detection {@value #CODE_ACCEPT} (info);</li>
 * <li>any target {@code NON_CONFORMANT} → {@link CTDecision#REJECT}, detection {@value #CODE_REJECT} (error) naming the
 * targets and the failing rule sets;</li>
 * <li>process cancelled before step 8 → {@link CTDecision#REJECT}, detection {@value #CODE_REJECT} naming the
 * cancelling step and its detection codes;</li>
 * <li>any target {@code INCONCLUSIVE} and none non-conformant → {@link CTDecision#EVALUATE_FURTHER}, detection
 * {@value #CODE_EVALUATE_FURTHER} (warning).</li>
 * </ul>
 * The rationale is the detection text. {@code acceptSelector} is not evaluated here: if step 8 applied one, that is
 * already reflected in the statements it hands over.
 * </p>
 *
 * @author Andreas Schmitz
 */
public class DecisionRecommendationAction implements CTAction {

    /** Detection code: every conformance target is conformant. */
    public static final String CODE_ACCEPT = "decision-accept";

    /** Detection code: a target is non-conformant, or the run was cancelled before conformance could be computed. */
    public static final String CODE_REJECT = "decision-reject";

    /** Detection code: no target is non-conformant, but at least one is inconclusive. */
    public static final String CODE_EVALUATE_FURTHER = "decision-evaluate-further";

    /** Location resource when the run never got as far as a parsed source. */
    private static final String UNKNOWN_RESOURCE = "document";

    /**
     * The result of step 9: status, the decision with its rationale, and the single summary detection. The status is
     * always {@link CTStepResult#SUCCESS} — this step cannot fail, it only decides differently.
     */
    public record DecisionRecommendationResult(CTStepResult status, CTDecision decision, String rationale, CTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == CTStepResult.SUCCESS;
        }
    }

    @Override
    public String getName() {
        return CTActionType.DECISION_RECOMMENDATION.getName();
    }

    @Override
    public CTActionType getType() {
        return CTActionType.DECISION_RECOMMENDATION;
    }

    /**
     * Decides a run that reached step 8.
     *
     * @param conformance the step-8 result; may be empty when step 8 was skipped (no rule results)
     * @return the decision
     */
    public DecisionRecommendationResult execute(final CTComputeConformanceResult conformance) {
        if (conformance == null) {
            throw new IllegalArgumentException("conformance may not be null");
        }
        final String resourceId = conformance.getParsedSource().getSource().getName();
        if (conformance.isEmpty()) {
            // step 8 had nothing to evaluate — nothing was proven, so nothing is accepted
            return reject("No conformance statement was computed (step 8 had no rule results to evaluate)", resourceId);
        }
        if (conformance.hasNonConformantTarget()) {
            return reject(describe(conformance, CTConformanceResult.NON_CONFORMANT, "non-conformant"), resourceId);
        }
        if (conformance.isInconclusive()) {
            return result(CTDecision.EVALUATE_FURTHER, CODE_EVALUATE_FURTHER, CTStandardSeverity.WARNING,
                    describe(conformance, CTConformanceResult.INCONCLUSIVE, "inconclusive"), resourceId);
        }
        final Set<String> targets = new LinkedHashSet<>();
        conformance.getAllStatements().forEach(s -> targets.add(s.getTarget().getTargetName()));
        return result(CTDecision.ACCEPT, CODE_ACCEPT, CTStandardSeverity.NONE,
                "All " + targets.size() + " conformance target(s) conformant: " + String.join(", ", targets), resourceId);
    }

    /**
     * Decides a run that was cancelled before step 8 — always a rejection, naming the step that cancelled and what it
     * detected, so the decision alone tells a consumer where to look.
     *
     * @param cancelledAt the step that cancelled the run
     * @param detections the detections of that step
     * @param resourceId the document the run was about, or {@code null} when it never got that far
     * @return the decision
     */
    public DecisionRecommendationResult executeCancelled(final CTActionType cancelledAt, final CTDetectionList detections,
            final String resourceId) {
        if (cancelledAt == null || detections == null) {
            throw new IllegalArgumentException("cancelledAt and detections may not be null");
        }
        final Set<String> codes = new LinkedHashSet<>();
        detections.getAll().stream().filter(d -> d.getSeverity().isError()).map(CTDetection::getCode).forEach(codes::add);
        return reject("Run cancelled at " + cancelledAt.getName() + (codes.isEmpty() ? "" : ": " + String.join(", ", codes)),
                resourceId != null ? resourceId : UNKNOWN_RESOURCE);
    }

    private static DecisionRecommendationResult reject(final String rationale, final String resourceId) {
        return result(CTDecision.REJECT, CODE_REJECT, CTStandardSeverity.ERROR, rationale, resourceId);
    }

    private static DecisionRecommendationResult result(final CTDecision decision, final String code, final CTStandardSeverity severity,
            final String rationale, final String resourceId) {
        final Detection plain = Detection.of(severity, code, DetectionLocation.of(resourceId), rationale);
        final CTDetection detection = SubjectDetection.about(plain).with(SubjectDetection.ATTR_DECISION, decision.name()).build();
        return new DecisionRecommendationResult(CTStepResult.SUCCESS, decision, rationale, DetectionList.of(detection));
    }

    /**
     * "Target X non-conformant (rule set a.xsl: 2 error detection(s) …)" — one clause per statement with that result.
     */
    private static String describe(final CTComputeConformanceResult conformance, final CTConformanceResult result, final String label) {
        final List<String> clauses = conformance.getStatementsByRuleSet().entrySet().stream()
                .filter(e -> e.getValue().getResult() == result).map(e -> clause(e.getKey(), e.getValue(), label))
                .collect(Collectors.toList());
        return String.join("; ", clauses);
    }

    private static String clause(final CTPreparedRuleSet ruleSet, final CTConformanceStatement statement, final String label) {
        final String href = ruleSet.getArtifactReference().getValidationArtifactReference().toString();
        final String rationale = statement.getRationale() != null ? " — " + statement.getRationale() : "";
        return "Target '" + statement.getTarget().getTargetName() + "' " + label + " (rule set " + href + rationale + ")";
    }
}
