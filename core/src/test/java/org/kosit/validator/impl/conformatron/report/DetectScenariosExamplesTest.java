package org.kosit.validator.impl.conformatron.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.ScenarioRepository;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The three shapes a {@code detect-scenarios} report can take — no match, exactly one match, several matches — as
 * asserted behaviour and, at the same time, as the reference examples under {@code e2e/examples/}. Keeping the examples
 * generated instead of hand-written keeps them from drifting away from the writer.
 */
public class DetectScenariosExamplesTest {

    private static final String NS = CvrlWriter.NS_XVRL;

    private static final String NS_CVRL = CvrlWriter.NS_CVRL;

    private final CvrlWriter writer = new CvrlWriter("KoSIT XML Validator (canonical pipeline)", "2.0.0-SNAPSHOT");

    /**
     * Runs steps 2–4 only: the scenario reports are complete after step 4, and stopping there keeps the examples
     * readable.
     */
    private Document serialize(final URI scenarios, final URI document, final String exampleName) throws Exception {
        final VConfiguration configuration = VConfiguration.load(scenarios, Simple.REPOSITORY_URI).build(TestHelper.getTestProcessor());
        final ParseXmlResult parsed = new ParseXmlAction().execute(TestHelper.read(document));
        assertThat(parsed.isSuccess()).isTrue();

        final DetectScenariosResult detected = new DetectScenariosAction(new ScenarioRepository(configuration),
                TestHelper.getTestProcessor()).execute(parsed.getParsedSource());
        final SelectScenarioAction.SelectScenarioResult selected = detected.isSuccess()
                ? new SelectScenarioAction().execute(detected.matches())
                : null;

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        this.writer.write(document.getPath().substring(document.getPath().lastIndexOf('/') + 1),
                new CvrlWriter.PipelineResults(parsed, detected, selected, null, null, null, null), out);
        writeExample(exampleName, out.toByteArray());

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
    }

    /** Writes the example next to the other e2e material; skipped when the folder is not part of the checkout. */
    private static void writeExample(final String name, final byte[] cvrl) throws Exception {
        final Path moduleDir = Paths.get("").toAbsolutePath();
        final Path examples = (moduleDir.endsWith("core") ? moduleDir.getParent() : moduleDir).resolve("e2e/examples");
        if (!Files.isDirectory(examples.getParent())) {
            return;
        }
        Files.createDirectories(examples);
        Files.write(examples.resolve(name), cvrl);
    }

    private static Element report(final Document cvrl, final String creator) {
        final NodeList reports = cvrl.getElementsByTagNameNS(NS, "report");
        for (int i = 0; i < reports.getLength(); i++) {
            final Element report = (Element) reports.item(i);
            final Element creatorElement = (Element) report.getElementsByTagNameNS(NS, "creator").item(0);
            if (creator.equals(creatorElement.getAttribute("name"))) {
                return report;
            }
        }
        return null;
    }

    private static NodeList detections(final Element report) {
        return report.getElementsByTagNameNS(NS, "detection");
    }

    @Test
    public void testNoScenarioMatches() throws Exception {
        final Document cvrl = serialize(Simple.SCENARIOS_WITH_SCH, Simple.UNKNOWN, "detect-scenarios-no-match.xml");

        // no match cancels the process — the run is reported as such, and the step reports an error
        assertThat(cvrl.getDocumentElement().getAttributeNS(NS_CVRL, "status")).isEqualTo("CANCELLED");
        final Element detect = report(cvrl, "detect-scenarios");
        final Element digest = (Element) detect.getElementsByTagNameNS(NS, "digest").item(0);
        assertThat(digest.getAttribute("valid")).isEqualTo("false");
        assertThat(digest.getAttribute("error-count")).isEqualTo("1");
        assertThat(detections(detect).getLength()).isEqualTo(1);
        final Element detection = (Element) detections(detect).item(0);
        assertThat(detection.getAttribute("code")).isEqualTo(DetectScenariosAction.CODE_NO_SCENARIO_MATCHED);
        // an error keeps its severity even though scenario detection otherwise omits it
        assertThat(detection.getAttribute("severity")).isEqualTo("error");
        // no scenario, hence no scenario id and no location
        assertThat(detection.hasAttributeNS(NS_CVRL, "scenario-id")).isFalse();
        // select-scenario never ran
        assertThat(report(cvrl, "select-scenario")).isNull();
    }

    @Test
    public void testExactlyOneScenarioMatches() throws Exception {
        final Document cvrl = serialize(Simple.SCENARIOS_WITH_SCH, Simple.SIMPLE_VALID, "detect-scenarios-single-match.xml");

        final Element detect = report(cvrl, "detect-scenarios");
        assertThat(detections(detect).getLength()).isEqualTo(1);
        final Element detection = (Element) detections(detect).item(0);
        assertThat(detection.getAttributeNS(NS_CVRL, "scenario-id")).isEqualTo("Simple");
        assertThat(detection.getElementsByTagNameNS(NS, "location").getLength()).isEqualTo(1);
        // selection is a pass-through and embeds the selected scenario
        final Element select = report(cvrl, "select-scenario");
        assertThat(detections(select).getLength()).isEqualTo(1);
        assertThat(select.getElementsByTagNameNS(NS, "message").getLength()).isEqualTo(2);
    }

    @Test
    public void testSeveralScenariosMatch() throws Exception {
        final Document cvrl = serialize(Simple.SCENARIOS_AMBIGUOUS, Simple.SIMPLE_VALID, "detect-scenarios-multiple-matches.xml");

        // detection succeeds with one detection per candidate ...
        final Element detect = report(cvrl, "detect-scenarios");
        assertThat((Element) detect.getElementsByTagNameNS(NS, "digest").item(0)).extracting(d -> d.getAttribute("valid"))
                .isEqualTo("true");
        assertThat(detections(detect).getLength()).isEqualTo(2);
        assertThat(((Element) detections(detect).item(0)).getAttributeNS(NS_CVRL, "scenario-id")).isEqualTo("Simple");
        assertThat(((Element) detections(detect).item(1)).getAttributeNS(NS_CVRL, "scenario-id")).isEqualTo("Simple (second opinion)");
        // ... and selection is where the ambiguity becomes a reportable failure
        final Element select = report(cvrl, "select-scenario");
        final Element detection = (Element) detections(select).item(0);
        assertThat(detection.getAttribute("code")).isEqualTo(SelectScenarioAction.CODE_SCENARIO_AMBIGUOUS);
        assertThat(detection.getAttribute("severity")).isEqualTo("error");
        assertThat(cvrl.getDocumentElement().getAttributeNS(NS_CVRL, "status")).isEqualTo("CANCELLED");
    }
}
