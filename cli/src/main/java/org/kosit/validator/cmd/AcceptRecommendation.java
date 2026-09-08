package org.kosit.validator.cmd;

/**
 * The vocabulary of the {@code Acceptance} column of the result table — the words of 1.6, kept because the text output
 * of the CLI is kept. The engine decides in {@code CTDecision} ({@code ACCEPT}, {@code REJECT},
 * {@code EVALUATE_FURTHER}); {@link ResultTable} maps that onto these three.
 * <p>
 * This is display vocabulary of the CLI and nothing else: it used to live in the API as the result of the legacy
 * acceptance computation and moved here when that computation went.
 * </p>
 */
enum AcceptRecommendation {

    /** The decision could not be made — the CVR word is {@code EVALUATE_FURTHER}. */
    UNDEFINED,

    /** Accept the document — the CVR word is {@code ACCEPT}. */
    ACCEPTABLE,

    /** Reject the document. */
    REJECT;
}
