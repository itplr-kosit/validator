package org.kosit.validator.impl.conformatron.action;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.conformatron.api.model.action.CTAction;
import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.scenario.CTScenarioMatch;
import org.kosit.conformatron.detection.Detection;
import org.kosit.conformatron.detection.DetectionList;
import org.kosit.conformatron.detection.SubjectDetection;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosAction;
import org.kosit.validator.impl.conformatron.model.ScenarioMatch;
import org.kosit.validator.impl.conformatron.util.ScenarioXml;

/**
 * Step 4 of the canonical pipeline, {@code SELECT_SCENARIO} (see
 * {@code conformatron-api/doc/steps/step-04-select-scenario.md}): decides which of the candidates produced by
 * {@link DetectScenariosAction} the run applies. The step separates detection (all candidates) from selection, making
 * the ambiguity case an explicit, reportable failure instead of an accident of evaluation order.
 * <p>
 * Selection policy: among the candidates that matched <b>by expression</b> (or were named by the caller), exactly one
 * may exist — several fail with {@code scenario-ambiguous}. Candidates that apply <b>unconditionally</b> (a scenario
 * without match expression) are never ambiguous: they are applied in addition to the matched scenario, and they are
 * what remains when nothing matched by expression. The {@link SelectScenarioResult#selected() selected} scenario is the
 * matched one, or the first unconditional one when there is no matched one; {@link SelectScenarioResult#applied()
 * applied} lists everything the following steps run — one conformance target each (step 8).
 * </p>
 * <p>
 * Resolution policies among matched candidates (priority order, newest-version-wins) are an open question in the step
 * spec and deliberately not implemented.
 * </p>
 *
 * @author Andreas Schmitz
 */
public class SelectScenarioAction implements CTAction {

    /** Detection code on successful selection (INFO, one per applied scenario, scenario id as value). */
    public static final String CODE_SCENARIO_SELECTED = "scenario-selected";

    /** Detection code when more than one candidate matched by expression (ERROR, cancels the process). */
    public static final String CODE_SCENARIO_AMBIGUOUS = "scenario-ambiguous";

    /**
     * Result of a single execution of this action.
     *
     * @param status success or failure (failure cancels the process)
     * @param selected the selected scenario - the one matched by expression, or the first unconditional one;
     *            {@code null} unless status is {@code SUCCESS}
     * @param applied every scenario the run applies, the selected one first; empty unless status is {@code SUCCESS}
     * @param detections this execution's contribution to the report; never {@code null}
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

    @Override
    public String getName() {
        return CTActionType.SELECT_SCENARIO.getName();
    }

    @Override
    public CTActionType getType() {
        return CTActionType.SELECT_SCENARIO;
    }

    /**
     * Selects the scenarios the run applies from the candidate list.
     *
     * @param detectedScenarios the candidate set from step 3; must not be {@code null} or empty (an empty candidate set
     *            already cancelled the process in step 3 per input constraint)
     * @return the result carrying the selected and the applied scenarios and any detections
     */
    public SelectScenarioResult execute(final List<CTScenarioMatch> detectedScenarios) {
        if (detectedScenarios == null || detectedScenarios.isEmpty()) {
            throw new IllegalArgumentException("detectedScenarios may not be null or empty (input constraint of step 4)");
        }
        final String resourceId = detectedScenarios.get(0).getParsedSource().getSource().getName();
        final List<CTScenarioMatch> matched = detectedScenarios.stream().filter(c -> !isUnconditional(c)).toList();
        if (matched.size() > 1) {
            // the display names, as step 3 printed them; the id is what the detection attribute carries
            final String candidates = matched.stream().map(CTScenarioMatch::getScenarioName).collect(Collectors.joining(", "));
            final CTDetection detection = Detection.builderError().code(CODE_SCENARIO_AMBIGUOUS).location(resourceId)
                    .text("More than one scenario matches the document: " + candidates).build();
            return new SelectScenarioResult(CTStepResult.FAILURE, null, null, new DetectionList(detection));
        }

        // the matched scenario leads; the unconditional ones follow in candidate order
        final List<CTScenarioMatch> applied = new ArrayList<>(matched);
        detectedScenarios.stream().filter(SelectScenarioAction::isUnconditional).forEach(applied::add);
        final CTScenarioMatch selected = applied.get(0);

        final List<CTDetection> detections = new ArrayList<>();
        for (final CTScenarioMatch scenario : applied) {
            final String text = isUnconditional(scenario)
                    ? "Scenario '" + scenario.getScenarioName() + "' applies unconditionally and is selected"
                            + (scenario == selected ? "" : " in addition")
                    : "Scenario '" + scenario.getScenarioName() + "' selected";
            detections.add(selectedDetection(scenario, resourceId, text));
        }
        return new SelectScenarioResult(CTStepResult.SUCCESS, selected, applied, new DetectionList(detections));
    }

    private static boolean isUnconditional(final CTScenarioMatch candidate) {
        // a scenario the caller named is applied as named, whatever its declaration says
        return candidate instanceof final ScenarioMatch match && !match.isUserSelected() && match.getScenario().isUnconditional();
    }

    private static CTDetection selectedDetection(final CTScenarioMatch scenario, final String resourceId, final String text) {
        final Detection plain = Detection.builderNone().code(CODE_SCENARIO_SELECTED).location(resourceId).text(text).build();
        // the applied scenario additionally carries its own XML, so the report shows which rules were applied
        return scenario instanceof final ScenarioMatch match
                ? SubjectDetection.about(plain).identifiedBy(SubjectDetection.ATTR_SCENARIO_ID, match.getScenarioID())
                        .locatedByXPath(match.getConfigurationLocation()).inFile(match.getDefinitionFile())
                        .embedding(match.getConfiguration() == null ? null : ScenarioXml.toXmlBytes(match.getConfiguration())).build()
                : plain;
    }
}
