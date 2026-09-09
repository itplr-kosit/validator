package org.kosit.validator.impl.conformatron;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.ConformanceValidation;
import org.kosit.validator.impl.EngineInformation;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction.ApplyRulesActionResult;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction.ComputeConformanceActionResult;
import org.kosit.validator.impl.conformatron.action.PrepareRulesAction.PrepareRulesResult;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction.SelectScenarioResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.kosit.validator.impl.conformatron.model.ConformanceTarget;
import org.kost.validator.api.saxon.ProcessorProvider;

import net.sf.saxon.s9api.Processor;

/**
 * <b>E2E runner</b>: walks the canonical pipeline (steps 2–9) over the real XRechnung testsuite instances against the
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
 * All inputs and outputs live in the repository's own {@code e2e/} folder (self-contained): scenarios, repository and
 * instances are read from {@code e2e/comparison/input/}, per-instance reports and CVRLs are written to
 * {@code e2e/comparison/v2_0/reports/}, the summary to {@code e2e/results/}. Every default can be overridden with the
 * system properties {@code e2e.scenarios}, {@code e2e.repository}, {@code e2e.instances}, {@code e2e.output},
 * {@code e2e.reports}.
 * </p>
 * <p>
 * Scenario {@code customLevel} overrides are applied by step 7 — detections carry the effective severity, overridden
 * ones additionally the declared one. Remaining gaps are listed in the report header.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class XRechnungE2ERunner {

    /** Result row of one instance run. */
    private record InstanceResult(String instance, String outcome, String decision, String scenario, int ruleSets, long infos,
            long warnings, long errors, List<String> conformance, List<CTDetection> findings, String failedStep, String hash,
            List<CTDetection> allDetections) {
    }

    /** The engine identity the comparison reports are written with. */
    private static final EngineInformation ENGINE = new EngineInformation() {

        @Override
        public String getName() {
            return "KoSIT XML Validator (canonical pipeline)";
        }

        @Override
        public String getVersion() {
            return "2.0.0-SNAPSHOT";
        }

        @Override
        public String getFrameworkVersion() {
            return "2.0.0";
        }

        @Override
        public String getBuild() {
            return "e2e";
        }
    };

    private final ConformanceValidation engine;

    private XRechnungE2ERunner(final VConfiguration configuration, final Processor processor) {
        this.engine = new ConformanceValidation(ENGINE, processor, configuration);
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

        final XRechnungE2ERunner runner = new XRechnungE2ERunner(configuration, processor);
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
                out.printf("| %s | `%s` | %s |%n", d.getSeverity().getId(), d.getCode(),
                        d.getText().getDisplayTextLocaleIndependent().replace("|", "\\|").replace("\n", " "));
            }
        }
    }

    /** Runs steps 2–9 for one instance; never throws — every outcome becomes a result row plus a (partial) CVR. */
    private InstanceResult run(final Path file, final Path instancesRoot, final Path reportsDir) {
        final String name = instancesRoot.relativize(file).toString().replace('\\', '/');
        final ConformanceValidationResult result;
        try {
            // the caller names the document: the corpus-relative path, not where it happens to sit on this machine
            result = this.engine.validate(ReadResource.inMemory(Resource.of(name, Files.readAllBytes(file))));
        } catch (final IOException | RuntimeException e) {
            return new InstanceResult(name, "RUNNER_ERROR: " + e.getClass().getSimpleName(), "-", "-", 0, 0, 0, 0, List.of(), List.of(),
                    e.getMessage(), "-", List.of());
        }
        if (reportsDir != null) {
            writeCvrl(reportsDir, name, result);
        }
        return toInstanceResult(name, result.getRun());
    }

    /** Serializes the (partial) run as CVRL draft report next to the Markdown report. */
    private static void writeCvrl(final Path reportsDir, final String name, final ConformanceValidationResult result) {
        try {
            final Path file = reportsDir.resolve(name.replace(".xml", "-cvrl.xml"));
            Files.createDirectories(file.getParent());
            final ByteArrayOutputStream cvrl = new ByteArrayOutputStream();
            result.writeCvr(cvrl);
            // the reports are kept in the repository, so their timestamps are fixed — see FixedTimestamps
            Files.write(file, FixedTimestamps.apply(cvrl.toByteArray()));
        } catch (final IOException e) {
            throw new IllegalStateException("Can not write CVRL for " + name, e);
        }
    }

    /** Derives the summary row from the (partial) pipeline results. */
    private static InstanceResult toInstanceResult(final String name, final PipelineResults r) {
        // step 9 always runs, so every row has a decision — a cancelled run is a rejection with the step in the
        // rationale
        final String decision = r.decision().decision().name();
        if (!r.isCompleted()) {
            return failed(name, r.cancelledAt().name(), decision, r.cancelDetections().getAll());
        }
        final ParseXmlResult parsed = r.parse();
        final SelectScenarioResult selected = r.select();
        final PrepareRulesResult prepared = r.prepare();
        final ApplyRulesActionResult applied = r.apply();
        final ComputeConformanceActionResult conformance = r.conformance();
        final String scenarioName = selected.selected().getScenarioName();

        // complete detection trace across all steps, in pipeline order (for the per-instance report)
        final List<CTDetection> trace = r.allDetections();

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
        return new InstanceResult(name, conformant ? "CONFORMANT" : "NON_CONFORMANT", decision, scenarioName, prepared.ruleSets().size(),
                infos, warnings, errors, statements, findings, null, hash, trace);
    }

    private static InstanceResult failed(final String name, final String step, final String decision, final List<CTDetection> detections) {
        final List<CTDetection> findings = detections.stream().filter(d -> d.getSeverity() != CTStandardSeverity.NONE).toList();
        return new InstanceResult(name, "FAILED@" + step, decision, "-", 0, 0, 0, findings.size(), List.of(), findings, step, "-",
                detections);
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
            out.println("# XRechnung E2E — kanonische Pipeline Steps 2–9");
            out.println();
            // kein Erzeugungszeitpunkt: die Datei liegt im Repository, und wann sie erzeugt wurde sagt der Commit
            out.println("Instanzen: " + total);
            out.println();
            out.println("**Bekannte Lücken dieses Laufs** (bei der Bewertung berücksichtigen):");
            out.println(
                    "- `customLevel`-Overrides werden von Step 7 angewandt (effektive Severity; Original als `cvrl:original-severity`).");
            out.println(
                    "- `acceptMatch` der Szenarien wird nicht ausgewertet (läuft auf dem Report; ADR-004 Follow-up) — die Entscheidung (Step 9) folgt allein aus den Konformitätsaussagen von Step 8.");
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
            out.println("| Instanz | Ergebnis | Entscheidung | Szenario | RuleSets | INFO | WARN | ERROR+ | Conformance je RuleSet |");
            out.println("|---|---|---|---|---|---|---|---|---|");
            for (final InstanceResult r : results) {
                out.printf("| %s | %s | %s | %s | %d | %d | %d | %d | %s |%n", r.instance(), r.outcome(), r.decision(), r.scenario(),
                        r.ruleSets(), r.infos(), r.warnings(), r.errors(), String.join("<br>", r.conformance()));
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
                    out.println("- `" + d.getSeverity().getId() + "` **" + d.getCode() + "** — "
                            + d.getText().getDisplayTextLocaleIndependent());
                }
            }
        }
    }
}
