package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.xml.RemoteResolvingStrategy;
import org.kosit.validator.impl.xml.StrictRelativeResolvingStrategy;

/**
 * @author Andreas Penski
 */
public class ConfigurationLoaderTest {

    @Test
    public void testCustomResolvingStrategy() {
        final ConfigurationLoader loader = TestConfigurationFactory.loadSimpleConfiguration();
        loader.setResolvingStrategy(new StrictRelativeResolvingStrategy());
        loader.setResolvingMode(ResolvingMode.ALLOW_REMOTE);
        final VConfiguration config = loader.build(TestHelper.getTestProcessor());
        assertThat(config.getContentRepository().getResolvingConfigurationStrategy()).isNotInstanceOf(RemoteResolvingStrategy.class);
    }
}
