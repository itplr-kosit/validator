package de.kosit.validationtool.config;

import static de.kosit.validationtool.config.ConfigurationBuilder.fallback;
import static de.kosit.validationtool.config.TestScenarioFactory.createScenario;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.impl.DefaultCheck;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.ResolvingMode;

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

    static ConfigurationBuilder createSimpleConfiguration() {
        return Configuration.create().name("Simple-API").with(createScenario()
        // .description("awesome api")
        ).with(fallback().name("default").source("report.xsl"))

                .resolvingMode(ResolvingMode.STRICT_RELATIVE).useRepository(Simple.REPOSITORY_URI);
    }


}
