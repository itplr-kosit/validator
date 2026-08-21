package org.kosit.validator.impl.conformatron.report;

import org.kosit.validator.impl.conformatron.action.PrepareRulesAction;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLResult;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.model.ConformanceTarget;
import org.kosit.validator.impl.conformatron.action.DetectScenariosAction;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.api.VInputFactory.read;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.ScenarioRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Tests the {@link CvrlWriter} draft format: a completed run serializes with one report per step (and per rule set for
 * APPLY_RULES), a cancelled run still serializes as partial CVRL (ADR-004 constraint 2).
 */
public class CvrlWriterTest {

    private static final String NS = CvrlWriter.NS_XVRL;

    private static final String NS_CVRL = CvrlWriter.NS_CVRL;

    private ScenarioRepository scenarioRepository;

    private VConfiguration configuration;

    private final CvrlWriter writer = new CvrlWriter("KoSIT XML Validator (canonical pipeline)", "2.0.0-SNAPSHOT");

    @BeforeEach
    public void setup() {
        this.configuration = VConfiguration.load(Simple.SCENARIOS_WITH_SCH, Simple.REPOSITORY_URI).build(Helper.getTestProcessor());
        this.scenarioRepository = new ScenarioRepository(this.configuration);
    }

    private CvrlWriter.PipelineResults runPipeline(final URI document) {
        final ParseXMLResult parsed = new ParseXMLAction().execute(read(document));
        if (!parsed.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, null, null, null, null, null, null);
        }
        final DetectScenariosAction.DetectScenariosResult detected = new DetectScenariosAction(this.scenarioRepository,
                Helper.getTestProcessor()).execute(parsed.getParsedSource());
        final SelectScenarioAction.SelectScenarioResult selected = new SelectScenarioAction().execute(detected.matches());
        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(Simple.REPOSITORY_URI)
                .execute(selected.selected());
        final PrepareRulesAction.PrepareRulesResult prepared = new PrepareRulesAction(this.configuration.getContentRepository())
                .execute(retrieved.artifacts(), "test");
        final ApplyRulesAction.ApplyRulesActionResult applied = new ApplyRulesAction().execute(parsed.getParsedSource(),
                prepared.ruleSets());
        final ComputeConformanceAction.ComputeConformanceActionResult conformance = new ComputeConformanceAction().execute(applied.result(),
                List.of(ConformanceTarget.ofScenario(selected.selected())));
        return new CvrlWriter.PipelineResults(parsed, detected, selected, retrieved, prepared, applied, conformance);
    }

    private Document serialize(final URI document) throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        this.writer.write("test-document.xml", runPipeline(document), out);
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        // well-formedness is the first assertion: parsing fails on broken output
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
    }

    private static List<Element> reports(final Document cvrl) {
        final NodeList nodes = cvrl.getElementsByTagNameNS(NS, "report");
        return java.util.stream.IntStream.range(0, nodes.getLength()).mapToObj(i -> (Element) nodes.item(i)).toList();
    }

    private static String creator(final Element report) {
        return ((Element) report.getElementsByTagNameNS(NS, "creator").item(0)).getAttribute("name");
    }

    @Test
    public void testCompletedRunSerializesOneReportPerStepExecution() throws Exception {
        final Document cvrl = serialize(Simple.SIMPLE_VALID);
        final Element root = cvrl.getDocumentElement();

        assertThat(root.getLocalName()).isEqualTo("reports");
        assertThat(root.getAttributeNS(NS_CVRL, "conformant")).isEqualTo("true");
        assertThat(root.getAttributeNS(NS_CVRL, "status")).isEqualTo("COMPLETED");
        // document identity: reference + checksum with algorithm name
        final Element document = (Element) root.getElementsByTagNameNS(NS, "document").item(0);
        assertThat(document.getAttributeNS(NS_CVRL, "checksum")).isNotEmpty();
        assertThat(document.getAttributeNS(NS_CVRL, "checksum-algorithm")).isEqualTo("SHA-512");
        // 6 steps + APPLY_RULES twice (xsd + schematron rule set) = 8 reports
        assertThat(reports(cvrl)).extracting(CvrlWriterTest::creator).containsExactly("parse-document", "scenario-matcher",
                "scenario-selector", "artifact-retriever", "rule-transpiler", "apply-validation-rules", "apply-validation-rules",
                "conformance-computer");
        // the APPLY_RULES reports carry the rule set identity
        final Element schematronReport = reports(cvrl).get(6);
        final Element schema = (Element) schematronReport.getElementsByTagNameNS(NS, "schema").item(0);
        assertThat(schema.getAttribute("href")).isEqualTo("simple.sch");
        assertThat(schema.getAttribute("language")).isEqualTo("Schematron");
        assertThat(schema.getAttributeNS(NS_CVRL, "phase")).isEqualTo("#ALL");
    }

    @Test
    public void testFindingsAppearWithDigestAndCodes() throws Exception {
        final Document cvrl = serialize(Simple.SCHEMATRON_INVALID);

        assertThat(cvrl.getDocumentElement().getAttributeNS(NS_CVRL, "conformant")).isEqualTo("false");
        final Element schematronReport = reports(cvrl).get(6);
        final Element digest = (Element) schematronReport.getElementsByTagNameNS(NS, "digest").item(0);
        assertThat(digest.getAttribute("valid")).isEqualTo("false");
        assertThat(digest.getAttribute("error-count")).isEqualTo("1");
        assertThat(digest.getAttribute("error-codes")).isEqualTo("content-1");
    }

    @Test
    public void testCancelledRunStillSerializesAsPartialCvrl() throws Exception {
        final Document cvrl = serialize(Simple.NOT_WELLFORMED);
        final Element root = cvrl.getDocumentElement();

        assertThat(root.getAttributeNS(NS_CVRL, "status")).isEqualTo("CANCELLED");
        assertThat(root.getAttributeNS(NS_CVRL, "conformant")).isEqualTo("false");
        // only the executed step is reported — but document identity survives the failed parse
        assertThat(reports(cvrl)).extracting(CvrlWriterTest::creator).containsExactly("parse-document");
        final Element document = (Element) root.getElementsByTagNameNS(NS, "document").item(0);
        assertThat(document.getAttributeNS(NS_CVRL, "checksum")).isNotEmpty();
        final Element digest = (Element) cvrl.getElementsByTagNameNS(NS, "digest").item(0);
        assertThat(digest.getAttribute("valid")).isEqualTo("false");
        assertThat(digest.getAttribute("worst-severity")).isEqualTo("fatal-error");
    }
}
