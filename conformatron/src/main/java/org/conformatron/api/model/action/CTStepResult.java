package org.conformatron.api.model.action;

/**
 * The execution status of a single pipeline step.
 * <p>
 * Used to mark the status of each step's result for report rendering and for step 9 ({@code DECISION_RECOMMENDATION})
 * to identify which step triggered a cancel.
 * </p>
 */
public enum CTStepResult {

    /** The step executed successfully (happy path or findings path — both are success). */
    SUCCESS,

    /**
     * The step encountered an unrecoverable error (e.g. engine failure, IO error). Causes process cancellation;
     * downstream steps are skipped except {@code DECISION_RECOMMENDATION}.
     */
    FAILURE,

    /**
     * The step was skipped because a precondition was not met (e.g. no artifacts retrieved, previous execution in the
     * loop failed). The skip reason must be recorded in the associated {@link ICTActionReport}.
     */
    SKIPPED,

    /**
     * The step was not reached because the process was cancelled by an earlier step. Distinct from {@link #SKIPPED}: a
     * cancelled step was never started, whereas a skipped step was evaluated but intentionally bypassed.
     */
    CANCELLED;

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
