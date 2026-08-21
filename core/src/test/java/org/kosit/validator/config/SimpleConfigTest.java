package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.config.TestConfigurationFactory.createSimpleConfiguration;

import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.VInputFactory;
import org.kosit.validator.api.VResult;
import org.kosit.validator.impl.DefaultVCheck;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.TestEngineInformation;

/**
 * @author Andreas Penski
 */
public class SimpleConfigTest {

    @Test
    public void testSimpleWithApi() {
        //@formatter:off
        final VConfiguration config = createSimpleConfiguration().build(TestHelper.getTestProcessor());
        //@formatter:on
        final DefaultVCheck check = new DefaultVCheck(new TestEngineInformation(), config);
        final VResult result = check.checkInput(VInputFactory.read(Simple.SIMPLE_VALID));
        assertThat(result).isNotNull();
    }

}
