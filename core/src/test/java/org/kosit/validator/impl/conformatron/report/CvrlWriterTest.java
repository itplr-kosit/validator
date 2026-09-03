package org.kosit.validator.impl.conformatron.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
<<<<<<< Upstream, based on origin/2.x
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
=======
>>>>>>> 1d6eabe Trying to resolve in JAR resources as well
import java.util.List;
import java.util.stream.IntStream;

import javax.xml.XMLConstants;

import org.conformatron.api.model.action.CTActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.base.xml.XmlHelper;
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
import org.kosit.validator.impl.conformatron.util.ScenarioXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(CvrlWriterTest.class);

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

<<<<<<< Upstream, based on origin/2.x
    private Document serialize(final URI document) throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        this.writer.write("test-document.xml", runPipeline(document), out);
        // CVRL is a profile of XVRL: a report that does not validate against it is not a CVRL report
        CvrlSchema.assertValid(out.toByteArray());

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        // well-formedness is the first assertion: parsing fails on broken output
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
=======
    private Document runPipelineAndSerializeAsXml(final URI document) throws Exception {
        try ( final ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
            this.writer.write("test-document.xml", runPipeline(document), out);
            // well-formedness is the first assertion: parsing fails on broken output
            return XmlHelper.createSafeDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
        }
>>>>>>> 1d6eabe Trying to resolve in JAR resources as well
    }

    private static List<Element> getAllReportElements(final Document cvrl) {
        final NodeList nodes = cvrl.getElementsByTagNameNS(NS, "report");
        return IntStream.range(0, nodes.getLength()).mapToObj(i -> (Element) nodes.item(i)).toList();
    }

    private static String getCreator(final Element report) {
        return ((Element) report.getElementsByTagNameNS(NS, "creator").item(0)).getAttribute("name");
    }

    @Test
    public void testTimestampAlwaysCarriesSeconds() {
        // the bug this guards against only appears when the seconds happen to be zero, so it needs a fixed moment:
        // OffsetDateTime.toString() drops them, and xs:dateTime rejects the result
        final OffsetDateTime onTheMinute = OffsetDateTime.of(2026, 8, 31, 15, 48, 0, 0, ZoneOffset.ofHours(2));

        assertThat(CvrlWriter.timestamp(onTheMinute)).isEqualTo("2026-08-31T15:48:00+02:00");
        assertThat(CvrlWriter.timestamp(onTheMinute.withSecond(7))).isEqualTo("2026-08-31T15:48:07+02:00");
    }

    @Test
    public void testCompletedRunSerializesOneReportPerStepExecution() throws Exception {
        final Document cvrl = runPipelineAndSerializeAsXml(Simple.SIMPLE_VALID);
        final Element root = cvrl.getDocumentElement();

        assertThat(root.getLocalName()).isEqualTo("reports");
        assertThat(root.getAttributeNS(NS_CVRL, "conformant")).isEqualTo("true");
        assertThat(root.getAttributeNS(NS_CVRL, "status")).isEqualTo("COMPLETED");

        // document by reference only — no checksum attributes in the root metadata
        final Element document = (Element) root.getElementsByTagNameNS(NS, "document").item(0);
        assertThat(document.getAttribute("href")).isEqualTo("test-document.xml");
        assertThat(document.getAttributeNS(NS_CVRL, "checksum")).isEmpty();
<<<<<<< Upstream, based on origin/2.x
        // the document hash is context of the detection, not one of its messages
        final Element parseReport = reports(cvrl).get(0);
        final Element hash = (Element) parseReport.getElementsByTagNameNS(NS_CVRL, "hash").item(0);
        assertThat(hash.getAttributeNS(NS_CVRL, "algorithm")).isEqualTo("SHA-512");
        assertThat(hash.getTextContent()).matches("[0-9a-f]{128}");
        assertThat(parseReport.getElementsByTagNameNS(NS, "context").getLength()).isEqualTo(1);
        // exactly one message is left: the document itself
=======

        // document-parsed carries two messages — hash first, then the embedded payload
        final Element parseReport = getAllReportElements(cvrl).get(0);
>>>>>>> 1d6eabe Trying to resolve in JAR resources as well
        final NodeList messages = parseReport.getElementsByTagNameNS(NS, "message");
<<<<<<< Upstream, based on origin/2.x
        assertThat(messages.getLength()).isEqualTo(1);
        final Element payloadMessage = (Element) messages.item(0);
=======
        assertThat(messages.getLength()).isEqualTo(2);
        final Element hashMessage = (Element) messages.item(0);

        // messages are identified by xml:id so consumers never depend on their order
        assertThat(hashMessage.getAttributeNS(XMLConstants.XML_NS_URI, "id")).isEqualTo(CvrlWriter.ID_DOCUMENT_HASH);
        assertThat(hashMessage.getAttributeNS(NS_CVRL, "algorithm")).isEqualTo("SHA-512");
        assertThat(hashMessage.getTextContent()).matches("[0-9a-f]{128}");
        final Element payloadMessage = (Element) messages.item(1);
>>>>>>> 1d6eabe Trying to resolve in JAR resources as well
        assertThat(payloadMessage.getAttributeNS(XMLConstants.XML_NS_URI, "id")).isEqualTo(CvrlWriter.ID_DOCUMENT_CONTENT);
        assertThat(payloadMessage.getAttributeNS(NS_CVRL, "mime-type")).isEqualTo("application/xml");
<<<<<<< Upstream, based on origin/2.x
        // UTF-8 XML goes in as a DOM fragment, and then the source encoding says nothing worth writing
=======

        // UTF-8 XML is embedded as a DOM fragment, and the declared encoding is always reported
>>>>>>> 1d6eabe Trying to resolve in JAR resources as well
        assertThat(payloadMessage.getAttributeNS(NS_CVRL, "encoding")).isEqualTo(CvrlWriter.ENCODING_DOM);
<<<<<<< Upstream, based on origin/2.x
        assertThat(payloadMessage.hasAttributeNS(NS_CVRL, "source-encoding")).isFalse();
=======
        assertThat(payloadMessage.getAttributeNS(NS_CVRL, "source-encoding")).isEqualTo("UTF-8");

>>>>>>> 1d6eabe Trying to resolve in JAR resources as well
        // the parsed document is embedded as element content, not as escaped text
        assertThat(payloadMessage.getElementsByTagName("*").getLength()).isGreaterThan(0);
<<<<<<< Upstream, based on origin/2.x
        // a statement that the step ran carries neither a code nor a severity — both would suggest a finding
=======

        // no code that merely restates the creator name
>>>>>>> 1d6eabe Trying to resolve in JAR resources as well
        final Element parseDetection = (Element) parseReport.getElementsByTagNameNS(NS, "detection").item(0);
        assertThat(parseDetection.hasAttribute("code")).isFalse();
<<<<<<< Upstream, based on origin/2.x
        assertThat(parseDetection.hasAttribute("severity")).isFalse();
=======

>>>>>>> 1d6eabe Trying to resolve in JAR resources as well
        // 6 steps + APPLY_RULES twice (xsd + schematron rule set) = 8 reports
        assertThat(getAllReportElements(cvrl)).extracting(CvrlWriterTest::getCreator).containsExactly(CTActionType.PARSE_DOCUMENT.getName(),
                CTActionType.DETECT_SCENARIOS.getName(), CTActionType.SELECT_SCENARIO.getName(), CTActionType.RETRIEVE_ARTIFACTS.getName(),
                CTActionType.PREPARE_RULES.getName(), CTActionType.APPLY_RULES.getName(), CTActionType.APPLY_RULES.getName(),
                CTActionType.COMPUTE_CONFORMANCE.getName());

        // the APPLY_RULES reports carry the rule set identity
        final Element schematronReport = getAllReportElements(cvrl).get(6);
        final Element schema = (Element) schematronReport.getElementsByTagNameNS(NS, "schema").item(0);
        assertThat(schema.getAttribute("href")).isEqualTo("simple.sch");
        // XVRL has no "language" attribute — the rule language is stated by its namespace, and it is required
        assertThat(schema.getAttribute("schematypens")).isEqualTo(CvrlWriter.SCHEMATYPENS_SCHEMATRON);
        assertThat(schema.getAttributeNS(NS_CVRL, "phase")).isEqualTo("#ALL");
    }

    @Test
    public void testNonUtf8SourceIsEmbeddedAsBase64() throws Exception {
        final Document cvrl = runPipelineAndSerializeAsXml(Simple.SIMPLE_LATIN1);

<<<<<<< Upstream, based on origin/2.x
        final NodeList messages = reports(cvrl).get(0).getElementsByTagNameNS(NS, "message");
        final Element payloadMessage = (Element) messages.item(0);
=======
        final NodeList messages = getAllReportElements(cvrl).get(0).getElementsByTagNameNS(NS, "message");
        final Element payloadMessage = (Element) messages.item(1);
>>>>>>> 1d6eabe Trying to resolve in JAR resources as well
        // transcoding into the UTF-8 report would lose the original bytes, so the source travels base64
        assertThat(payloadMessage.getAttributeNS(NS_CVRL, "encoding")).isEqualTo(CvrlWriter.ENCODING_BASE64);
        assertThat(payloadMessage.getAttributeNS(NS_CVRL, "source-encoding")).isEqualTo("ISO-8859-1");
        assertThat(payloadMessage.getElementsByTagName("*").getLength()).isZero();
        final byte[] decoded = Base64.getMimeDecoder().decode(payloadMessage.getTextContent());
        assertThat(new String(decoded, StandardCharsets.ISO_8859_1)).contains("encoding=\"ISO-8859-1\"").contains("Maßnahme");
    }

    @Test
    public void testScenarioDetectionsCarryIdAndLocation() throws Exception {
        final Document cvrl = runPipelineAndSerializeAsXml(Simple.SIMPLE_VALID);

        final Element detectReport = getAllReportElements(cvrl).get(1);
        final Element detection = (Element) detectReport.getElementsByTagNameNS(NS, "detection").item(0);
        assertThat(detection.getAttributeNS(NS_CVRL, "scenario-id")).isEqualTo("Simple");
        // severity is omitted for scenario detection, the code would only restate the creator
        assertThat(detection.hasAttribute("severity")).isFalse();
        assertThat(detection.hasAttribute("code")).isFalse();
        // the location points into the scenario configuration so the scenario can be looked up
        final Element location = (Element) detection.getElementsByTagNameNS(NS, "location").item(0);
        assertThat(location.getAttribute("xpath")).isEqualTo("/*:scenarios/*:scenario[*:name='Simple']");
    }

    @Test
    public void testSelectedScenarioIsEmbeddedInFull() throws Exception {
        final Document cvrl = runPipelineAndSerializeAsXml(Simple.SIMPLE_VALID);

        final Element selectReport = getAllReportElements(cvrl).get(2);
        final NodeList messages = selectReport.getElementsByTagNameNS(NS, "message");
        assertThat(messages.getLength()).isEqualTo(2);
        final Element scenarioMessage = (Element) messages.item(1);
        assertThat(scenarioMessage.getAttributeNS(XMLConstants.XML_NS_URI, "id")).isEqualTo(CvrlWriter.ID_SCENARIO_CONTENT);

        // scenario configurations are UTF-8 by definition, so the scenario is always embedded as a DOM fragment
        assertThat(scenarioMessage.getAttributeNS(NS_CVRL, "encoding")).isEqualTo(CvrlWriter.ENCODING_DOM);
        final Element scenario = (Element) scenarioMessage.getElementsByTagName("*").item(0);
        assertThat(scenario.getLocalName()).isEqualTo("scenario");
        assertThat(scenario.getElementsByTagNameNS(ScenarioXml.NS_SCENARIOS, "name").item(0).getTextContent()).isEqualTo("Simple");
    }

    @Test
    public void testFindingsAppearWithDigestAndCodes() throws Exception {
        final Document cvrl = runPipelineAndSerializeAsXml(Simple.SCHEMATRON_INVALID);

        LOGGER.info("XML: " + XmlHelper.getXmlAsString(cvrl));

        assertThat(cvrl.getDocumentElement().getAttributeNS(NS_CVRL, "conformant")).isEqualTo("false");
        final Element schematronReport = getAllReportElements(cvrl).get(6);
        final Element digest = (Element) schematronReport.getElementsByTagNameNS(NS, "digest").item(0);
        assertThat(digest.getAttribute("valid")).isEqualTo("false");
        assertThat(digest.getAttribute("error-count")).isEqualTo("1");
        assertThat(digest.getAttribute("error-codes")).isEqualTo("content-1");
    }

    @Test
    public void testCancelledRunStillSerializesAsPartialCvrl() throws Exception {
        final Document cvrl = runPipelineAndSerializeAsXml(Simple.NOT_WELLFORMED);
        final Element root = cvrl.getDocumentElement();

        assertThat(root.getAttributeNS(NS_CVRL, "status")).isEqualTo("CANCELLED");
        assertThat(root.getAttributeNS(NS_CVRL, "conformant")).isEqualTo("false");
        // only the executed step is reported
        assertThat(getAllReportElements(cvrl)).extracting(CvrlWriterTest::getCreator).containsExactly("parse-document");
        // failed content is never echoed into the report (injection safety)
        final NodeList messages = cvrl.getElementsByTagNameNS(NS, "message");
        for (int i = 0; i < messages.getLength(); i++) {
            assertThat(((Element) messages.item(i)).getAttributeNS(NS_CVRL, "mime-type")).isEmpty();
        }
        final Element digest = (Element) cvrl.getElementsByTagNameNS(NS, "digest").item(0);
        assertThat(digest.getAttribute("valid")).isEqualTo("false");
        assertThat(digest.getAttribute("error-count")).isEqualTo("1");
        // one detection already states its severity, so the digest does not repeat it
        assertThat(digest.hasAttribute("worst-severity")).isFalse();
    }
}
