package org.kosit.validator.impl.conformatron.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.IntStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.conformatron.api.model.action.CTActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.ScenarioRepository;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
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
        this.configuration = VConfiguration.load(Simple.SCENARIOS_WITH_SCH, Simple.REPOSITORY_URI).build(TestHelper.getTestProcessor());
        this.scenarioRepository = new ScenarioRepository(this.configuration);
    }

    private CvrlWriter.PipelineResults runPipeline(final URI document) {
        final ParseXmlResult parsed = new ParseXmlAction().execute(TestHelper.read(document));
        if (!parsed.isSuccess()) {
            return new CvrlWriter.PipelineResults(parsed, null, null, null, null, null, null);
        }
        final DetectScenariosResult detected = new DetectScenariosAction(this.scenarioRepository, TestHelper.getTestProcessor())
                .execute(parsed.getParsedSource());
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
        return IntStream.range(0, nodes.getLength()).mapToObj(i -> (Element) nodes.item(i)).toList();
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
        // document by reference only — no checksum attributes in the root metadata
        final Element document = (Element) root.getElementsByTagNameNS(NS, "document").item(0);
        assertThat(document.getAttribute("href")).isEqualTo("test-document.xml");
        assertThat(document.getAttributeNS(NS_CVRL, "checksum")).isEmpty();
        // document-parsed carries two messages — hash first, then the embedded payload
        final Element parseReport = reports(cvrl).get(0);
        final NodeList messages = parseReport.getElementsByTagNameNS(NS, "message");
        assertThat(messages.getLength()).isEqualTo(2);
        final Element hashMessage = (Element) messages.item(0);
        assertThat(hashMessage.getAttributeNS(NS_CVRL, "algorithm")).isEqualTo("SHA-512");
        assertThat(hashMessage.getTextContent()).matches("[0-9a-f]{128}");
        final Element payloadMessage = (Element) messages.item(1);
        assertThat(payloadMessage.getAttributeNS(NS_CVRL, "mime-type")).isEqualTo("application/xml");
        // the parsed document is embedded as element content, not as escaped text
        assertThat(payloadMessage.getElementsByTagName("*").getLength()).isGreaterThan(0);
        // 6 steps + APPLY_RULES twice (xsd + schematron rule set) = 8 reports
        assertThat(reports(cvrl)).extracting(CvrlWriterTest::creator).containsExactly(CTActionType.PARSE_DOCUMENT.getName(),
                CTActionType.DETECT_SCENARIOS.getName(), CTActionType.SELECT_SCENARIO.getName(), CTActionType.RETRIEVE_ARTIFACTS.getName(),
                CTActionType.PREPARE_RULES.getName(), CTActionType.APPLY_RULES.getName(), CTActionType.APPLY_RULES.getName(),
                CTActionType.COMPUTE_CONFORMANCE.getName());
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
        // only the executed step is reported
        assertThat(reports(cvrl)).extracting(CvrlWriterTest::creator).containsExactly("parse-document");
        // failed content is never echoed into the report (injection safety)
        final NodeList messages = cvrl.getElementsByTagNameNS(NS, "message");
        for (int i = 0; i < messages.getLength(); i++) {
            assertThat(((Element) messages.item(i)).getAttributeNS(NS_CVRL, "mime-type")).isEmpty();
        }
        final Element digest = (Element) cvrl.getElementsByTagNameNS(NS, "digest").item(0);
        assertThat(digest.getAttribute("valid")).isEqualTo("false");
        assertThat(digest.getAttribute("worst-severity")).isEqualTo("error");
    }
}
