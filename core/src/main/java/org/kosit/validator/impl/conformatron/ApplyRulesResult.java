package org.kosit.validator.impl.conformatron;

import java.util.LinkedHashMap;

import org.conformatron.api.model.detection.ICTDetectionList;
import org.conformatron.api.model.rule.ICTApplyRulesResult;
import org.conformatron.api.model.rule.ICTPreparedRuleSet;
import org.conformatron.api.model.source.ICTParsedValidationSource;

/**
 * Validator implementation of {@link ICTApplyRulesResult} (conformatron-api step 7, {@code APPLY_RULES}): one detection
 * list per prepared rule set, in scenario execution order. Skipped and failed executions keep their key — their
 * detection list carries the {@code step-skipped} / {@code rule-engine-error} marker.
 *
 * @author Andreas Schmitz
 */
public final class ApplyRulesResult implements ICTApplyRulesResult {

    private final ICTParsedValidationSource parsedSource;

    private final LinkedHashMap<ICTPreparedRuleSet, ICTDetectionList> resultsByRuleSet;

    public ApplyRulesResult(final ICTParsedValidationSource parsedSource,
            final LinkedHashMap<ICTPreparedRuleSet, ICTDetectionList> resultsByRuleSet) {
        if (parsedSource == null) {
            throw new IllegalArgumentException("parsedSource may not be null");
        }
        if (resultsByRuleSet == null) {
            throw new IllegalArgumentException("resultsByRuleSet may not be null");
        }
        this.parsedSource = parsedSource;
        this.resultsByRuleSet = new LinkedHashMap<>(resultsByRuleSet);
    }

    /**
     * @param parsedSource the parsed source from step 2
     * @return an empty result (no rule sets were executed)
     */
    public static ApplyRulesResult empty(final ICTParsedValidationSource parsedSource) {
        return new ApplyRulesResult(parsedSource, new LinkedHashMap<>());
    }

    @Override
    public ICTParsedValidationSource getParsedSource() {
        return this.parsedSource;
    }

    @Override
    public LinkedHashMap<ICTPreparedRuleSet, ICTDetectionList> getResultsByRuleSet() {
        return new LinkedHashMap<>(this.resultsByRuleSet);
    }
}
