package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;
import org.kosit.schematron.resolve.RemoteResolvingStrategy;
import org.kosit.schematron.resolve.ResolvingMode;
import org.kosit.validator.TestHelper;
import org.kosit.validator.api.VConfiguration;
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
        final VConfiguration config = loader.build(TestHelper.getTestProcessor());
        assertThat(config.getContentRepository().getResolvingConfigurationStrategy()).isNotInstanceOf(RemoteResolvingStrategy.class);
    }

    @Test
    public void testAScenarioWrittenFor20Loads() {
        final VConfiguration config = VConfiguration
                .load(TestResources.Simple.SCENARIOS_WRITTEN_FOR_2_0, TestResources.Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
        final Scenario simple = config.getScenarios().get(0);

        // an empty acceptMatch is a placeholder, not an XPath
        assertThat(simple.getAcceptExecutable()).isNull();
        // the processor is named at the rule set, the overrides are declared with it
        assertThat(simple.getConfiguration().getValidateWithSchematron().get(0).getCompiler()).isEqualTo("schxslt");
        assertThat(ScenarioSeverityOverrides.fromConfiguration(simple.getConfiguration()).effectiveFor("rejected"))
                .isEqualTo(CTStandardSeverity.WARNING);
    }
}
