package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.config.TestConfigurationFactory.createSimpleConfiguration;

import org.junit.jupiter.api.Test;
import org.kosit.validator.api.Configuration;
import org.kosit.validator.api.InputFactory;
import org.kosit.validator.api.Result;
import org.kosit.validator.impl.DefaultVCheck;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.TestEngineInformation;

/**
 * @author Andreas Penski
 */
public class SimpleConfigTest {

    @Test
    public void testSimpleWithApi() {
        //@formatter:off
        final Configuration config = createSimpleConfiguration().build(Helper.getTestProcessor());
        //@formatter:on
        final DefaultVCheck check = new DefaultVCheck(new TestEngineInformation(), config);
        final Result result = check.checkInput(InputFactory.read(Simple.SIMPLE_VALID));
        assertThat(result).isNotNull();
    }

}
