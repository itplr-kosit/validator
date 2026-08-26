package org.kosit.validator.api.compact;

/**
 * Tri-state recommendation whether to accept the Input xml or not.
 */
public enum AcceptRecommendation {

    /**
     * The evaluation of the overall validation could not be computed.
     */
    UNDEFINED,

    /**
     * Recommendation is to accept Input xml based on the evaluation of the overall validation.
     */
    ACCEPTABLE,

    /**
     * Recommendation is to reject Input xml based on the evaluation of the overall validation.
     */
    REJECT
}
