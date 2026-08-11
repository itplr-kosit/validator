package org.kosit.validator.impl.conformatron;

import java.util.List;
import java.util.stream.Collectors;

import org.conformatron.api.model.action.ECTActionType;
import org.conformatron.api.model.action.ECTCanonicalAction;
import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.action.ICTAction;
import org.conformatron.api.model.detection.ECTSeverity;
import org.conformatron.api.model.detection.ICTDetection;
import org.conformatron.api.model.detection.ICTDetectionList;
import org.conformatron.api.model.scenario.ICTScenarioMatch;

/**
 * Step 4 of the canonical pipeline, {@code SELECT_SCENARIO} (see
 * {@code conformatron-api/doc/steps/step-04-select-scenario.md}): selects <b>exactly one</b> scenario from the
 * candidate list produced by {@link DetectScenariosAction}. The step separates detection (all candidates) from
 * selection, making the ambiguity case an explicit, reportable failure instead of an accident of evaluation order.
 * <p>
 * Selection policy: strict — multiple candidates always fail ({@code scenario-ambiguous}). Resolution policies
 * (priority order, newest-version-wins) are an open question in the step spec and deliberately not implemented.
 * </p>
 *
 * @author Andreas Schmitz
 */
public class SelectScenarioAction implements ICTAction {

    /** Detection code on successful selection (INFO, scenario id as value). */
    public static final String CODE_SCENARIO_SELECTED = "scenario-selected";

    /** Detection code when more than one candidate was detected (ERROR, cancels the process). */
    public static final String CODE_SCENARIO_AMBIGUOUS = "scenario-ambiguous";

    /**
     * Result of a single execution of this action.
     *
     * @param status success or failure (failure cancels the process)
     * @param selected the selected scenario; {@code null} unless status is {@code SUCCESS}
     * @param detections this execution's contribution to the report; never {@code null}
     */
    public record SelectScenarioResult(ECTStepResult status, ICTScenarioMatch selected, ICTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == ECTStepResult.SUCCESS;
        }
    }

    @Override
    public String getName() {
        return ECTCanonicalAction.SELECT_SCENARIO.getCanonicalName();
    }

    @Override
    public ECTActionType getType() {
        return ECTCanonicalAction.SELECT_SCENARIO.getDefaultType();
    }

    /**
     * Selects exactly one scenario from the candidate list.
     *
     * @param detectedScenarios the candidate set from step 3; must not be {@code null} or empty (an empty candidate set
     *            already cancelled the process in step 3 per input constraint)
     * @return the result carrying the selected scenario and any detections
     */
    public SelectScenarioResult execute(final List<ICTScenarioMatch> detectedScenarios) {
        if (detectedScenarios == null || detectedScenarios.isEmpty()) {
            throw new IllegalArgumentException("detectedScenarios may not be null or empty (input constraint of step 4)");
        }
        final String resourceId = detectedScenarios.get(0).getParsedSource().getSource().getName();
        if (detectedScenarios.size() > 1) {
            final String candidates = detectedScenarios.stream().map(ICTScenarioMatch::getScenarioID).collect(Collectors.joining(", "));
            final ICTDetection detection = Detection.of(ECTSeverity.ERROR, CODE_SCENARIO_AMBIGUOUS,
                    DetectionLocation.ofResource(resourceId), "More than one scenario matches the document: " + candidates);
            return new SelectScenarioResult(ECTStepResult.FAILURE, null, DetectionList.of(detection));
        }
        final ICTScenarioMatch selected = detectedScenarios.get(0);
        final ICTDetection detection = Detection.of(ECTSeverity.INFO, CODE_SCENARIO_SELECTED, DetectionLocation.ofResource(resourceId),
                "Scenario '" + selected.getScenarioID() + "' selected");
        return new SelectScenarioResult(ECTStepResult.SUCCESS, selected, DetectionList.of(detection));
    }
}
