package de.kosit.validationtool.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.ResolvingConfigurationStrategy;
import de.kosit.validationtool.impl.ResolvingMode;
import de.kosit.validationtool.impl.xml.StrictLocalResolvingStrategy;

/**
 * @author Andreas Penski
 */
public class ConfigurationLoaderTest {

    @Test
    public void testCustomResolvingStrategy() {
        final ConfigurationLoader loader = TestConfigurationFactory.loadSimpleConfiguration();
        final ResolvingConfigurationStrategy strategy = mock(ResolvingConfigurationStrategy.class);
        loader.setResolvingStrategy(strategy);
        loader.setResolvingMode(ResolvingMode.STRICT_LOCAL);
        final Configuration config = loader.build();
        assertThat(config.getContentRepository().getResolvingConfigurationStrategy()).isNotInstanceOf(StrictLocalResolvingStrategy.class);
    }
}
