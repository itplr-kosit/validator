package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;
import org.kosit.schematron.resolve.RemoteResolvingStrategy;
import org.kosit.schematron.resolve.ResolvingMode;
import org.kosit.validator.TestHelper;
import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.conformatron.model.ScenarioSeverityOverrides;
import org.kosit.validator.testdata.TestResources;

/**
 * @author Andreas Penski
 */
public class ConfigurationLoaderTest {

    @Test
    public void testCustomResolvingStrategy() {
        final ConfigurationLoader loader = TestConfigurationFactory.loadSimpleConfiguration();
        loader.setResolvingStrategy(TestHelper.getTestResolvingStrategy());
        loader.setResolvingMode(ResolvingMode.ALLOW_REMOTE);
        final ScenarioSet config = loader.build(TestHelper.getTestProcessor());
        assertThat(config.getScenarios().get(0).getRepository().getResolvingConfigurationStrategy())
                .isNotInstanceOf(RemoteResolvingStrategy.class);
    }

    @Test
    public void testAScenarioWrittenFor20Loads() {
        final ScenarioSet config = ScenarioSet.load(TestResources.Simple.SCENARIOS_WRITTEN_FOR_2_0, TestResources.Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
        final Scenario simple = config.getScenarios().get(0);

        // an empty acceptMatch is a placeholder, not an XPath - loading did not choke on it, and the version
        // independent model does not distinguish an empty one from an absent one
        assertThat(simple.getDeclaration().hasAcceptMatch()).isFalse();
        assertThat(simple.isUnconditional()).isFalse();
        assertThat(simple.getDefinitionFile()).isEqualTo(TestResources.Simple.SCENARIOS_WRITTEN_FOR_2_0.toString());
        assertThat(config.getDefinitionFile()).isEqualTo(TestResources.Simple.SCENARIOS_WRITTEN_FOR_2_0.toString());
        // the processor is named at the rule set, the overrides are declared with it
        assertThat(simple.getDeclaration().getSchematrons().get(0).getCompiler()).isEqualTo("schxslt");
        // the ids of the scenario and of its artifacts are read; nothing but the report uses them yet
        assertThat(simple.getDeclaredId()).isEqualTo("org.kosit.validator.test:simple:1.0.0");
        assertThat(simple.getDeclaration().getXmlSchemas().get(0).getCoordinate().getAsSingleID())
                .isEqualTo("org.kosit.validator.test:simple-xsd:1.0.0");
        assertThat(simple.getDeclaration().getSchematrons().get(0).getResource().getCoordinate().getAsSingleID())
                .isEqualTo("org.kosit.validator.test:simple-rules:1.0.0:compiled");
        assertThat(ScenarioSeverityOverrides.fromDeclaration(simple.getDeclaration()).effectiveFor("rejected"))
                .isEqualTo(CTStandardSeverity.WARNING);
    }

    @Test
    public void testAScenarioWrittenInVersion3LoadsIntoTheSameModel() {
        final Scenario v2 = load(TestResources.Simple.SCENARIOS_WRITTEN_FOR_2_0).getScenarios().get(0);
        final Scenario v3 = load(TestResources.Simple.SCENARIOS_V3).getScenarios().get(0);

        // the two documents are the same scenario in two formats, so the engine has to see the same declaration
        assertThat(v3.getDeclaration()).hasToString(v2.getDeclaration().toString());
        assertThat(v3.getDeclaredId()).isEqualTo("org.kosit.validator.test:simple:1.0.0");
        assertThat(v3.isUnconditional()).isFalse();

        // the report embeds what the configuration wrote, so the two differ exactly there
        assertThat(new String(v3.getDeclarationXml(), java.nio.charset.StandardCharsets.UTF_8)).contains("urn:kosit:validator:scenario:3")
                .contains("<artifactId>simple</artifactId>");
        assertThat(new String(v2.getDeclarationXml(), java.nio.charset.StandardCharsets.UTF_8)).contains("framework/2/scenarios")
                .contains("org.kosit.validator.test:simple:1.0.0");
    }

    @Test
    public void testEveryResourceIsResolvedInTheRepositoryItNames() {
        final Scenario scenario = load(TestResources.Simple.SCENARIOS_V3_REPOSITORIES).getScenarios().get(0);

        // the two repositories lie beside each other, and neither holds the artifact of the other
        assertThat(scenario.getRepository("standard").getRepository().toString()).endsWith("/standard/");
        assertThat(scenario.getRepository("national").getRepository().toString()).endsWith("/national/");
        // a name the configuration does not declare is an error, not a silent fallback
        assertThatThrownBy(() -> scenario.getRepository("nowhere")).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not declare");
    }

    @Test
    public void testAPdfScenarioIsRefusedWhileLoading() {
        // version 3 can express a PDF scenario, this version validates XML documents. Loading it would not fail
        // anywhere else: without a match expression it counts as unconditional, so every document would find two
        // candidates and the run would be cancelled as ambiguous, which says nothing about the cause
        assertThatThrownBy(() -> load(TestResources.Simple.SCENARIOS_V3_PDF)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Simple as PDF").hasMessageContaining("validates a PDF document");
    }

    @Test
    public void testAPseudoVersionIsRefusedWhileLoading() {
        // "latest" names no version but a rule for choosing one, and this version chooses nothing by coordinate - the
        // report would carry "latest" as if it were the identity of the artifact
        assertThatThrownBy(() -> load(TestResources.Simple.SCENARIOS_V3_PSEUDO_VERSION)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("org.kosit.validator.test:simple-xsd:latest").hasMessageContaining("pseudo version 'latest'");
    }

    @Test
    public void testAConcreteVersionThatIsNoDvrVersionIsKeptAsWritten() {
        // 1.09.2 has a leading zero and so no DVR form, but it is a concrete version and identifies the artifact
        final Scenario scenario = load(TestResources.Simple.SCENARIOS_V3_NOT_DVR).getScenarios().get(0);

        assertThat(scenario.getDeclaration().getXmlSchemas().get(0).getCoordinate().getAsSingleID())
                .isEqualTo("org.kosit.validator.test:simple-xsd:1.09.2");
        assertThat(scenario.getDeclaration().getXmlSchemas().get(0).getCoordinate().hasCoordinate()).isFalse();
    }

    @Test
    public void testAValidFromDateIsRefusedWhileLoading() {
        // applying a configuration that says it is valid from 2027 on, today, would be a wrong verdict, not a missing
        // feature
        assertThatThrownBy(() -> load(TestResources.Simple.SCENARIOS_V3_VALID_FROM)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is valid from 2027-01-01");
    }

    @Test
    public void testOneCoordinateForTwoArtifactsIsRefused() {
        assertThatThrownBy(() -> load(TestResources.Simple.SCENARIOS_V3_ARTIFACT_CONFLICT)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("'org.kosit.validator.test:simple-xsd:1.0.0' is declared for both 'simple.xsd' and 'simple.xsl'");
    }

    @Test
    public void testOneCoordinateForTwoScenariosIsRefused() {
        assertThatThrownBy(() -> load(TestResources.Simple.SCENARIOS_V3_SCENARIO_CONFLICT)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("'Simple' and 'Simple again' both declare the coordinate 'org.kosit.validator.test:simple:1.0.0'");
    }

    @Test
    public void testOneArtifactSharedByTwoScenariosLoads() {
        // the same file under the same coordinate is one artifact used twice - that is what a coordinate is for
        final ScenarioSet set = load(TestResources.Simple.SCENARIOS_V3_SHARED_ARTIFACT);

        assertThat(set.getScenarios()).hasSize(2);
        assertThat(set.getScenarios().get(1).getDeclaration().getXmlSchemas().get(0).getCoordinate().getAsSingleID())
                .isEqualTo(set.getScenarios().get(0).getDeclaration().getXmlSchemas().get(0).getCoordinate().getAsSingleID());
    }

    private static ScenarioSet load(final URI scenarios) {
        return ScenarioSet.load(scenarios, TestResources.Simple.REPOSITORY_URI).setResolvingStrategy(TestHelper.getTestResolvingStrategy())
                .build(TestHelper.getTestProcessor());
    }
}
