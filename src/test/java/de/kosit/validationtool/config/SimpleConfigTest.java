package de.kosit.validationtool.config;

import static de.kosit.validationtool.config.TestConfigurationFactory.createSimpleConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.impl.DefaultCheck;
import de.kosit.validationtool.impl.Helper.Simple;

/**
 * @author Andreas Penski
 */
public class SimpleConfigTest {

    @Test
    public void testSimpleWithApi() {
        //@formatter:off
        final Configuration config = createSimpleConfiguration().build();
        //@formatter:on
        final DefaultCheck check = new DefaultCheck(config);
        final Result result = check.checkInput(InputFactory.read(Simple.SIMPLE_VALID));
        assertThat(result).isNotNull();
    }

}
