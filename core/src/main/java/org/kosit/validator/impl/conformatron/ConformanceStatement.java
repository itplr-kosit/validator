package org.kosit.validator.impl.conformatron;

import org.conformatron.api.model.conformance.ECTConformanceResult;
import org.conformatron.api.model.conformance.ICTConformanceStatement;
import org.conformatron.api.model.scenario.ICTConformanceTarget;

/**
 * Validator implementation of {@link ICTConformanceStatement} (conformatron-api step 8, {@code COMPUTE_CONFORMANCE}):
 * the conformance verdict for one target, derived from one rule set's detections.
 *
 * @author Andreas Schmitz
 */
public final class ConformanceStatement implements ICTConformanceStatement {

    private final ICTConformanceTarget target;

    private final ECTConformanceResult result;

    private final String rationale;

    private final boolean acceptSelectorApplied;

    private ConformanceStatement(final ICTConformanceTarget target, final ECTConformanceResult result, final String rationale,
            final boolean acceptSelectorApplied) {
        if (target == null) {
            throw new IllegalArgumentException("target may not be null");
        }
        if (result == null) {
            throw new IllegalArgumentException("result may not be null");
        }
        this.target = target;
        this.result = result;
        this.rationale = rationale;
        this.acceptSelectorApplied = acceptSelectorApplied;
    }

    /**
     * Creates a statement from the default detection-based evaluation.
     *
     * @param target the evaluated target
     * @param result the verdict
     * @param rationale human-readable rationale; may be {@code null}
     * @return the statement
     */
    public static ConformanceStatement of(final ICTConformanceTarget target, final ECTConformanceResult result, final String rationale) {
        return new ConformanceStatement(target, result, rationale, false);
    }

    @Override
    public ICTConformanceTarget getTarget() {
        return this.target;
    }

    @Override
    public ECTConformanceResult getResult() {
        return this.result;
    }

    @Override
    public String getRationale() {
        return this.rationale;
    }

    @Override
    public boolean isAcceptSelectorApplied() {
        return this.acceptSelectorApplied;
    }
}
