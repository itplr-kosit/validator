package org.kosit.validator.impl.conformatron.model;

import java.util.LinkedHashMap;

import org.conformatron.api.model.conformance.ICTComputeConformanceResult;
import org.conformatron.api.model.conformance.ICTConformanceStatement;
import org.conformatron.api.model.rule.ICTApplyRulesResult;
import org.conformatron.api.model.rule.ICTPreparedRuleSet;
import org.conformatron.api.model.source.ICTParsedValidationSource;

/**
 * Validator implementation of {@link ICTComputeConformanceResult} (conformatron-api step 8,
 * {@code COMPUTE_CONFORMANCE}): the per-rule-set conformance statements plus the full provenance chain (parsed source
 * and apply-rules result) required by step 9.
 *
 * @author Andreas Schmitz
 */
public final class ComputeConformanceResult implements ICTComputeConformanceResult {

    private final ICTApplyRulesResult applyRulesResult;

    private final LinkedHashMap<ICTPreparedRuleSet, ICTConformanceStatement> statementsByRuleSet;

    public ComputeConformanceResult(final ICTApplyRulesResult applyRulesResult,
            final LinkedHashMap<ICTPreparedRuleSet, ICTConformanceStatement> statementsByRuleSet) {
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
    public static ComputeConformanceResult empty(final ICTApplyRulesResult applyRulesResult) {
        return new ComputeConformanceResult(applyRulesResult, new LinkedHashMap<>());
    }

    @Override
    public ICTParsedValidationSource getParsedSource() {
        return this.applyRulesResult.getParsedSource();
    }

    @Override
    public ICTApplyRulesResult getApplyRulesResult() {
        return this.applyRulesResult;
    }

    @Override
    public LinkedHashMap<ICTPreparedRuleSet, ICTConformanceStatement> getStatementsByRuleSet() {
        return new LinkedHashMap<>(this.statementsByRuleSet);
    }
}
