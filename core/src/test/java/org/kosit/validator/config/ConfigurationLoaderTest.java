package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.kosit.schematron.resolve.RemoteResolvingStrategy;
import org.kosit.schematron.resolve.ResolvingMode;
import org.kosit.validator.TestHelper;
import org.kosit.validator.api.VConfiguration;

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
}
