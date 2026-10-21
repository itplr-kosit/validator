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
    public void testAVersion3ConfigurationThisVersionCanNotResolveIsRefused() {
        // several repositories, a resource outside all of them, or one without a location: readable, not resolvable
        assertThatThrownBy(() -> load(TestResources.Simple.SCENARIOS_V3_REPOSITORIES)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("repositories");
    }

    private static ScenarioSet load(final URI scenarios) {
        return ScenarioSet.load(scenarios, TestResources.Simple.REPOSITORY_URI).setResolvingStrategy(TestHelper.getTestResolvingStrategy())
                .build(TestHelper.getTestProcessor());
    }
}
