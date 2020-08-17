package de.kosit.validationtool.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.ResolvingMode;
import de.kosit.validationtool.impl.xml.RemoteResolvingStrategy;
import de.kosit.validationtool.impl.xml.StrictRelativeResolvingStrategy;

/**
 * @author Andreas Penski
 */
public class ConfigurationLoaderTest {

    @Test
    public void testCustomResolvingStrategy() {
        final ConfigurationLoader loader = TestConfigurationFactory.loadSimpleConfiguration();
        loader.setResolvingStrategy(new StrictRelativeResolvingStrategy());
        loader.setResolvingMode(ResolvingMode.ALLOW_REMOTE);
        final Configuration config = loader.build();
        assertThat(config.getContentRepository().getResolvingConfigurationStrategy()).isNotInstanceOf(RemoteResolvingStrategy.class);
    }
}
