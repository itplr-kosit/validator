package org.conformatron.api.model.conformance;

/**
 * The machine-readable decision produced by step 9 ({@code DECISION_RECOMMENDATION}).
 * <p>
 * This step <b>always runs</b>, even when the process was cancelled in an earlier step, so that every validation run
 * produces an explicit outcome.
 * </p>
 *
 * <p>
 * <b>Mapping from reference implementation ({@code ComputeAcceptanceAction}):</b>
 * </p>
 * <ul>
 * <li>{@code AcceptRecommendation.ACCEPTABLE} → {@link #ACCEPT}</li>
 * <li>{@code AcceptRecommendation.REJECT} → {@link #REJECT}</li>
 * <li>{@code AcceptRecommendation.UNDEFINED} → {@link #EVALUATE_FURTHER}</li>
 * </ul>
 */
public enum CTDecision {
    /**
     * All relevant conformance targets are {@link CTConformanceResult#CONFORMANT}, or the {@code acceptSelector}
     * evaluated to {@code true}. The document may be accepted for further processing.
     */
    ACCEPT,

    /**
     * At least one conformance target is {@link CTConformanceResult#NON_CONFORMANT}, or the process was cancelled in a
     * preceding step (e.g. parse failure, no scenario match, engine error). The document must be rejected.
     */
    REJECT,

    /**
     * At least one conformance target is {@link CTConformanceResult#INCONCLUSIVE} and no target is
     * {@link CTConformanceResult#NON_CONFORMANT}. Manual inspection is required before a final accept/reject decision
     * can be made. Replaces the reference implementation's {@code AcceptRecommendation.UNDEFINED}.
     */
    EVALUATE_FURTHER;
}
