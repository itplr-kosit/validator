package org.kosit.validator.impl.conformatron.action.detectscen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.conformatron.api.model.action.CTAction;
import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.scenario.CTScenarioMatch;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.ScenarioRepository;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.kosit.validator.impl.conformatron.model.SubjectDetection;
import org.kosit.validator.impl.conformatron.model.ScenarioMatch;
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

    private final ScenarioRepository repository;

    private final Processor processor;

    private String definitionFile;

    public DetectScenariosAction(final ScenarioRepository repository) {
        this(repository, null);
    }

    /**
     * The scenario configuration the repository was built from. The report locates a matched scenario both by an XPath
     * inside the configuration and by the file itself, and the file is not derivable from the legacy scenario model —
     * so whoever assembles the pipeline has to say.
     *
     * @param definitionFile the configuration file, e.g. its URI
     * @return this for chaining
     */
    public DetectScenariosAction withDefinitionFile(final String definitionFile) {
        this.definitionFile = definitionFile;
        return this;
    }

    /**
     * @param repository the scenario repository providing the match expressions
     * @param processor optional Saxon processor used to wrap non-Saxon parsed content (e.g. the W3C DOM produced by the
     *            step-2 reference action) into the {@link XdmNode} the match evaluation needs. Must be the same
     *            processor the match executables were compiled with. If {@code null}, only {@link XdmNode} parsed
     *            content is accepted
     */
    public DetectScenariosAction(final ScenarioRepository repository, final Processor processor) {
        Objects.requireNonNull(repository);
        this.repository = repository;
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
        final Scenario scenario = repository.getScenarios().stream().filter(s -> requestedScenarioId.equals(s.getName()) && !s.isFallback())
                .findFirst().orElse(null);
        if (scenario == null) {
            final CTDetection detection = Detection.of(CTStandardSeverity.ERROR, CODE_SCENARIO_UNKNOWN_ID, DetectionLocation.of(resourceId),
                    "Requested scenario '" + requestedScenarioId + "' is not configured");
            return new DetectScenariosResult(CTStepResult.FAILURE, List.of(), DetectionList.of(detection));
        }

        final ScenarioMatch match = ScenarioMatch.userSelected(scenario, parsedSource, this.definitionFile);
        final CTDetection detection = SubjectDetection
                .about(Detection.of(CTStandardSeverity.NONE, CODE_SCENARIO_USER_SELECTED, DetectionLocation.of(resourceId),
                        "Scenario '" + scenario.getName() + "' fixed by user input"))
                .identifiedBy(SubjectDetection.ATTR_SCENARIO_ID, match.getScenarioID()).locatedByXPath(match.getConfigurationLocation())
                .inFile(match.getDefinitionFile()).build();
        return new DetectScenariosResult(CTStepResult.SUCCESS, List.of(match), DetectionList.of(detection));
    }

    private DetectScenariosResult detectByMatchExpressions(final CTParsedValidationSource parsedSource, final XdmNode document) {
        final String resourceId = parsedSource.getSource().getName();
        final List<Scenario> matching = repository.findMatches(document);
        if (matching.isEmpty()) {
            final CTDetection detection = Detection.of(CTStandardSeverity.ERROR, CODE_NO_SCENARIO_MATCHED, DetectionLocation.of(resourceId),
                    "None of the configured scenarios matches the document");
            return new DetectScenariosResult(CTStepResult.FAILURE, List.of(), DetectionList.of(detection));
        }

        LOGGER.debug("{} scenario(s) matched for {}", matching.size(), resourceId);
        final List<ScenarioMatch> matches = matching.stream().map(scenario -> ScenarioMatch.of(scenario, parsedSource, this.definitionFile))
                .toList();
        final List<CTDetection> detections = new ArrayList<>();
        for (final ScenarioMatch match : matches) {
            // scenario id and the pointer into the configuration travel with every candidate
            detections.add(SubjectDetection
                    .about(Detection.of(CTStandardSeverity.NONE, CODE_SCENARIO_MATCHED, DetectionLocation.of(resourceId),
                            "Scenario '" + match.getScenarioName() + "' matched"))
                    .identifiedBy(SubjectDetection.ATTR_SCENARIO_ID, match.getScenarioID()).locatedByXPath(match.getConfigurationLocation())
                    .inFile(match.getDefinitionFile()).build());
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
