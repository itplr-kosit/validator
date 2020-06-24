package de.kosit.validationtool.api;

/**
 * Tri-state recommendation whether to accept the {@link Input} or not.
 */
public enum AcceptRecommendation {

    /**
     * The evaluation of the overall validation could not be computed.
     */
    UNDEFINED,

    /**
     * Recommendation is to accept {@link Input} based on the evaluation of the overall validation.
     */
    ACCEPTABLE,

    /**
     * Recommendation is to reject {@link Input}  based on the evaluation of the overall validation.
     */
    REJECT
}
