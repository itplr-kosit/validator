package org.kosit.validator.impl.conformatron.action;

import java.util.List;
import java.util.stream.Collectors;

import org.conformatron.api.model.action.CTAction;
import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.scenario.CTScenarioMatch;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.kosit.validator.impl.conformatron.model.ScenarioDetection;
import org.kosit.validator.impl.conformatron.model.ScenarioMatch;

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
public class SelectScenarioAction implements CTAction {

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
    public record SelectScenarioResult(CTStepResult status, CTScenarioMatch selected, CTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == CTStepResult.SUCCESS;
        }
    }

    @Override
    public String getName() {
        return CTActionType.SELECT_SCENARIO.getName();
    }

    @Override
    public CTActionType getType() {
        return CTActionType.SELECT_SCENARIO;
    }

    /**
     * Selects exactly one scenario from the candidate list.
     *
     * @param detectedScenarios the candidate set from step 3; must not be {@code null} or empty (an empty candidate set
     *            already cancelled the process in step 3 per input constraint)
     * @return the result carrying the selected scenario and any detections
     */
    public SelectScenarioResult execute(final List<CTScenarioMatch> detectedScenarios) {
        if (detectedScenarios == null || detectedScenarios.isEmpty()) {
            throw new IllegalArgumentException("detectedScenarios may not be null or empty (input constraint of step 4)");
        }
        final String resourceId = detectedScenarios.get(0).getParsedSource().getSource().getName();
        if (detectedScenarios.size() > 1) {
            final String candidates = detectedScenarios.stream().map(CTScenarioMatch::getScenarioID).collect(Collectors.joining(", "));
            final CTDetection detection = Detection.of(CTStandardSeverity.ERROR, CODE_SCENARIO_AMBIGUOUS, DetectionLocation.of(resourceId),
                    "More than one scenario matches the document: " + candidates);
            return new SelectScenarioResult(CTStepResult.FAILURE, null, DetectionList.of(detection));
        }
        final CTScenarioMatch selected = detectedScenarios.get(0);
        final Detection plain = Detection.of(CTStandardSeverity.NONE, CODE_SCENARIO_SELECTED, DetectionLocation.of(resourceId),
                "Scenario '" + selected.getScenarioID() + "' selected");
        // the selected scenario additionally carries its configuration so the report can embed the scenario itself
        final CTDetection detection = selected instanceof final ScenarioMatch match
                ? ScenarioDetection.selected(plain, match.getScenarioID(), match.getConfigurationLocation(), match.getConfiguration())
                : plain;
        return new SelectScenarioResult(CTStepResult.SUCCESS, selected, DetectionList.of(detection));
    }
}
