package org.kosit.validator.impl.conformatron.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.validation.CTValidationArtifactReference;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.ScenarioRepository;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction;
import org.kosit.validator.impl.conformatron.action.DecisionRecommendationAction;
import org.kosit.validator.impl.conformatron.action.PrepareRulesAction;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.XmlDetection;
import org.kosit.validator.impl.conformatron.model.ConformanceTarget;
import org.kosit.validator.impl.conformatron.model.ValidationArtifactReference;
import org.kosit.validator.impl.conformatron.model.SeverityOverrides;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * One test per way the pipeline can be cancelled, checking what the <b>report</b> looks like — the step specs define a
 * failure path for every step and require a partial CVRL to be emitted, so each of those paths needs a report that a
 * consumer can act on. The action-level tests already cover that the steps detect their failures; what is easy to get
 * wrong, and invisible without these tests, is the report: a cancelled run that looks conformant, a failing step whose
 * digest says {@code valid="true"}, a document echoed back after it failed to parse.
 * <p>
 * Every test asserts the same contract via {@link #assertCancelledAt}, so a new failure path only has to say where it
 * cancels and with which code. The reports are also written to {@code e2e/examples/} for review.
 * </p>
 */
public class CvrlUnhappyPathTest {

    private static final String NS = CvrlWriter.NS_XVRL;

    private static final String NS_CVRL = CvrlWriter.NS_CVRL;

    private final CvrlWriter writer = new CvrlWriter("KoSIT XML Validator (canonical pipeline)", "2.0.0-SNAPSHOT");

    /**
     * Runs the canonical pipeline until it cancels, exactly as the E2E runner does, and serializes whatever was
     * reached. Nothing here short-circuits on failure beyond what the pipeline itself does — that is the point.
     */
    private CvrlWriter.PipelineResults run(final URI scenarios, final URI document, final String requestedScenarioId) {
        final VConfiguration configuration = VConfiguration.load(scenarios, Simple.REPOSITORY_URI).build(TestHelper.getTestProcessor());
        final ParseXmlResult parsed = new ParseXmlAction().execute(TestHelper.read(document));
        if (!parsed.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, null, null, null, null, null, null);
        }
        final DetectScenariosResult detected = new DetectScenariosAction(new ScenarioRepository(configuration),
                TestHelper.getTestProcessor()).withDefinitionFile(scenarios.toString()).execute(parsed.getParsedSource(),
                        requestedScenarioId);
        if (!detected.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, detected, null, null, null, null, null);
        }
        final SelectScenarioAction.SelectScenarioResult selected = new SelectScenarioAction().execute(detected.matches());
        if (!selected.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, detected, selected, null, null, null, null);
        }
        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(Simple.REPOSITORY_URI)
                .execute(selected.selected());
        if (!retrieved.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, detected, selected, retrieved, null, null, null);
        }
        final PrepareRulesAction.PrepareRulesResult prepared = new PrepareRulesAction(configuration.getContentRepository())
                .execute(retrieved.artifacts(), "test");
        if (!prepared.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, detected, selected, retrieved, prepared, null, null);
        }
        final ApplyRulesAction.ApplyRulesActionResult applied = new ApplyRulesAction().execute(parsed.getParsedSource(),
                prepared.ruleSets(), SeverityOverrides.of(selected.selected()));
        if (!applied.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, detected, selected, retrieved, prepared, applied, null);
        }
        final ComputeConformanceAction.ComputeConformanceActionResult conformance = new ComputeConformanceAction().execute(applied.result(),
                List.of(ConformanceTarget.ofScenario(selected.selected())));
        return new CvrlWriter.PipelineResults(parsed, detected, selected, retrieved, prepared, applied, conformance);
    }

    /**
     * Runs steps 2–4 against a working configuration and then feeds step 5 the given artifact references directly.
     * <p>
     * This deliberately bypasses {@code VConfiguration.load}: the legacy {@code ConfigurationLoader} resolves and
     * compiles every scenario resource eagerly at load time and throws {@link IllegalStateException} when that fails
     * ({@code ContentRepository}). A configuration with a missing or non-compiling rule set therefore never reaches
     * step 5 or 6 at all — their spec'd failure paths are unreachable through the normal entry point, and no partial
     * CVRL is produced for them. Until artifact resolution and rule preparation belong to the pipeline alone, the only
     * way to exercise those reports is to call the steps directly.
     * </p>
     */
    private CvrlWriter.PipelineResults runWithReferences(final URI document, final String... references) {
        final VConfiguration configuration = VConfiguration.load(Simple.SCENARIOS_WITH_SCH, Simple.REPOSITORY_URI)
                .build(TestHelper.getTestProcessor());
        final ParseXmlResult parsed = new ParseXmlAction().execute(TestHelper.read(document));
        assertThat(parsed.isSuccess()).isTrue();
        final DetectScenariosResult detected = new DetectScenariosAction(new ScenarioRepository(configuration),
                TestHelper.getTestProcessor()).withDefinitionFile(Simple.SCENARIOS_WITH_SCH.toString()).execute(parsed.getParsedSource());
        assertThat(detected.isSuccess()).isTrue();
        final SelectScenarioAction.SelectScenarioResult selected = new SelectScenarioAction().execute(detected.matches());
        assertThat(selected.isSuccess()).isTrue();

        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(Simple.REPOSITORY_URI).execute(
                Arrays.stream(references).map(ValidationArtifactReference::of).map(r -> (CTValidationArtifactReference) r).toList(),
                document.getPath());
        if (!retrieved.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, detected, selected, retrieved, null, null, null);
        }
        final PrepareRulesAction.PrepareRulesResult prepared = new PrepareRulesAction(configuration.getContentRepository())
                .execute(retrieved.artifacts(), "test");
        if (!prepared.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, detected, selected, retrieved, prepared, null, null);
        }
        final ApplyRulesAction.ApplyRulesActionResult applied = new ApplyRulesAction().execute(parsed.getParsedSource(),
                prepared.ruleSets(), SeverityOverrides.of(selected.selected()));
        return new CvrlWriter.PipelineResults(parsed, detected, selected, retrieved, prepared, applied, null);
    }

    private Document serialize(final CvrlWriter.PipelineResults results, final String documentName, final String exampleName)
            throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        this.writer.write(documentName, results, out);
        writeExample(exampleName, out.toByteArray());

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
    }

    private Document serialize(final URI scenarios, final URI document, final String requestedScenarioId, final String exampleName)
            throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        this.writer.write(document.getPath().substring(document.getPath().lastIndexOf('/') + 1),
                run(scenarios, document, requestedScenarioId), out);
        writeExample(exampleName, out.toByteArray());

        // CVRL is a profile of XVRL: a report that does not validate against it is not a CVRL report
        CvrlSchema.assertValid(out.toByteArray());

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        // a broken report would already fail here
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
    }

    /** Writes the example next to the other e2e material; skipped when that folder is not part of the checkout. */
    private static void writeExample(final String name, final byte[] cvrl) throws Exception {
        final Path moduleDir = Paths.get("").toAbsolutePath();
        final Path examples = (moduleDir.endsWith("core") ? moduleDir.getParent() : moduleDir).resolve("e2e/examples/unhappy");
        if (!Files.isDirectory(examples.getParent().getParent())) {
            return;
        }
        Files.createDirectories(examples);
        Files.write(examples.resolve(name), cvrl);
    }

    private static List<Element> reports(final Document cvrl) {
        final NodeList nodes = cvrl.getElementsByTagNameNS(NS, "report");
        return IntStream.range(0, nodes.getLength()).mapToObj(i -> (Element) nodes.item(i)).toList();
    }

    private static String creator(final Element report) {
        return ((Element) report.getElementsByTagNameNS(NS, "creator").item(0)).getAttribute("name");
    }

    private static Element digest(final Element report) {
        return (Element) report.getElementsByTagNameNS(NS, "digest").item(0);
    }

    /**
     * The contract every cancelled run has to satisfy, per the step specs: the run says it was cancelled and is not
     * conformant, the failing step is the last thing in the report (nothing after a cancellation), its digest reports
     * the error rather than claiming validity, and the detection carries the code the spec names for that path. Steps
     * before the failure stay in the report and stay valid — that is what makes a partial CVRL diagnostic.
     *
     * @param cvrl the serialized report
     * @param failingStep the step expected to cancel
     * @param expectedCode the detection code the spec names for this path
     */
    private static void assertCancelledAt(final Document cvrl, final CTActionType failingStep, final String expectedCode) {
        final Element root = cvrl.getDocumentElement();
        assertThat(root.getAttributeNS(NS_CVRL, "status")).as("run status").isEqualTo("CANCELLED");
        assertThat(root.getAttributeNS(NS_CVRL, "conformant")).as("a cancelled run must never look conformant").isEqualTo("false");

        final List<Element> reports = reports(cvrl);
        assertThat(reports).as("the report must not be empty").hasSizeGreaterThanOrEqualTo(2);
        // step 9 always runs, so the last report is the decision — and a cancelled run is always rejected
        final Element decision = reports.get(reports.size() - 1);
        assertThat(creator(decision)).as("the decision is the last report").isEqualTo(CTActionType.DECISION_RECOMMENDATION.getName());
        assertThat(digest(decision).getAttribute("error-codes")).contains(DecisionRecommendationAction.CODE_REJECT);
        final Element verdict = (Element) decision.getElementsByTagNameNS(NS, "detection").item(0);
        assertThat(verdict.getAttributeNS(NS_CVRL, "decision")).isEqualTo("REJECT");
        assertThat(verdict.getElementsByTagNameNS(NS, "message").item(0).getTextContent()).as("the rationale names the cancelling step")
                .contains(failingStep.getName());

        // the failing step is the last one that did real work — nothing else runs after a cancellation
        final Element last = reports.get(reports.size() - 2);
        assertThat(creator(last)).as("the failing step is the last step before the decision").isEqualTo(failingStep.getName());

        assertThat(digest(last).getAttribute("valid")).as("the failing step's digest").isEqualTo("false");
        assertThat(Integer.parseInt(digest(last).getAttribute("error-count"))).as("errors counted").isPositive();
        assertThat(digest(last).getAttribute("error-codes")).as("error codes listed for triage").contains(expectedCode);

        final NodeList detections = last.getElementsByTagNameNS(NS, "detection");
        assertThat(IntStream.range(0, detections.getLength()).mapToObj(i -> ((Element) detections.item(i)).getAttribute("code")))
                .as("the spec's code for this path").contains(expectedCode);

        // every step before the failure ran cleanly and stays in the report
        for (final Element earlier : reports.subList(0, reports.size() - 2)) {
            assertThat(digest(earlier).getAttribute("valid")).as("earlier step " + creator(earlier)).isEqualTo("true");
        }
    }

    @Test
    public void step2NotWellformed() throws Exception {
        final Document cvrl = serialize(Simple.SCENARIOS_WITH_SCH, Simple.NOT_WELLFORMED, null, "step2-not-wellformed.xml");

        assertCancelledAt(cvrl, CTActionType.PARSE_DOCUMENT, XmlDetection.CODE_NOT_WELLFORMED);
        // security: content that failed to parse is never echoed back into the report
        final NodeList messages = cvrl.getElementsByTagNameNS(NS, "message");
        for (int i = 0; i < messages.getLength(); i++) {
            assertThat(((Element) messages.item(i)).getAttributeNS(NS_CVRL, "mime-type")).isEmpty();
        }
    }

    @Test
    public void step3NoScenarioMatches() throws Exception {
        final Document cvrl = serialize(Simple.SCENARIOS_WITH_SCH, Simple.UNKNOWN, null, "step3-no-scenario-matched.xml");

        assertCancelledAt(cvrl, CTActionType.DETECT_SCENARIOS, DetectScenariosAction.CODE_NO_SCENARIO_MATCHED);
        // without a scenario there is nothing to identify or locate
        final Element detection = (Element) reports(cvrl).get(1).getElementsByTagNameNS(NS, "detection").item(0);
        assertThat(detection.hasAttributeNS(NS_CVRL, "scenario-id")).isFalse();
        assertThat(detection.getElementsByTagNameNS(NS, "location").getLength()).isZero();
    }

    @Test
    public void step3RequestedScenarioIsUnknown() throws Exception {
        final Document cvrl = serialize(Simple.SCENARIOS_WITH_SCH, Simple.SIMPLE_VALID, "no-such-scenario",
                "step3-scenario-unknown-id.xml");

        assertCancelledAt(cvrl, CTActionType.DETECT_SCENARIOS, DetectScenariosAction.CODE_SCENARIO_UNKNOWN_ID);
        // the requested id belongs in the message so the caller sees what was asked for
        final Element failing = reports(cvrl).get(reports(cvrl).size() - 2);
        assertThat(failing.getElementsByTagNameNS(NS, "message").item(0).getTextContent()).contains("no-such-scenario");
    }

    @Test
    public void step4ScenarioIsAmbiguous() throws Exception {
        final Document cvrl = serialize(Simple.SCENARIOS_AMBIGUOUS, Simple.SIMPLE_VALID, null, "step4-scenario-ambiguous.xml");

        assertCancelledAt(cvrl, CTActionType.SELECT_SCENARIO, SelectScenarioAction.CODE_SCENARIO_AMBIGUOUS);
        // detection succeeded and reported both candidates — the ambiguity is a selection problem, not a detection one
        assertThat(reports(cvrl).get(1).getElementsByTagNameNS(NS, "detection").getLength()).isEqualTo(2);
    }

    @Test
    public void step5ArtifactIsMissing() throws Exception {
        final Document cvrl = serialize(runWithReferences(Simple.SIMPLE_VALID, "simple.xsd", "does-not-exist.sch"), "simple.xml",
                "step5-artifact-missing.xml");

        assertCancelledAt(cvrl, CTActionType.RETRIEVE_ARTIFACTS, RetrieveArtifactsAction.CODE_ARTIFACT_MISSING);
        // knowing *which* artifact is missing is the whole point, so it is named and located on the detection
        final Element failing = lastDetectionWithCode(cvrl, RetrieveArtifactsAction.CODE_ARTIFACT_MISSING);
        assertThat(failing.getAttributeNS(NS_CVRL, "artifact-id")).contains("does-not-exist.sch");
        final Element location = (Element) failing.getElementsByTagNameNS(NS, "location").item(0);
        assertThat(location.getAttribute("href")).contains("does-not-exist.sch");
    }

    @Test
    public void step6RulesDoNotCompile() throws Exception {
        final Document cvrl = serialize(runWithReferences(Simple.SIMPLE_VALID, "does-not-compile.sch"), "simple.xml",
                "step6-rule-prepare-error.xml");

        assertCancelledAt(cvrl, CTActionType.PREPARE_RULES, PrepareRulesAction.CODE_RULE_PREPARE_ERROR);
        // the artifact was retrieved fine — the failure is in preparing it, and the report separates the two
        assertThat(creator(reports(cvrl).get(3))).isEqualTo(CTActionType.RETRIEVE_ARTIFACTS.getName());
        assertThat(digest(reports(cvrl).get(3)).getAttribute("valid")).isEqualTo("true");
    }

    @Test
    public void step7RuleEngineFails() throws Exception {
        final Document cvrl = serialize(Simple.SCENARIOS_ENGINE_ERROR, Simple.SIMPLE_VALID, null, "step7-rule-engine-error.xml");

        assertCancelledAt(cvrl, CTActionType.APPLY_RULES, ApplyRulesAction.CODE_RULE_ENGINE_ERROR);
        // an engine error is not a finding: it cancels, and conformance is never computed
        assertThat(reports(cvrl)).extracting(CvrlUnhappyPathTest::creator).doesNotContain(CTActionType.COMPUTE_CONFORMANCE.getName());
    }

    private static Element lastDetectionWithCode(final Document cvrl, final String code) {
        final NodeList detections = cvrl.getElementsByTagNameNS(NS, "detection");
        Element ret = null;
        for (int i = 0; i < detections.getLength(); i++) {
            final Element detection = (Element) detections.item(i);
            if (code.equals(detection.getAttribute("code"))) {
                ret = detection;
            }
        }
        assertThat(ret).as("a detection with code " + code).isNotNull();
        return ret;
    }
}
