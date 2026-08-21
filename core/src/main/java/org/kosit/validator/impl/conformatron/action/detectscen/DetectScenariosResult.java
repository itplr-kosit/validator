package org.kosit.validator.impl.conformatron.action.detectscen;

import java.util.List;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.scenario.CTScenarioMatch;

/**
 * Result of a single execution of this action.
 *
 * @param status success or failure (failure cancels the process)
 * @param matches all detected scenarios; empty on failure. Exactly one entry with {@code isUserSelected() == true}
 *            on the fixed-scenario path
 * @param detections this execution's contribution to the report; never {@code null}
 */
public record DetectScenariosResult(CTStepResult status, List<CTScenarioMatch> matches, CTDetectionList detections) {

    public boolean isSuccess() {
        return this.status == CTStepResult.SUCCESS;
    }
}