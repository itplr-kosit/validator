package org.kosit.validator.impl.conformatron.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.kosit.base.xml.XmlHelper;
import org.kosit.validator.TestHelper;
import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.impl.ConformanceValidation;
import org.kosit.validator.impl.TestEngineInformation;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kosit.validator.impl.conformatron.FixedTimestamps;
import org.kosit.validator.testdata.TestResources;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * What the report names its subjects by — the same pipeline over the same document, once with a configuration that
 * declares ids and once with one that does not, kept as the reference examples
 * {@code e2e/examples/report-identity-with-ids.xml} and {@code e2e/examples/report-identity-without-ids.xml}. A diff of
 * the two shows exactly what declaring an id changes, and nothing else.
 * <p>
 * Three things are easy to get wrong here and invisible without this test. That an id reaches only some of the three
 * attributes, so a consumer joins a coordinate against a name. That a name or a location leaks into an attribute of a
 * run whose configuration declares ids, or the other way round. And that the id reaches a message text, which happened
 * to step 4 and only showed once a configuration actually carried ids: the text is for people and names the scenario
 * the way step 3 names it, the id belongs in the attribute.
 * </p>
 */
public class ReportIdentityExamplesTest {

    private static final String NS = CvrlWriter.NS_XVRL;

    private static final String NS_CVR = CvrlWriter.NS_CVR;

    /** The ids the configuration written for 2.0 declares. */
    private static final String SCENARIO_ID = "org.kosit.validator.test:simple:1.0.0";

    private static final String XSD_ID = "org.kosit.validator.test:simple-xsd:1.0.0";

    private static final String RULES_ID = "org.kosit.validator.test:simple-rules:1.0.0:compiled";

    /** The scenario a message text names, e.g. {@code Scenario 'Simple' selected}. */
    private static final Pattern QUOTED_SCENARIO = Pattern.compile("Scenario '([^']*)'");

    /**
     * Runs the whole pipeline and returns the report as a document. A name writes the report as a reference example; a
     * {@code null} name is a run the test only reads.
     */
    private Document run(final URI scenarios, final URI document, final String exampleName) throws Exception {
        final ScenarioSet configuration = ScenarioSet.load(scenarios, TestResources.Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
        // the shared test repository lives inside an archive, so this engine is allowed to resolve into one
        final ConformanceValidationResult result = new ConformanceValidation(new TestEngineInformation(), TestHelper.getTestProcessor(),
                true, configuration).validate(TestHelper.read(document));
        assertThat(result.isCompleted()).isTrue();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        result.writeCvr(out);
        final byte[] cvr = out.toByteArray();

        // CVRL is a profile of XVRL: a report that does not satisfy the profile is not a CVRL report
        CvrAssert.assertValidCvr(exampleName == null ? "report.xml" : exampleName, cvr);
        if (exampleName != null) {
            writeExample(exampleName, cvr);
        }
        return XmlHelper.createSafeDocumentBuilder().parse(new ByteArrayInputStream(cvr));
    }

    /** Writes the example next to the other e2e material; skipped when the folder is not part of the checkout. */
    private static void writeExample(final String name, final byte[] cvr) throws Exception {
        final Path moduleDir = Paths.get("").toAbsolutePath();
        final Path examples = (moduleDir.endsWith("core") ? moduleDir.getParent() : moduleDir).resolve("e2e/examples");
        if (!Files.isDirectory(examples.getParent())) {
            return;
        }
        Files.createDirectories(examples);
        // the examples are kept in the repository: fixed timestamps, and no href of the generating machine
        Files.write(examples.resolve(name), FixedTimestamps.withoutAbsoluteHrefs(FixedTimestamps.apply(cvr)));
    }

    private static Element report(final Document cvr, final String creator) {
        final NodeList reports = cvr.getElementsByTagNameNS(NS, "report");
        return IntStream.range(0, reports.getLength()).mapToObj(i -> (Element) reports.item(i)).filter(report -> {
            final Element creatorElement = (Element) report.getElementsByTagNameNS(NS, "creator").item(0);
            return creatorElement != null && creator.equals(creatorElement.getAttribute("name"));
        }).findFirst().orElse(null);
    }

    private static List<Element> detections(final Document cvr, final String creator) {
        final NodeList detections = report(cvr, creator).getElementsByTagNameNS(NS, "detection");
        return IntStream.range(0, detections.getLength()).mapToObj(i -> (Element) detections.item(i)).toList();
    }

    /** The values one identity attribute carries in one step, in report order. */
    private static List<String> identities(final Document cvr, final String creator, final String attribute) {
        return detections(cvr, creator).stream().map(detection -> detection.getAttributeNS(NS_CVR, attribute))
                .filter(value -> !value.isEmpty()).toList();
    }

    /** The {@code href} of every location of one step, so that the artifact stays fetchable whatever identifies it. */
    private static List<String> locations(final Document cvr, final String creator) {
        final NodeList locations = report(cvr, creator).getElementsByTagNameNS(NS, "location");
        return IntStream.range(0, locations.getLength()).mapToObj(i -> ((Element) locations.item(i)).getAttribute("href"))
                .filter(href -> !href.isEmpty()).toList();
    }

    /** The scenario the message texts of one step name, in report order. */
    private static List<String> namedScenarios(final Document cvr, final String creator) {
        return detections(cvr, creator).stream().map(detection -> {
            final Element message = (Element) detection.getElementsByTagNameNS(NS, "message").item(0);
            final Matcher matcher = QUOTED_SCENARIO.matcher(message == null ? "" : message.getTextContent());
            return matcher.find() ? matcher.group(1) : null;
        }).filter(name -> name != null).toList();
    }

    @Test
    public void testAConfigurationWithIdsIsIdentifiedByThem() throws Exception {
        final Document cvr = run(TestResources.Simple.SCENARIOS_WRITTEN_FOR_2_0, TestResources.Simple.SIMPLE_VALID,
                "report-identity-with-ids.xml");

        // steps 3 and 4 name the scenario, step 8 names the conformance target derived from it
        assertThat(identities(cvr, "detect-scenarios", "scenario-id")).containsExactly(SCENARIO_ID);
        assertThat(identities(cvr, "select-scenario", "scenario-id")).containsExactly(SCENARIO_ID);
        assertThat(identities(cvr, "compute-conformance", "target-id")).containsOnly(SCENARIO_ID);
        // steps 5 and 6 name the artifacts, in the order the scenario declares them
        assertThat(identities(cvr, "retrieve-artifacts", "artifact-id")).containsExactly(XSD_ID, RULES_ID);
        // step 6 reports what it compiles; the rule set of this configuration is a precompiled XSL and is passed
        // through, so the schema is the only artifact of its own report
        assertThat(identities(cvr, "prepare-rules", "artifact-id")).containsExactly(XSD_ID);
        // the location keeps pointing at the artifact, so a consumer knows what to fetch and what was hashed
        assertThat(locations(cvr, "retrieve-artifacts")).containsExactly("simple.xsd", "simple.xsl");
    }

    @Test
    public void testAConfigurationWithoutIdsIsIdentifiedByNameAndLocation() throws Exception {
        final Document cvr = run(TestResources.Simple.SCENARIOS_WITHOUT_IDS, TestResources.Simple.SIMPLE_VALID,
                "report-identity-without-ids.xml");

        // the fallback is what keeps the configurations of the standards and the ad hoc runs reporting as before
        assertThat(identities(cvr, "detect-scenarios", "scenario-id")).containsExactly("Simple");
        assertThat(identities(cvr, "select-scenario", "scenario-id")).containsExactly("Simple");
        assertThat(identities(cvr, "compute-conformance", "target-id")).containsOnly("Simple");
        assertThat(identities(cvr, "retrieve-artifacts", "artifact-id")).containsExactly("simple.xsd", "simple.xsl");
        assertThat(identities(cvr, "prepare-rules", "artifact-id")).containsExactly("simple.xsd");
        assertThat(locations(cvr, "retrieve-artifacts")).containsExactly("simple.xsd", "simple.xsl");
    }

    @Test
    public void testNeitherRunMixesTheTwoWaysOfIdentifying() throws Exception {
        final Document withIds = run(TestResources.Simple.SCENARIOS_WRITTEN_FOR_2_0, TestResources.Simple.SIMPLE_VALID, null);
        final Document withoutIds = run(TestResources.Simple.SCENARIOS_WITHOUT_IDS, TestResources.Simple.SIMPLE_VALID, null);

        // a run whose configuration declares ids identifies by coordinates only - no name, no location leaked in
        assertThat(allIdentities(withIds)).isNotEmpty().allSatisfy(value -> assertThat(value).matches(".+:.+:.+"));
        // and a run whose configuration declares none carries no coordinate anywhere
        assertThat(allIdentities(withoutIds)).isNotEmpty().allSatisfy(value -> assertThat(value).doesNotContain(":"));
    }

    private static List<String> allIdentities(final Document cvr) {
        return List.of("detect-scenarios", "select-scenario", "retrieve-artifacts", "prepare-rules", "compute-conformance").stream()
                .flatMap(creator -> List.of("scenario-id", "artifact-id", "target-id").stream()
                        .flatMap(attribute -> identities(cvr, creator, attribute).stream()))
                .toList();
    }

    @Test
    public void testTheMessagesNameTheScenarioTheSameWayInStepsThreeAndFour() throws Exception {
        // the id belongs in the attribute; a message text is read by people and names the scenario. Step 4 used to
        // build its text from the id, which matched step 3 only as long as no configuration declared one
        for (final URI scenarios : List.of(TestResources.Simple.SCENARIOS_WRITTEN_FOR_2_0, TestResources.Simple.SCENARIOS_WITHOUT_IDS)) {
            final Document cvr = run(scenarios, TestResources.Simple.SIMPLE_VALID, null);

            assertThat(namedScenarios(cvr, "detect-scenarios")).containsExactly("Simple");
            assertThat(namedScenarios(cvr, "select-scenario")).containsExactly("Simple");
        }
    }
}
