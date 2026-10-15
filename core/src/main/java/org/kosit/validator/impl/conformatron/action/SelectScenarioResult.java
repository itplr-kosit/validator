package org.kosit.validator.impl.conformatron.action;

import java.util.List;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.scenario.CTScenarioMatch;

/**
 * Result of a single execution of {@link SelectScenarioAction} (step {@code select-scenario}).
 * <p>
 * A type of its own, like {@code DetectScenariosResult}, and not a record nested in the action: the results of the
 * steps are what {@link org.kosit.validator.impl.conformatron.PipelineResults} is assembled from, so they may not
 * depend on where the action that produced them happens to live.
 * </p>
 *
 * @param status success or failure (failure cancels the process)
 * @param selected the selected scenario - the one matched by expression, or the first unconditional one; {@code null}
 *            unless status is {@code SUCCESS}
 * @param applied every scenario the run applies, the selected one first; empty unless status is {@code SUCCESS}
 * @param detections this execution's contribution to the report; never {@code null}
 * @author Andreas Schmitz
 */
public record SelectScenarioResult(CTStepResult status, CTScenarioMatch selected, List<CTScenarioMatch> applied,
        CTDetectionList detections) {

    public SelectScenarioResult {
        applied = applied == null ? List.of() : List.copyOf(applied);
    }

    /**
     * A result applying the selected scenario alone.
     *
     * @param status success or failure
     * @param selected the selected scenario, or {@code null}
     * @param detections the detections
     */
    public SelectScenarioResult(final CTStepResult status, final CTScenarioMatch selected, final CTDetectionList detections) {
        this(status, selected, selected == null ? List.of() : List.of(selected), detections);
    }

    public boolean isSuccess() {
        return this.status == CTStepResult.SUCCESS;
    }
}
