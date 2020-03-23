package de.kosit.validationtool.api;

/**
 * Tri-state describtion of a Recommendation.
 */
public enum AcceptRecommendation {
    /**
     * The evaluation of the overall validation could not be computed.
     */
    UNDEFINED,

    /**
     * Recommendation is to accept input based on the evaluation of the overall validation.
     */
    ACCEPTABLE,

    /**
     * Recommendation is to reject input based on the evaluation of the overall validation.
     */
    REJECT
}
