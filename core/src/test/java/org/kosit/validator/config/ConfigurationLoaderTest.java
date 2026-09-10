package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;

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

        // an empty acceptMatch is a placeholder, not an XPath - loading did not choke on it
        assertThat(simple.getConfiguration().getAcceptMatch()).isEmpty();
        assertThat(simple.isUnconditional()).isFalse();
        assertThat(simple.getDefinitionFile()).isEqualTo(TestResources.Simple.SCENARIOS_WRITTEN_FOR_2_0.toString());
        assertThat(config.getDefinitionFile()).isEqualTo(TestResources.Simple.SCENARIOS_WRITTEN_FOR_2_0.toString());
        // the processor is named at the rule set, the overrides are declared with it
        assertThat(simple.getConfiguration().getValidateWithSchematron().get(0).getCompiler()).isEqualTo("schxslt");
        // the ids of the scenario and of its artifacts are read; nothing but the report uses them yet
        assertThat(simple.getConfiguration().getId()).isEqualTo("org.kosit.validator.test:simple:1.0.0");
        assertThat(simple.getConfiguration().getValidateWithXmlSchema().getResource().get(0).getId())
                .isEqualTo("org.kosit.validator.test:simple-xsd:1.0.0");
        assertThat(simple.getConfiguration().getValidateWithSchematron().get(0).getResource().getId())
                .isEqualTo("org.kosit.validator.test:simple-rules:1.0.0:compiled");
        assertThat(ScenarioSeverityOverrides.fromConfiguration(simple.getConfiguration()).effectiveFor("rejected"))
                .isEqualTo(CTStandardSeverity.WARNING);
    }
}
