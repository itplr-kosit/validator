package org.kosit.conformatron.rule;

import java.util.LinkedHashMap;
import java.util.Objects;

import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.rule.CTApplyRulesResult;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.source.CTParsedValidationSource;

/**
 * Validator implementation of {@link CTApplyRulesResult} (conformatron-api step 7, {@code APPLY_RULES}): one detection
 * list per prepared rule set, in scenario execution order. Skipped and failed executions keep their key — their
 * detection list carries the {@code step-skipped} / {@code rule-engine-error} marker.
 *
 * @author Andreas Schmitz
 */
public final class ApplyRulesResult implements CTApplyRulesResult {

    private final CTParsedValidationSource parsedSource;

    private final LinkedHashMap<CTPreparedRuleSet, CTDetectionList> resultsByRuleSet;

    /**
     * @param parsedSource the parsed source from step 2
     * @return an empty result (no rule sets were executed)
     */
    public static ApplyRulesResult empty(final CTParsedValidationSource parsedSource) {
        return new ApplyRulesResult(parsedSource, new LinkedHashMap<>());
    }

    public ApplyRulesResult(final CTParsedValidationSource parsedSource,
            final LinkedHashMap<CTPreparedRuleSet, CTDetectionList> resultsByRuleSet) {
        Objects.requireNonNull(parsedSource);
        Objects.requireNonNull(resultsByRuleSet);

        this.parsedSource = parsedSource;
        this.resultsByRuleSet = new LinkedHashMap<>(resultsByRuleSet);
    }

    @Override
    public CTParsedValidationSource getParsedSource() {
        return this.parsedSource;
    }

    @Override
    public LinkedHashMap<CTPreparedRuleSet, CTDetectionList> getResultsByRuleSet() {
        return new LinkedHashMap<>(this.resultsByRuleSet);
    }
}
