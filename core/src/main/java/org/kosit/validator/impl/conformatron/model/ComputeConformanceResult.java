package org.kosit.validator.impl.conformatron.model;

import java.util.LinkedHashMap;

import org.conformatron.api.model.conformance.CTComputeConformanceResult;
import org.conformatron.api.model.conformance.CTConformanceStatement;
import org.conformatron.api.model.rule.CTApplyRulesResult;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.source.CTParsedValidationSource;

/**
 * Validator implementation of {@link CTComputeConformanceResult} (conformatron-api step 8,
 * {@code COMPUTE_CONFORMANCE}): the per-rule-set conformance statements plus the full provenance chain (parsed source
 * and apply-rules result) required by step 9.
 *
 * @author Andreas Schmitz
 */
public final class ComputeConformanceResult implements CTComputeConformanceResult {

    private final CTApplyRulesResult applyRulesResult;

    private final LinkedHashMap<CTPreparedRuleSet, CTConformanceStatement> statementsByRuleSet;

    public ComputeConformanceResult(final CTApplyRulesResult applyRulesResult,
            final LinkedHashMap<CTPreparedRuleSet, CTConformanceStatement> statementsByRuleSet) {
        if (applyRulesResult == null) {
            throw new IllegalArgumentException("applyRulesResult may not be null");
        }
        if (statementsByRuleSet == null) {
            throw new IllegalArgumentException("statementsByRuleSet may not be null");
        }
        this.applyRulesResult = applyRulesResult;
        this.statementsByRuleSet = new LinkedHashMap<>(statementsByRuleSet);
    }

    /**
     * @param applyRulesResult the (possibly empty) step-7 result
     * @return an empty result — forwarded to step 9 even when this step was skipped, per spec
     */
    public static ComputeConformanceResult empty(final CTApplyRulesResult applyRulesResult) {
        return new ComputeConformanceResult(applyRulesResult, new LinkedHashMap<>());
    }

    @Override
    public CTParsedValidationSource getParsedSource() {
        return this.applyRulesResult.getParsedSource();
    }

    @Override
    public CTApplyRulesResult getApplyRulesResult() {
        return this.applyRulesResult;
    }

    @Override
    public LinkedHashMap<CTPreparedRuleSet, CTConformanceStatement> getStatementsByRuleSet() {
        return new LinkedHashMap<>(this.statementsByRuleSet);
    }
}
