package org.kosit.validator.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.scenario.CTConformanceTarget;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.scenario.CTScenarioMatch;
import org.conformatron.api.model.source.CTReadResource;
import org.conformatron.api.model.validation.CTResolvedValidationArtifact;
import org.kosit.conformatron.detection.DetectionList;
import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kosit.validator.impl.conformatron.PipelineResults;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction;
import org.kosit.validator.impl.conformatron.action.PrepareRulesAction;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.kosit.validator.impl.conformatron.model.ConformanceTarget;
import org.kosit.validator.impl.conformatron.model.ScenarioMatch;
import org.kosit.validator.impl.conformatron.model.ScenarioSeverityOverrides;
import org.kosit.validator.impl.conformatron.report.CvrlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;

/**
 * The {@link ValidationEngine} of the validator: the canonical pipeline, steps 2–9, along the configured scenarios.
 *
 * <pre>
 * 2 PARSE_DOCUMENT → 3 DETECT_SCENARIOS → 4 SELECT_SCENARIO → 5 RETRIEVE_ARTIFACTS
 *                  → 6 PREPARE_RULES    → 7 APPLY_RULES     → 8 COMPUTE_CONFORMANCE → 9 DECISION_RECOMMENDATION
 * </pre>
 *
 * <p>
 * The engine is a pure composition of the canonical actions — it holds no state of its own beyond its scenarios, and
 * every step is invoked with exactly the output of its predecessor. What it adds over calling the actions by hand is
 * the one thing a caller must not get wrong: <b>the cancel semantics</b>. A step that does not succeed ends the run,
 * the steps after it stay unexecuted, and the run is still assembled into a {@link PipelineResults} — so a cancelled
 * run yields a partial CVR with an explicit verdict rather than an exception (ADR-004, step-09 spec).
 * </p>
 * <p>
 * Step 1 ({@code DETECT_SYNTAX}) is not implemented; the engine parses XML.
 * </p>
 * <p>
 * Configuration is a construction concern (ADR-008): the engine is built over a list of {@link Scenario scenarios}, and
 * {@link #validate(CTReadResource)} takes nothing but the document. Every scenario brings its own artifact repository;
 * the artifacts of the selected scenario are retrieved from and compiled in <b>its</b> repository, so scenarios from
 * several configurations can be run by one engine.
 * </p>
 *
 * @author Andreas Schmitz
 */
public class ConformanceValidation implements ValidationEngine<ConformanceValidationResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConformanceValidation.class);

    private final DetectScenariosAction detect;

    /**
     * Artifact retrieval per repository: the resolver is confined to one repository, so there is one per repository.
     */
    private final Map<URI, RetrieveArtifactsAction> retrieval = new HashMap<>();

    private final boolean resolveInArchive;

    private final CvrlWriter writer;

    /**
     * Creates the engine over the given scenarios, resolving artifacts in the file system.
     *
     * @param engineInformation engine name and version for the report metadata
     * @param saxonProcessor the Saxon processor the scenarios were compiled with
     * @param scenarios the scenarios; at least one
     */
    public ConformanceValidation(final EngineInformation engineInformation, final Processor saxonProcessor,
            final List<Scenario> scenarios) {
        this(engineInformation, saxonProcessor, false, scenarios);
    }

    /**
     * Creates the engine over the scenarios of the given configurations, resolving artifacts in the file system.
     *
     * @param engineInformation engine name and version for the report metadata
     * @param saxonProcessor the Saxon processor the scenarios were compiled with
     * @param configurations the scenario configurations; at least one
     */
    public ConformanceValidation(final EngineInformation engineInformation, final Processor saxonProcessor,
            final ScenarioSet... configurations) {
        this(engineInformation, saxonProcessor, false, configurations);
    }

    /**
     * Creates the engine over the scenarios of the given configurations.
     *
     * @param engineInformation engine name and version for the report metadata
     * @param saxonProcessor the Saxon processor the scenarios were compiled with
     * @param resolveInArchive {@code true} if the artifact repositories live inside an archive
     *            ({@code jar:file:/some.jar!/repository})
     * @param configurations the scenario configurations; at least one
     */
    public ConformanceValidation(final EngineInformation engineInformation, final Processor saxonProcessor, final boolean resolveInArchive,
            final ScenarioSet... configurations) {
        this(engineInformation, saxonProcessor, resolveInArchive, scenariosOf(configurations));
    }

    /**
     * Creates the engine over the given scenarios.
     *
     * @param engineInformation engine name and version for the report metadata
     * @param saxonProcessor the Saxon processor the scenarios were compiled with
     * @param resolveInArchive {@code true} if the artifact repositories live inside an archive
     *            ({@code jar:file:/some.jar!/repository}); off by default, and an explicit decision of whoever
     *            assembles the engine
     * @param scenarios the scenarios; at least one
     */
    public ConformanceValidation(final EngineInformation engineInformation, final Processor saxonProcessor, final boolean resolveInArchive,
            final List<Scenario> scenarios) {
        if (engineInformation == null) {
            throw new IllegalArgumentException("engineInformation may not be null");
        }
        if (saxonProcessor == null) {
            throw new IllegalArgumentException("processor may not be null");
        }
        if (scenarios == null || scenarios.isEmpty()) {
            throw new IllegalArgumentException("At least one scenario is required");
        }
        this.resolveInArchive = resolveInArchive;
        this.detect = new DetectScenariosAction(scenarios, saxonProcessor);
        for (final Scenario scenario : scenarios) {
            this.retrieval.computeIfAbsent(scenario.getRepository().getRepository(),
                    repository -> new RetrieveArtifactsAction(repository, resolveInArchive));
        }
        this.writer = new CvrlWriter(engineInformation.getName(), engineInformation.getVersion());
    }

    /**
     * The engine for an ad hoc validation — "run this Schematron against this document": one scenario assembled from
     * the rule set ({@link Scenario#adHoc(Processor, URI, boolean)}), applying unconditionally, with the directory of
     * the Schematron as its repository. The same pipeline and the same report as for a configured scenario.
     *
     * @param engineInformation engine name and version for the report metadata
     * @param saxonProcessor the Saxon processor
     * @param schematron URI of the Schematron ({@code .sch}, or a precompiled {@code .xsl})
     * @param resolveInArchive {@code true} if the Schematron lives inside an archive
     * @return the engine
     * @throws IllegalArgumentException if no repository can be derived from the Schematron URI
     */
    public static ConformanceValidation adHoc(final EngineInformation engineInformation, final Processor saxonProcessor,
            final URI schematron, final boolean resolveInArchive) {
        return adHoc(engineInformation, saxonProcessor, List.of(schematron), null, resolveInArchive);
    }

    /**
     * The engine for an ad hoc validation against a set of artifacts — XML Schemas, Schematrons, precompiled Schematron
     * XSLTs — as one scenario ({@link Scenario#adHoc(Processor, List, URI, boolean)}).
     *
     * @param engineInformation engine name and version for the report metadata
     * @param saxonProcessor the Saxon processor
     * @param artifacts URIs of the artifacts, applied in this order; the kind is read from the extension
     * @param repository the artifact repository, or {@code null} for the common parent directory of the artifacts
     * @param resolveInArchive {@code true} if the artifacts live inside an archive
     * @return the engine
     * @throws IllegalArgumentException if the artifacts do not make a scenario, see {@link Scenario#adHoc}
     */
    public static ConformanceValidation adHoc(final EngineInformation engineInformation, final Processor saxonProcessor,
            final List<URI> artifacts, final URI repository, final boolean resolveInArchive) {
        return new ConformanceValidation(engineInformation, saxonProcessor, resolveInArchive,
                List.of(Scenario.adHoc(saxonProcessor, artifacts, repository, resolveInArchive)));
    }

    private static List<Scenario> scenariosOf(final ScenarioSet... configurations) {
        if (configurations == null || configurations.length == 0) {
            throw new IllegalArgumentException("At least one configuration is required");
        }
        final List<Scenario> scenarios = new ArrayList<>();
        Arrays.stream(configurations).forEach(c -> scenarios.addAll(c.getScenarios()));
        return scenarios;
    }

    /**
     * Full conformance validation ({@link ValidationEngine} contract).
     *
     * @param input the document to validate
     * @return the run and its verdict; never {@code null}, also for a cancelled run
     */
    @Override
    public ConformanceValidationResult validate(final CTReadResource input) {
        return validate(input, null);
    }

    /**
     * Full conformance validation against a scenario the caller names instead of letting detection find it.
     *
     * @param input the document to validate
     * @param requestedScenarioId name of the scenario to use, or {@code null} to auto-detect
     * @return the run and its verdict; never {@code null}, also for a cancelled run
     */
    public ConformanceValidationResult validate(final CTReadResource input, final String requestedScenarioId) {
        if (input == null) {
            throw new IllegalArgumentException("input may not be null");
        }
        final long started = System.currentTimeMillis();
        LOGGER.info("Checking content of {}", input.getName());
        final PipelineResults run = run(input, requestedScenarioId);
        LOGGER.info("Finished check of {} in {}ms — {}", input.getName(), System.currentTimeMillis() - started, run.decision().decision());
        return new ConformanceValidationResult(input.getName(), run, this.writer);
    }

    /**
     * Runs the pipeline and hands back the raw step results. The public entry point is
     * {@link #validate(CTReadResource)}; this is for callers that work on the run itself — the report writer, the E2E
     * comparison.
     *
     * @param input the document to validate
     * @param requestedScenarioId name of the scenario to use, or {@code null} to auto-detect
     * @return the results of every step the run reached; fields from the cancellation point onwards are {@code null}
     */
    public PipelineResults run(final CTReadResource input, final String requestedScenarioId) {
        // step 2: PARSE_DOCUMENT
        final ParseXmlResult parsed = new ParseXmlAction().execute(input);
        if (!parsed.isSuccess()) {
            return new PipelineResults(parsed, null, null, null, null, null, null);
        }
        final String resourceId = parsed.getParsedSource().getSource().getName();

        // step 3: DETECT_SCENARIOS — over all scenarios; each candidate knows the scenario it stands for
        final DetectScenariosResult detected = this.detect.execute(parsed.getParsedSource(), requestedScenarioId);
        if (!detected.isSuccess()) {
            return new PipelineResults(parsed, detected, null, null, null, null, null);
        }

        // step 4: SELECT_SCENARIO — one scenario matched by expression, plus every scenario that applies
        // unconditionally
        final SelectScenarioAction.SelectScenarioResult selected = new SelectScenarioAction().execute(detected.matches());
        if (!selected.isSuccess()) {
            return new PipelineResults(parsed, detected, selected, null, null, null, null);
        }
        final List<ScenarioMatch> applied = selected.applied().stream().map(ConformanceValidation::matchOf).toList();

        // step 5: RETRIEVE_ARTIFACTS — every applied scenario from its own repository, the results as one step result
        final List<CTResolvedValidationArtifact> artifacts = new ArrayList<>();
        final List<CTDetection> retrieveDetections = new ArrayList<>();
        final Map<ScenarioMatch, List<CTResolvedValidationArtifact>> artifactsByScenario = new LinkedHashMap<>();
        boolean retrievedAll = true;
        for (final ScenarioMatch match : applied) {
            final RetrieveArtifactsAction.RetrieveArtifactsResult result = retrievalFor(match.getScenario()).execute(match);
            artifacts.addAll(result.artifacts());
            artifactsByScenario.put(match, result.artifacts());
            retrieveDetections.addAll(result.detections().getAll());
            retrievedAll &= result.isSuccess();
        }
        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction.RetrieveArtifactsResult(
                retrievedAll ? CTStepResult.SUCCESS : CTStepResult.FAILURE, List.copyOf(artifacts), new DetectionList(retrieveDetections));
        if (!retrieved.isSuccess()) {
            return new PipelineResults(parsed, detected, selected, retrieved, null, null, null);
        }

        // step 6: PREPARE_RULES — in the repository of the scenario the artifact belongs to; its compile cache keeps
        // this cheap across documents
        final List<CTPreparedRuleSet> ruleSets = new ArrayList<>();
        final List<CTDetection> prepareDetections = new ArrayList<>();
        boolean preparedAll = true;
        for (final Map.Entry<ScenarioMatch, List<CTResolvedValidationArtifact>> entry : artifactsByScenario.entrySet()) {
            final PrepareRulesAction.PrepareRulesResult result = new PrepareRulesAction(entry.getKey().getScenario().getRepository())
                    .execute(entry.getValue(), resourceId);
            ruleSets.addAll(result.ruleSets());
            prepareDetections.addAll(result.detections().getAll());
            preparedAll &= result.isSuccess();
        }
        final PrepareRulesAction.PrepareRulesResult prepared = new PrepareRulesAction.PrepareRulesResult(
                preparedAll ? CTStepResult.SUCCESS : CTStepResult.FAILURE, List.copyOf(ruleSets), new DetectionList(prepareDetections));
        if (!prepared.isSuccess()) {
            return new PipelineResults(parsed, detected, selected, retrieved, prepared, null, null);
        }

        // step 7: APPLY_RULES — all rule sets, with the customLevel severity overrides of every applied scenario
        final ApplyRulesAction.ApplyRulesActionResult appliedRules = new ApplyRulesAction().execute(parsed.getParsedSource(),
                prepared.ruleSets(), ScenarioSeverityOverrides.ofAll(applied));
        if (!appliedRules.isSuccess()) {
            return new PipelineResults(parsed, detected, selected, retrieved, prepared, appliedRules, null);
        }

        // step 8: COMPUTE_CONFORMANCE — one target per applied scenario, covering the rule sets of that scenario
        final List<CTConformanceTarget> targets = new ArrayList<>();
        applied.forEach(match -> targets.add(ConformanceTarget.ofScenario(match)));
        final ComputeConformanceAction.ComputeConformanceActionResult conformance = new ComputeConformanceAction()
                .execute(appliedRules.result(), targets);

        // step 9 runs inside PipelineResults: it always runs, so it cannot be forgotten here
        return new PipelineResults(parsed, detected, selected, retrieved, prepared, appliedRules, conformance);
    }

    private static ScenarioMatch matchOf(final CTScenarioMatch selected) {
        if (selected instanceof final ScenarioMatch match) {
            return match;
        }
        // step 4 must hand back candidates step 3 produced, otherwise their artifacts have no home
        throw new IllegalStateException("The selected scenario did not come from the scenario detection of this engine");
    }

    private RetrieveArtifactsAction retrievalFor(final Scenario scenario) {
        return this.retrieval.computeIfAbsent(scenario.getRepository().getRepository(),
                repository -> new RetrieveArtifactsAction(repository, this.resolveInArchive));
    }
}
