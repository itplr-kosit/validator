package org.conformatron.api.model.rule;

import java.util.LinkedHashMap;

import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.jspecify.annotations.NonNull;

/**
 * The complete output of step 7 ({@code APPLY_RULES}).
 * <p>
 * Contains one {@link CTDetectionList} per {@link CTPreparedRuleSet}, in the fixed execution order defined by the
 * scenario. The {@link LinkedHashMap} preserves this order. Skipped and failed executions are represented by their
 * {@link CTDetectionList} with appropriate severity/code — the key is always present.
 * </p>
 * <p>
 * Forwarded unchanged to steps 8 ({@code COMPUTE_CONFORMANCE}) and 9 ({@code DECISION_RECOMMENDATION}).
 * </p>
 */
public interface CTApplyRulesResult {

    /**
     * @return The parsed source carried through from step 2.
     */
    @NonNull
    CTParsedValidationSource getParsedSource();

    /**
     * @return Detection lists keyed by prepared rule set, in scenario execution order. Never {@code null}; may be empty
     *         if no rule sets were executed.
     */
    @NonNull
    LinkedHashMap<CTPreparedRuleSet, CTDetectionList> getResultsByRuleSet();

    /**
     * @return {@code true} if at least one rule set execution produced an error-band detection.
     */
    default boolean hasErrors() {
        return getResultsByRuleSet().values().stream().anyMatch(CTDetectionList::containsAtLeastOneError);
    }

    /**
     * @return {@code true} if no rule sets were executed (empty map).
     */
    default boolean isEmpty() {
        return getResultsByRuleSet().isEmpty();
    }
}
