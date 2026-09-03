package org.conformatron.api.model.conformance;

import java.util.LinkedHashMap;
import java.util.List;

import org.conformatron.api.model.rule.CTApplyRulesResult;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.jspecify.annotations.NonNull;

/**
 * The complete output of step 8 ({@code COMPUTE_CONFORMANCE}).
 * <p>
 * Carries the full provenance chain required by step 9 ({@code DECISION_RECOMMENDATION}): the parsed source, the
 * apply-rules result (ordered per rule set), and the per-rule-set conformance statements.
 * </p>
 * <p>
 * The {@link LinkedHashMap} preserves scenario execution order so step 9 can identify exactly which rule set drove
 * which conformance outcome when building the decision summary.
 * </p>
 */
public interface CTComputeConformanceResult {

    /**
     * @return The parsed source carried through from step 2.
     */
    @NonNull
    CTParsedValidationSource getParsedSource();

    /**
     * @return The full apply-rules result from step 7 (ordered map of rule set → detection list). May represent a
     *         partial run if the process was cancelled.
     */
    @NonNull
    CTApplyRulesResult getApplyRulesResult();

    /**
     * @return Conformance statements keyed by rule set, in scenario execution order. Each value pairs the conformance
     *         statement for the target associated with that rule set with the detections that drove the verdict. Never
     *         {@code null}; may be empty if no rule sets were executed.
     */
    @NonNull
    LinkedHashMap<CTPreparedRuleSet, CTConformanceStatement> getStatementsByRuleSet();

    /**
     * @return A flat list of all conformance statements, in rule-set execution order. Convenience accessor for step 9.
     */
    @NonNull
    default List<CTConformanceStatement> getAllStatements() {
        return List.copyOf(getStatementsByRuleSet().values());
    }

    /**
     * @return {@code true} if any target is {@link CTConformanceResult#NON_CONFORMANT}.
     */
    default boolean hasNonConformantTarget() {
        return getStatementsByRuleSet().values().stream().anyMatch(s -> s.getResult() == CTConformanceResult.NON_CONFORMANT);
    }

    /**
     * @return {@code true} if any target is {@link CTConformanceResult#INCONCLUSIVE} and no target is
     *         {@link CTConformanceResult#NON_CONFORMANT}.
     */
    default boolean isInconclusive() {
        return !hasNonConformantTarget()
                && getStatementsByRuleSet().values().stream().anyMatch(s -> s.getResult() == CTConformanceResult.INCONCLUSIVE);
    }

    /**
     * @return {@code true} if the result set is empty (process was cancelled before step 7).
     */
    default boolean isEmpty() {
        return getStatementsByRuleSet().isEmpty();
    }
}
