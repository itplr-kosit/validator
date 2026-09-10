package org.kosit.validator.impl.conformatron.action.detectscen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.conformatron.api.model.action.CTAction;
import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.kosit.conformatron.detection.Detection;
import org.kosit.conformatron.detection.DetectionList;
import org.kosit.conformatron.detection.SubjectDetection;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction;
import org.kosit.validator.impl.conformatron.model.ScenarioMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

/**
 * Step 3 of the canonical pipeline, {@code DETECT_SCENARIOS} (see
 * {@code conformatron-api/doc/steps/step-03-detect-scenarios.md}): determines <b>all</b> validation scenarios
 * applicable to the parsed document — either fixed by user input ({@code requestedScenarioId}) or auto-detected via the
 * XPath match expressions of the configured scenarios. Picking exactly one is the job of {@link SelectScenarioAction}
 * (step 4).
 * <p>
 * The candidates are the scenarios whose match expression is true for the document, plus every scenario that applies
 * unconditionally ({@link Scenario#isUnconditional()}). Note the behavioral difference to 1.x: "no match" is a
 * <b>failure</b> (cancel + partial CVR), not a fallback-scenario continuation.
 * </p>
 * <p>
 * A match expression that can not be evaluated over the document is a failure of its own
 * ({@value #CODE_SCENARIO_MATCH_ERROR}) and cancels the run. It is not counted as a non-match: the step would then
 * select a different scenario, or none, on the strength of a question it never answered, and the report would not say
 * so. All scenarios are evaluated before the run is cancelled, so the report names every broken expression at once.
 * </p>
 * <p>
 * XPath evaluation requires the Saxon representation: the parsed content of the supplied
 * {@link CTParsedValidationSource} is either an {@link XdmNode} (legacy facade) or — when a {@link Processor} is
 * configured — any source providing a DOM via {@code getAsDom()} (ADR-002 common denominator), which is then wrapped
 * into the Saxon model without re-parsing. This closes the gap between the DOM-based step-2 reference action and the
 * Saxon-based scenario matching.
 * </p>
 *
 * @author Andreas Schmitz
 */
public class DetectScenariosAction implements CTAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DetectScenariosAction.class);

    /** Detection code per auto-detected match (INFO, one per matched scenario). */
    public static final String CODE_SCENARIO_MATCHED = "scenario-matched";

    /** Detection code when the scenario was fixed by user input (INFO). */
    public static final String CODE_SCENARIO_USER_SELECTED = "scenario-user-selected";

    /** Detection code when no scenario match expression fires (ERROR, cancels the process). */
    public static final String CODE_NO_SCENARIO_MATCHED = "no-scenario-matched";

    /** Detection code when the requested scenario id is not configured (ERROR, cancels the process). */
    public static final String CODE_SCENARIO_UNKNOWN_ID = "scenario-unknown-id";

    /** Detection code when a match expression can not be evaluated over the document (ERROR, cancels the process). */
    public static final String CODE_SCENARIO_MATCH_ERROR = "scenario-match-error";

    private final List<Scenario> scenarios;

    private final Processor processor;

    /**
     * @param scenarios the configured scenarios, in configuration order; the match runs over all of them
     */
    public DetectScenariosAction(final List<Scenario> scenarios) {
        this(scenarios, null);
    }

    /**
     * @param scenarios the configured scenarios, in configuration order; the match runs over all of them
     * @param processor optional Saxon processor used to wrap non-Saxon parsed content (e.g. the W3C DOM produced by the
     *            step-2 reference action) into the {@link XdmNode} the match evaluation needs. Must be the same
     *            processor the match executables were compiled with. If {@code null}, only {@link XdmNode} parsed
     *            content is accepted
     */
    public DetectScenariosAction(final List<Scenario> scenarios, final Processor processor) {
        Objects.requireNonNull(scenarios);
        if (scenarios.isEmpty()) {
            throw new IllegalArgumentException("At least one scenario is required");
        }
        this.scenarios = List.copyOf(scenarios);
        this.processor = processor;
    }

    @Override
    public String getName() {
        return CTActionType.DETECT_SCENARIOS.getName();
    }

    @Override
    public CTActionType getType() {
        return CTActionType.DETECT_SCENARIOS;
    }

    /**
     * Auto-detects all matching scenarios for the parsed document.
     *
     * @param parsedSource the parsed source from step 2; parsed content must be an {@link XdmNode}
     * @return the result including all matches and any detections
     */
    public DetectScenariosResult execute(final CTParsedValidationSource parsedSource) {
        return execute(parsedSource, null);
    }

    /**
     * Detects the applicable scenarios — fixed by {@code requestedScenarioId} or auto-detected via XPath.
     *
     * @param parsedSource the parsed source from step 2; parsed content must be an {@link XdmNode}
     * @param requestedScenarioId optional user-fixed scenario name; bypasses XPath evaluation
     * @return the result including all matches and any detections
     */
    public DetectScenariosResult execute(final CTParsedValidationSource parsedSource, final String requestedScenarioId) {
        final XdmNode document = requireXdmNode(parsedSource);
        if (requestedScenarioId != null) {
            return detectByRequestedId(parsedSource, requestedScenarioId);
        }
        return detectByMatchExpressions(parsedSource, document);
    }

    private DetectScenariosResult detectByRequestedId(final CTParsedValidationSource parsedSource, final String requestedScenarioId) {
        final String resourceId = parsedSource.getSource().getName();
        // the report names a scenario by its id where the configuration declares one, so a caller may hand back
        // either that id or the scenario name
        final Scenario scenario = this.scenarios.stream()
                .filter(s -> requestedScenarioId.equals(s.getName()) || requestedScenarioId.equals(s.getConfiguration().getId()))
                .findFirst().orElse(null);
        if (scenario == null) {
            final CTDetection detection = Detection.builderError().code(CODE_SCENARIO_UNKNOWN_ID).location(resourceId)
                    .text("Requested scenario '" + requestedScenarioId + "' is not configured").build();
            return new DetectScenariosResult(CTStepResult.FAILURE, List.of(), new DetectionList(detection));
        }

        final ScenarioMatch match = ScenarioMatch.userSelected(scenario, parsedSource);
        final CTDetection detection = SubjectDetection
                .about(Detection.builderNone().code(CODE_SCENARIO_USER_SELECTED).location(resourceId)
                        .text("Scenario '" + scenario.getName() + "' fixed by user input").build())
                .identifiedBy(SubjectDetection.ATTR_SCENARIO_ID, match.getScenarioID()).locatedByXPath(match.getConfigurationLocation())
                .inFile(match.getDefinitionFile()).build();
        return new DetectScenariosResult(CTStepResult.SUCCESS, List.of(match), new DetectionList(detection));
    }

    private DetectScenariosResult detectByMatchExpressions(final CTParsedValidationSource parsedSource, final XdmNode document) {
        final String resourceId = parsedSource.getSource().getName();
        // a scenario without a match expression applies unconditionally and is always among the candidates
        final List<Scenario> matching = new ArrayList<>();
        final List<CTDetection> matchErrors = new ArrayList<>();
        for (final Scenario scenario : this.scenarios) {
            try {
                if (scenario.matches(document)) {
                    matching.add(scenario);
                }
            } catch (final SaxonApiException e) {
                // deliberately not a non-match: the engine can not tell whether this scenario applies, so it must not
                // answer as if it could. Reported here and the run is cancelled - see Scenario#matches
                LOGGER.error("Error evaluating the match expression of scenario '{}'", scenario.getName(), e);
                matchErrors.add(Detection.builderError().code(CODE_SCENARIO_MATCH_ERROR).location(resourceId)
                        .text("The match expression of scenario '" + scenario.getName() + "' could not be evaluated: " + e.getMessage())
                        .build());
            }
        }
        if (!matchErrors.isEmpty()) {
            // every scenario is evaluated before the run is cancelled, so the report names every broken expression
            return new DetectScenariosResult(CTStepResult.FAILURE, List.of(), new DetectionList(matchErrors));
        }

        if (matching.isEmpty()) {
            final CTDetection detection = Detection.builderError().code(CODE_NO_SCENARIO_MATCHED).location(resourceId)
                    .text("None of the configured scenarios matches the document").build();
            return new DetectScenariosResult(CTStepResult.FAILURE, List.of(), new DetectionList(detection));
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug(matching.size() + " scenario(s) matched for " + resourceId);
        final List<ScenarioMatch> matches = matching.stream().map(scenario -> ScenarioMatch.of(scenario, parsedSource)).toList();
        final List<CTDetection> detections = new ArrayList<>();
        for (final ScenarioMatch match : matches) {
            // scenario id and the pointer into the configuration travel with every candidate
            final String text = match.getScenario().isUnconditional() ? "Scenario '" + match.getScenarioName() + "' applies unconditionally"
                    : "Scenario '" + match.getScenarioName() + "' matched";
            detections
                    .add(SubjectDetection.about(Detection.builderNone().code(CODE_SCENARIO_MATCHED).location(resourceId).text(text).build())
                            .identifiedBy(SubjectDetection.ATTR_SCENARIO_ID, match.getScenarioID())
                            .locatedByXPath(match.getConfigurationLocation()).inFile(match.getDefinitionFile()).build());
        }
        return new DetectScenariosResult(CTStepResult.SUCCESS, List.copyOf(matches), new DetectionList(detections));
    }

    private XdmNode requireXdmNode(final CTParsedValidationSource parsedSource) {
        Objects.requireNonNull(parsedSource);
        if (parsedSource.getParsedContent() instanceof final XdmNode node) {
            return node;
        }

        // ADR-002 common denominator: every parsed source can provide a DOM — wrap it into the Saxon model
        if (parsedSource.isParsed() && processor != null) {
            return processor.newDocumentBuilder().wrap(parsedSource.getParsedContent());
        }

        throw new IllegalArgumentException(
                "Scenario detection requires an XdmNode as parsed content (or a DOM plus a " + "configured processor), but got "
                        + (parsedSource.getParsedContent() == null ? "null" : parsedSource.getParsedContent().getClass().getName()));
    }
}
