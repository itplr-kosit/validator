package org.kosit.validator.impl.conformatron.action;

import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.kosit.validator.impl.conformatron.model.ScenarioMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.conformatron.api.model.action.ECTActionType;
import org.conformatron.api.model.action.ECTActionType;
import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.action.ICTAction;
import org.conformatron.api.model.detection.ECTSeverity;
import org.conformatron.api.model.detection.ICTDetection;
import org.conformatron.api.model.detection.ICTDetectionList;
import org.conformatron.api.model.scenario.ICTScenarioMatch;
import org.conformatron.api.model.source.ICTParsedValidationSource;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.ScenarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;

/**
 * Step 3 of the canonical pipeline, {@code DETECT_SCENARIOS} (see
 * {@code conformatron-api/doc/steps/step-03-detect-scenarios.md}): determines <b>all</b> validation scenarios
 * applicable to the parsed document — either fixed by user input ({@code requestedScenarioId}) or auto-detected via the
 * XPath match expressions of the configured scenarios. Picking exactly one is the job of {@link SelectScenarioAction}
 * (step 4).
 * <p>
 * Facade strategy: the legacy {@link ScenarioRepository} keeps doing the heavy lifting
 * ({@link ScenarioRepository#findMatches(XdmNode)}). Note the spec'd behavioral difference to the legacy pipeline: "no
 * match" is a <b>failure</b> (cancel + partial CVRL), not a fallback-scenario continuation.
 * </p>
 * <p>
 * XPath evaluation requires the Saxon representation: the parsed content of the supplied
 * {@link ICTParsedValidationSource} is either an {@link XdmNode} (legacy facade) or — when a {@link Processor} is
 * configured — any source providing a DOM via {@code getAsDom()} (ADR-002 common denominator), which is then wrapped
 * into the Saxon model without re-parsing. This closes the gap between the DOM-based step-2 reference action and the
 * Saxon-based scenario matching.
 * </p>
 *
 * @author Andreas Schmitz
 */
public class DetectScenariosAction implements ICTAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DetectScenariosAction.class);

    /** Detection code per auto-detected match (INFO, one per matched scenario). */
    public static final String CODE_SCENARIO_MATCHED = "scenario-matched";

    /** Detection code when the scenario was fixed by user input (INFO). */
    public static final String CODE_SCENARIO_USER_SELECTED = "scenario-user-selected";

    /** Detection code when no scenario match expression fires (ERROR, cancels the process). */
    public static final String CODE_NO_SCENARIO_MATCHED = "no-scenario-matched";

    /** Detection code when the requested scenario id is not configured (ERROR, cancels the process). */
    public static final String CODE_SCENARIO_UNKNOWN_ID = "scenario-unknown-id";

    private final ScenarioRepository repository;

    private final Processor processor;

    public DetectScenariosAction(final ScenarioRepository repository) {
        this(repository, null);
    }

    /**
     * @param repository the scenario repository providing the match expressions
     * @param processor optional Saxon processor used to wrap non-Saxon parsed content (e.g. the W3C DOM produced by the
     *            step-2 reference action) into the {@link XdmNode} the match evaluation needs. Must be the same
     *            processor the match executables were compiled with. If {@code null}, only {@link XdmNode} parsed
     *            content is accepted
     */
    public DetectScenariosAction(final ScenarioRepository repository, final Processor processor) {
        if (repository == null) {
            throw new IllegalArgumentException("repository may not be null");
        }
        this.repository = repository;
        this.processor = processor;
    }

    /**
     * Result of a single execution of this action.
     *
     * @param status success or failure (failure cancels the process)
     * @param matches all detected scenarios; empty on failure. Exactly one entry with {@code isUserSelected() == true}
     *            on the fixed-scenario path
     * @param detections this execution's contribution to the report; never {@code null}
     */
    public record DetectScenariosResult(ECTStepResult status, List<ICTScenarioMatch> matches, ICTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == ECTStepResult.SUCCESS;
        }
    }

    @Override
    public String getName() {
        return ECTActionType.DETECT_SCENARIOS.getName();
    }

    @Override
    public ECTActionType getType() {
        return ECTActionType.DETECT_SCENARIOS;
    }

    /**
     * Auto-detects all matching scenarios for the parsed document.
     *
     * @param parsedSource the parsed source from step 2; parsed content must be an {@link XdmNode}
     * @return the result including all matches and any detections
     */
    public DetectScenariosResult execute(final ICTParsedValidationSource parsedSource) {
        return execute(parsedSource, null);
    }

    /**
     * Detects the applicable scenarios — fixed by {@code requestedScenarioId} or auto-detected via XPath.
     *
     * @param parsedSource the parsed source from step 2; parsed content must be an {@link XdmNode}
     * @param requestedScenarioId optional user-fixed scenario name; bypasses XPath evaluation
     * @return the result including all matches and any detections
     */
    public DetectScenariosResult execute(final ICTParsedValidationSource parsedSource, final String requestedScenarioId) {
        final XdmNode document = requireXdmNode(parsedSource);
        if (requestedScenarioId != null) {
            return detectByRequestedId(parsedSource, requestedScenarioId);
        }
        return detectByMatchExpressions(parsedSource, document);
    }

    private DetectScenariosResult detectByRequestedId(final ICTParsedValidationSource parsedSource, final String requestedScenarioId) {
        final String resourceId = parsedSource.getSource().getName();
        final Scenario scenario = this.repository.getScenarios().stream()
                .filter(s -> requestedScenarioId.equals(s.getName()) && !s.isFallback()).findFirst().orElse(null);
        if (scenario == null) {
            final ICTDetection detection = Detection.of(ECTSeverity.ERROR, CODE_SCENARIO_UNKNOWN_ID,
                    DetectionLocation.ofResource(resourceId), "Requested scenario '" + requestedScenarioId + "' is not configured");
            return new DetectScenariosResult(ECTStepResult.FAILURE, List.of(), DetectionList.of(detection));
        }
        final ScenarioMatch match = ScenarioMatch.userSelected(scenario, parsedSource);
        final ICTDetection detection = Detection.of(ECTSeverity.INFO, CODE_SCENARIO_USER_SELECTED, DetectionLocation.ofResource(resourceId),
                "Scenario '" + scenario.getName() + "' fixed by user input");
        return new DetectScenariosResult(ECTStepResult.SUCCESS, List.of(match), DetectionList.of(detection));
    }

    private DetectScenariosResult detectByMatchExpressions(final ICTParsedValidationSource parsedSource, final XdmNode document) {
        final String resourceId = parsedSource.getSource().getName();
        final List<Scenario> matching = this.repository.findMatches(document);
        if (matching.isEmpty()) {
            final ICTDetection detection = Detection.of(ECTSeverity.ERROR, CODE_NO_SCENARIO_MATCHED,
                    DetectionLocation.ofResource(resourceId), "None of the configured scenarios matches the document");
            return new DetectScenariosResult(ECTStepResult.FAILURE, List.of(), DetectionList.of(detection));
        }
        LOGGER.debug("{} scenario(s) matched for {}", matching.size(), resourceId);
        final List<ICTScenarioMatch> matches = matching.stream()
                .map(scenario -> (ICTScenarioMatch) ScenarioMatch.of(scenario, parsedSource)).collect(Collectors.toList());
        final List<ICTDetection> detections = new ArrayList<>();
        for (final ICTScenarioMatch match : matches) {
            detections.add(Detection.of(ECTSeverity.INFO, CODE_SCENARIO_MATCHED, DetectionLocation.ofResource(resourceId),
                    "Scenario '" + match.getScenarioName() + "' matched"));
        }
        return new DetectScenariosResult(ECTStepResult.SUCCESS, List.copyOf(matches), new DetectionList(detections));
    }

    private XdmNode requireXdmNode(final ICTParsedValidationSource parsedSource) {
        if (parsedSource == null) {
            throw new IllegalArgumentException("parsedSource may not be null");
        }
        if (parsedSource.getParsedContent() instanceof final XdmNode node) {
            return node;
        }
        // ADR-002 common denominator: every parsed source can provide a DOM — wrap it into the Saxon model
        if (parsedSource.getAsDom() != null && this.processor != null) {
            return this.processor.newDocumentBuilder().wrap(parsedSource.getAsDom());
        }
        throw new IllegalArgumentException("Scenario detection requires an XdmNode as parsed content (or a DOM plus a "
                + "configured processor), but got "
                + (parsedSource.getParsedContent() == null ? "null" : parsedSource.getParsedContent().getClass().getName()));
    }
}
