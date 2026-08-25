package org.kosit.validator.impl.conformatron;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.conformatron.api.model.conformance.CTConformanceStatement;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.ScenarioRepository;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction.ApplyRulesActionResult;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction.ComputeConformanceActionResult;
import org.kosit.validator.impl.conformatron.action.PrepareRulesAction;
import org.kosit.validator.impl.conformatron.action.PrepareRulesAction.PrepareRulesResult;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction.RetrieveArtifactsResult;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction.SelectScenarioResult;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLResult;
import org.kosit.validator.impl.conformatron.model.ConformanceTarget;
import org.kosit.validator.impl.conformatron.report.CvrlWriter;
import org.kosit.validator.impl.xml.ProcessorProvider;

import net.sf.saxon.s9api.Processor;

/**
 * <b>E2E runner</b>: walks the canonical pipeline (steps 2–8) over the real XRechnung testsuite instances against the
 * real XRechnung validator configuration and writes human-readable Markdown reports for manual evaluation.
 * <p>
 * Not a JUnit test — run it via:
 * </p>
 *
 * <pre>
 * mvn -pl core test-compile org.codehaus.mojo:exec-maven-plugin:3.1.0:java \
 *     -Dexec.mainClass=org.kosit.validator.impl.conformatron.XRechnungE2ERunner \
 *     -Dexec.classpathScope=test
 * </pre>
 * <p>
 * All inputs and outputs live in the repository's own {@code e2e/} folder (self-contained since session 25.08.2026):
 * scenarios, repository and instances are read from {@code e2e/comparison/input/}, per-instance reports and CVRLs are
 * written to {@code e2e/comparison/v2_0/reports/}, the summary to {@code e2e/results/}. Every default can be overridden
 * with the system properties {@code e2e.scenarios}, {@code e2e.repository}, {@code e2e.instances}, {@code e2e.output},
 * {@code e2e.reports}.
 * </p>
 * <p>
 * <b>Known gap surfaced by this run</b> (list in the report header): scenario {@code customLevel} overrides are not yet
 * applied by step 7 — severities are reported as declared by the rules (open question in step-07 spec).
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class XRechnungE2ERunner {

    /** Result row of one instance run. */
    private record InstanceResult(String instance, String outcome, String scenario, int ruleSets, long infos, long warnings, long errors,
            List<String> conformance, List<CTDetection> findings, String failedStep, String hash, List<CTDetection> allDetections) {
    }

    private final ScenarioRepository scenarioRepository;

    private final VConfiguration configuration;

    private final Processor processor;

    private final URI repository;

    private XRechnungE2ERunner(final VConfiguration configuration, final Processor processor, final URI repository) {
        this.configuration = configuration;
        this.processor = processor;
        this.repository = repository;
        this.scenarioRepository = new ScenarioRepository(configuration);
    }

    public static void main(final String[] args) throws IOException {
        final Path moduleDir = Paths.get("").toAbsolutePath();
        // repo-local e2e folder: defaults work when started from the validator root or from the core module
        final Path root = moduleDir.endsWith("core") ? moduleDir.getParent() : moduleDir;
        final Path scenarios = Paths
                .get(System.getProperty("e2e.scenarios", root.resolve("e2e/comparison/input/scenarios-v2.0-framework2.xml").toString()));
        final Path repository = Paths.get(System.getProperty("e2e.repository", root.resolve("e2e/comparison/input/repository").toString()));
        final Path instances = Paths.get(System.getProperty("e2e.instances", root.resolve("e2e/comparison/input/instances").toString()));
        final Path output = Paths.get(System.getProperty("e2e.output", root.resolve("e2e/results").toString()));

        System.out.println("Scenarios : " + scenarios);
        System.out.println("Repository: " + repository);
        System.out.println("Instances : " + instances);
        System.out.println("Output    : " + output);

        final Processor processor = ProcessorProvider.getProcessor();
        final long t0 = System.currentTimeMillis();
        final VConfiguration configuration = VConfiguration.load(scenarios.toUri(), repository.toUri()).build(processor);
        System.out.println("Configuration loaded in " + (System.currentTimeMillis() - t0) + " ms (" + configuration.getScenarios().size()
                + " scenarios)");

        final XRechnungE2ERunner runner = new XRechnungE2ERunner(configuration, processor, repository.toUri());
        final List<Path> files;
        try ( Stream<Path> stream = Files.walk(instances) ) {
            files = stream.filter(p -> p.toString().endsWith(".xml")).filter(p -> !p.toString().contains(".idea")).sorted().toList();
        }
        final Path reports = Paths.get(System.getProperty("e2e.reports", root.resolve("e2e/comparison/v2_0/reports").toString()));
        System.out.println("Running " + files.size() + " instances ...");
        final List<InstanceResult> results = new ArrayList<>();
        for (final Path file : files) {
            final long ti = System.currentTimeMillis();
            final InstanceResult result = runner.run(file, instances, reports);
            results.add(result);
            if (reports != null) {
                writeInstanceReport(reports, result);
            }
            System.out.printf("%-70s %-22s %5d ms%n", result.instance(), result.outcome(), System.currentTimeMillis() - ti);
        }
        Files.createDirectories(output);
        writeSummary(output.resolve("xrechnung-e2e-summary.md"), results, files.size());
        writeDetails(output.resolve("xrechnung-e2e-details.md"), results);
        System.out.println("\nReports written to " + output + (reports != null ? " and " + reports : ""));
    }

    /** Writes the complete per-instance report (mirrors the instance directory structure). */
    private static void writeInstanceReport(final Path reportsDir, final InstanceResult result) throws IOException {
        final Path file = reportsDir.resolve(result.instance().replace(".xml", "-report.md"));
        Files.createDirectories(file.getParent());
        try ( PrintWriter out = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8)) ) {
            out.println("# " + result.instance());
            out.println();
            out.println("- **Ergebnis**: " + result.outcome());
            out.println("- **Szenario**: " + result.scenario());
            out.println("- **Dokument-Hash**: `" + result.hash() + "`");
            out.println("- **RuleSets**: " + result.ruleSets());
            if (!result.conformance().isEmpty()) {
                out.println("- **Conformance je RuleSet**:");
                result.conformance().forEach(s -> out.println("  - " + s));
            }
            out.println();
            out.println("## Detections (Steps 2–8, in Pipeline-Reihenfolge)");
            out.println();
            out.println("| Severity | Code | Meldung |");
            out.println("|---|---|---|");
            for (final CTDetection d : result.allDetections()) {
                out.printf("| %s | `%s` | %s |%n", d.getSeverity().getID(), d.getCode(),
                        d.getText().getDisplayTextLocaleIndependent().replace("|", "\\|").replace("\n", " "));
            }
        }
    }

    /** Runs steps 2–8 for one instance; never throws — every outcome becomes a result row plus a (partial) CVRL. */
    private InstanceResult run(final Path file, final Path instancesRoot, final Path reportsDir) {
        final String name = instancesRoot.relativize(file).toString().replace('\\', '/');
        final CvrlWriter.PipelineResults results;
        try {
            results = runSteps(file, name);
        } catch (final RuntimeException e) {
            return new InstanceResult(name, "RUNNER_ERROR: " + e.getClass().getSimpleName(), "-", 0, 0, 0, 0, List.of(), List.of(),
                    e.getMessage(), "-", List.of());
        }
        if (reportsDir != null) {
            writeCvrl(reportsDir, name, results);
        }
        return toInstanceResult(name, results);
    }

    /** Executes the pipeline; fields from the cancellation point onwards stay {@code null} (partial CVRL). */
    private CvrlWriter.PipelineResults runSteps(final Path file, final String name) {
        // step 2: PARSE_DOCUMENT
        final ParseXMLResult parsed = new ParseXMLAction().execute(TestHelper.read(file.toFile()));
        if (!parsed.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, null, null, null, null, null, null);
        }
        // step 3: DETECT_SCENARIOS (DOM wrapped into the Saxon model)
        final DetectScenariosResult detected = new DetectScenariosAction(this.scenarioRepository, this.processor)
                .execute(parsed.getParsedSource());
        if (!detected.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, detected, null, null, null, null, null);
        }
        // step 4: SELECT_SCENARIO
        final SelectScenarioResult selected = new SelectScenarioAction().execute(detected.matches());
        if (!selected.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, detected, selected, null, null, null, null);
        }
        // step 5: RETRIEVE_ARTIFACTS
        final RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(this.repository).execute(selected.selected());
        if (!retrieved.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, detected, selected, retrieved, null, null, null);
        }
        // step 6: PREPARE_RULES (compile cache inside the ContentRepository keeps this fast across instances)
        final PrepareRulesResult prepared = new PrepareRulesAction(this.configuration.getContentRepository()).execute(retrieved.artifacts(),
                name);
        if (!prepared.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, detected, selected, retrieved, prepared, null, null);
        }
        // step 7: APPLY_RULES
        final ApplyRulesActionResult applied = new ApplyRulesAction().execute(parsed.getParsedSource(), prepared.ruleSets());
        if (!applied.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, detected, selected, retrieved, prepared, applied, null);
        }
        // step 8: COMPUTE_CONFORMANCE (scenario-wide default target)
        final ComputeConformanceActionResult conformance = new ComputeConformanceAction().execute(applied.result(),
                List.of(ConformanceTarget.ofScenario(selected.selected())));
        return new CvrlWriter.PipelineResults(parsed, detected, selected, retrieved, prepared, applied, conformance);
    }

    /** Serializes the (partial) run as CVRL draft report next to the Markdown report. */
    private static void writeCvrl(final Path reportsDir, final String name, final CvrlWriter.PipelineResults results) {
        try {
            final Path file = reportsDir.resolve(name.replace(".xml", "-cvrl.xml"));
            Files.createDirectories(file.getParent());
            try ( var out = Files.newOutputStream(file) ) {
                new CvrlWriter("KoSIT XML Validator (canonical pipeline)", "2.0.0-SNAPSHOT").write(name, results, out);
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Can not write CVRL for " + name, e);
        }
    }

    /** Derives the summary row from the (partial) pipeline results. */
    private static InstanceResult toInstanceResult(final String name, final CvrlWriter.PipelineResults r) {
        if (!r.parse().isSuccess()) {
            return failed(name, "PARSE_DOCUMENT", r.parse().getDetectionList().getAll());
        }
        if (r.detect() != null && !r.detect().isSuccess()) {
            return failed(name, "DETECT_SCENARIOS", r.detect().detections().getAll());
        }
        if (r.select() != null && !r.select().isSuccess()) {
            return failed(name, "SELECT_SCENARIO", r.select().detections().getAll());
        }
        if (r.retrieve() != null && !r.retrieve().isSuccess()) {
            return failed(name, "RETRIEVE_ARTIFACTS", r.retrieve().detections().getAll());
        }
        if (r.prepare() != null && !r.prepare().isSuccess()) {
            return failed(name, "PREPARE_RULES", r.prepare().detections().getAll());
        }
        if (r.apply() != null && !r.apply().isSuccess()) {
            return failed(name, "APPLY_RULES", r.apply().detections().getAll());
        }
        final ParseXMLResult parsed = r.parse();
        final SelectScenarioResult selected = r.select();
        final PrepareRulesResult prepared = r.prepare();
        final ApplyRulesActionResult applied = r.apply();
        final ComputeConformanceActionResult conformance = r.conformance();
        final String scenarioName = selected.selected().getScenarioName();

        // complete detection trace across all steps, in pipeline order (for the per-instance report)
        final List<CTDetection> trace = new ArrayList<>();
        trace.addAll(parsed.getDetectionList().getAll());
        trace.addAll(r.detect().detections().getAll());
        trace.addAll(selected.detections().getAll());
        trace.addAll(r.retrieve().detections().getAll());
        trace.addAll(prepared.detections().getAll());
        trace.addAll(applied.detections().getAll());
        trace.addAll(conformance.detections().getAll());

        final String hash = parsed.getParsedSource().getSource().getReadResource().getHashAlgorithmName() + "="
                + HexFormat.of().formatHex(parsed.getParsedSource().getSource().getReadResource().getHashBytes());
        final List<CTDetection> all = applied.detections().getAll();
        final long infos = count(all, CTStandardSeverity.NONE);
        final long warnings = count(all, CTStandardSeverity.WARNING);
        final long errors = all.stream().filter(d -> d.getSeverity().isError()).count();
        final List<String> statements = new ArrayList<>();
        for (final Map.Entry<CTPreparedRuleSet, CTConformanceStatement> e : conformance.result().getStatementsByRuleSet().entrySet()) {
            statements.add(shortRef(e.getKey()) + " → " + e.getValue().getResult());
        }
        final boolean conformant = !conformance.result().hasNonConformantTarget();
        final List<CTDetection> findings = all.stream().filter(d -> d.getSeverity() != CTStandardSeverity.NONE).toList();
        return new InstanceResult(name, conformant ? "CONFORMANT" : "NON_CONFORMANT", scenarioName, prepared.ruleSets().size(), infos,
                warnings, errors, statements, findings, null, hash, trace);
    }

    private static InstanceResult failed(final String name, final String step, final List<CTDetection> detections) {
        final List<CTDetection> findings = detections.stream().filter(d -> d.getSeverity() != CTStandardSeverity.NONE).toList();
        return new InstanceResult(name, "FAILED@" + step, "-", 0, 0, 0, findings.size(), List.of(), findings, step, "-", detections);
    }

    private static long count(final List<CTDetection> detections, final CTStandardSeverity severity) {
        return detections.stream().filter(d -> d.getSeverity() == severity).count();
    }

    private static String shortRef(final CTPreparedRuleSet ruleSet) {
        final String href = ruleSet.getArtifactReference().getValidationArtifactReference().toString();
        return href.substring(href.lastIndexOf('/') + 1);
    }

    private static void writeSummary(final Path file, final List<InstanceResult> results, final int total) throws IOException {
        try ( PrintWriter out = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8)) ) {
            out.println("# XRechnung E2E — kanonische Pipeline Steps 2–8");
            out.println();
            out.println("Erzeugt: " + LocalDateTime.now() + " · Instanzen: " + total);
            out.println();
            out.println("**Bekannte Lücken dieses Laufs** (bei der Bewertung berücksichtigen):");
            out.println(
                    "- `customLevel`-Overrides der Szenarien werden von Step 7 noch nicht angewandt — Severities wie in den Regeln deklariert (offene Frage step-07).");
            out.println(
                    "- `acceptMatch` der Szenarien wird nicht ausgewertet (läuft auf dem Report; ADR-004 Follow-up) — Verdikt ist rein detection-basiert.");
            out.println("- Step 8 nutzt ein szenarioweites Default-Target (`ConformanceTarget.ofScenario`).");
            out.println();
            final Map<String, Long> byOutcome = new LinkedHashMap<>();
            results.forEach(r -> byOutcome.merge(r.outcome(), 1L, Long::sum));
            out.println("## Ergebnisverteilung");
            out.println();
            byOutcome.forEach((k, v) -> out.println("- **" + k + "**: " + v));
            out.println();
            out.println("## Übersicht");
            out.println();
            out.println("| Instanz | Ergebnis | Szenario | RuleSets | INFO | WARN | ERROR+ | Conformance je RuleSet |");
            out.println("|---|---|---|---|---|---|---|---|");
            for (final InstanceResult r : results) {
                out.printf("| %s | %s | %s | %d | %d | %d | %d | %s |%n", r.instance(), r.outcome(), r.scenario(), r.ruleSets(), r.infos(),
                        r.warnings(), r.errors(), String.join("<br>", r.conformance()));
            }
        }
    }

    private static void writeDetails(final Path file, final List<InstanceResult> results) throws IOException {
        try ( PrintWriter out = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8)) ) {
            out.println("# XRechnung E2E — Findings je Instanz (alle Nicht-INFO-Detections)");
            for (final InstanceResult r : results) {
                if (r.findings().isEmpty() && r.failedStep() == null) {
                    continue;
                }
                out.println();
                out.println("## " + r.instance() + " — " + r.outcome());
                if (r.failedStep() != null && r.findings().isEmpty()) {
                    out.println();
                    out.println("Runner/Step-Fehler: " + r.failedStep());
                }
                for (final CTDetection d : r.findings()) {
                    out.println("- `" + d.getSeverity().getID() + "` **" + d.getCode() + "** — "
                            + d.getText().getDisplayTextLocaleIndependent());
                }
            }
        }
    }
}
