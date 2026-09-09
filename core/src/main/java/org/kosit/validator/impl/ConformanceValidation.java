package org.kosit.validator.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.scenario.CTScenarioMatch;
import org.conformatron.api.model.source.CTReadResource;
import org.kosit.conformatron.detection.DetectionList;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.config.ConfigurationKeys;
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
import org.kosit.validator.impl.conformatron.model.ScenarioSeverityOverrides;
import org.kosit.validator.impl.conformatron.report.CvrlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;

/**
 * The <b>full conformance validation</b> mode of the {@link ValidationEngine}: the canonical pipeline, steps 2–9, along
 * the configured scenarios.
 *
 * <pre>
 * 2 PARSE_DOCUMENT → 3 DETECT_SCENARIOS → 4 SELECT_SCENARIO → 5 RETRIEVE_ARTIFACTS
 *                  → 6 PREPARE_RULES    → 7 APPLY_RULES     → 8 COMPUTE_CONFORMANCE → 9 DECISION_RECOMMENDATION
 * </pre>
 *
 * <p>
 * The engine is a pure composition of the canonical actions, like its ad-hoc counterpart {@link SchematronValidation} —
 * it holds no state of its own beyond its configuration, and every step is invoked with exactly the output of its
 * predecessor. What it adds over calling the actions by hand is the one thing a caller must not get wrong: <b>the
 * cancel semantics</b>. A step that does not succeed ends the run, the steps after it stay unexecuted, and the run is
 * still assembled into a {@link PipelineResults} — so a cancelled run yields a partial CVR with an explicit verdict
 * rather than an exception (ADR-004, step-09 spec).
 * </p>
 * <p>
 * Step 1 ({@code DETECT_SYNTAX}) is not implemented; the engine parses XML.
 * </p>
 * <p>
 * Configuration is a construction concern (ADR-008): which scenarios apply and where their artifacts live is fixed at
 * construction, and {@link #validate(CTReadResource)} takes nothing but the document. Several configurations may be
 * given; each brings its own artifact repository, so the engine keeps them apart and retrieves the artifacts of the
 * selected scenario from the repository of <b>its</b> configuration.
 * </p>
 *
 * @author Andreas Schmitz
 */
public class ConformanceValidation implements ValidationEngine<ConformanceValidationResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConformanceValidation.class);

    /**
     * One configuration with the actions that are bound to it: scenario detection knows the definition file it reports,
     * artifact retrieval knows the repository it is confined to.
     */
    private record ConfiguredScenarios(VConfiguration configuration, DetectScenariosAction detect, RetrieveArtifactsAction retrieve) {
    }

    private final List<ConfiguredScenarios> configurations;

    private final CvrlWriter writer;

    /**
     * Creates the engine over the given scenario configurations, resolving artifacts in the file system.
     *
     * @param engineInformation engine name and version for the report metadata
     * @param processor the Saxon processor
     * @param configuration the scenario configurations; at least one
     */
    public ConformanceValidation(final EngineInformation engineInformation, final Processor processor,
            final VConfiguration... configuration) {
        this(engineInformation, processor, false, configuration);
    }

    /**
     * Creates the engine over the given scenario configurations.
     *
     * @param engineInformation engine name and version for the report metadata
     * @param processor the Saxon processor
     * @param resolveInArchive {@code true} if the scenario repositories live inside an archive
     *            ({@code jar:file:/some.jar!/repository}); off by default, and an explicit decision of whoever
     *            assembles the engine, exactly as in {@link SchematronValidation}
     * @param configuration the scenario configurations; at least one
     */
    public ConformanceValidation(final EngineInformation engineInformation, final Processor processor, final boolean resolveInArchive,
            final VConfiguration... configuration) {
        if (engineInformation == null) {
            throw new IllegalArgumentException("engineInformation may not be null");
        }
        if (processor == null) {
            throw new IllegalArgumentException("processor may not be null");
        }
        if (configuration == null || configuration.length == 0) {
            throw new IllegalArgumentException("At least one configuration is required");
        }
        this.configurations = new ArrayList<>();
        for (final VConfiguration single : configuration) {
            final URI repository = single.getContentRepository().getRepository();
            final Object definitionFile = single.getAdditionalParameters().get(ConfigurationKeys.SCENARIOS_FILE);
            this.configurations.add(new ConfiguredScenarios(single,
                    new DetectScenariosAction(new ScenarioRepository(single), processor)
                            .withDefinitionFile(definitionFile == null ? null : definitionFile.toString()),
                    new RetrieveArtifactsAction(repository, resolveInArchive)));
        }
        this.writer = new CvrlWriter(engineInformation.getName(), engineInformation.getVersion());
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

        // step 3: DETECT_SCENARIOS — per configuration, so that the origin of a match stays known
        final Map<CTScenarioMatch, ConfiguredScenarios> origin = new IdentityHashMap<>();
        final DetectScenariosResult detected = detect(parsed, requestedScenarioId, origin);
        if (!detected.isSuccess()) {
            return new PipelineResults(parsed, detected, null, null, null, null, null);
        }

        // step 4: SELECT_SCENARIO — strict: exactly one candidate
        final SelectScenarioAction.SelectScenarioResult selected = new SelectScenarioAction().execute(detected.matches());
        if (!selected.isSuccess()) {
            return new PipelineResults(parsed, detected, selected, null, null, null, null);
        }
        final ConfiguredScenarios source = origin.get(selected.selected());
        if (source == null) {
            // step 4 must hand back one of the candidates step 3 produced, otherwise its artifacts have no home
            throw new IllegalStateException("The selected scenario did not come from any configured scenario detection");
        }

        // step 5: RETRIEVE_ARTIFACTS — from the repository of the configuration the scenario came from
        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = source.retrieve().execute(selected.selected());
        if (!retrieved.isSuccess()) {
            return new PipelineResults(parsed, detected, selected, retrieved, null, null, null);
        }

        // step 6: PREPARE_RULES — the compile cache of the ContentRepository keeps this cheap across documents
        final PrepareRulesAction.PrepareRulesResult prepared = new PrepareRulesAction(source.configuration().getContentRepository())
                .execute(retrieved.artifacts(), resourceId);
        if (!prepared.isSuccess()) {
            return new PipelineResults(parsed, detected, selected, retrieved, prepared, null, null);
        }

        // step 7: APPLY_RULES — with the customLevel severity overrides of the selected scenario
        final ApplyRulesAction.ApplyRulesActionResult applied = new ApplyRulesAction().execute(parsed.getParsedSource(),
                prepared.ruleSets(), ScenarioSeverityOverrides.of(selected.selected()));
        if (!applied.isSuccess()) {
            return new PipelineResults(parsed, detected, selected, retrieved, prepared, applied, null);
        }

        // step 8: COMPUTE_CONFORMANCE — scenario-wide default target (the legacy model declares none)
        final ComputeConformanceAction.ComputeConformanceActionResult conformance = new ComputeConformanceAction().execute(applied.result(),
                List.of(ConformanceTarget.ofScenario(selected.selected())));

        // step 9 runs inside PipelineResults: it always runs, so it cannot be forgotten here
        return new PipelineResults(parsed, detected, selected, retrieved, prepared, applied, conformance);
    }

    /**
     * Detects scenarios across all configurations and records which configuration each match came from.
     * <p>
     * With a single configuration this is exactly one {@code DETECT_SCENARIOS} execution. With several, the matches and
     * their detections are concatenated in configuration order; a configuration that matched nothing contributes no
     * detection, because "none of the configured scenarios matches" would be false as soon as another one did. Only
     * when no configuration matched at all does the run cancel — with the failure of the first configuration, which
     * already carries the canonical code and message.
     * </p>
     */
    private DetectScenariosResult detect(final ParseXmlResult parsed, final String requestedScenarioId,
            final Map<CTScenarioMatch, ConfiguredScenarios> origin) {
        DetectScenariosResult firstFailure = null;
        final List<CTScenarioMatch> matches = new ArrayList<>();
        final List<CTDetection> detections = new ArrayList<>();
        for (final ConfiguredScenarios candidate : this.configurations) {
            final DetectScenariosResult result = candidate.detect().execute(parsed.getParsedSource(), requestedScenarioId);
            if (!result.isSuccess()) {
                if (firstFailure == null) {
                    firstFailure = result;
                }
                continue;
            }
            result.matches().forEach(match -> origin.put(match, candidate));
            matches.addAll(result.matches());
            detections.addAll(result.detections().getAll());
        }
        if (matches.isEmpty()) {
            return firstFailure;
        }
        return new DetectScenariosResult(CTStepResult.SUCCESS, List.copyOf(matches), new DetectionList(detections));
    }
}
