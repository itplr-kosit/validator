package de.kosit.validationtool.config;

import static de.kosit.validationtool.config.ConfigurationBuilder.fallback;
import static de.kosit.validationtool.config.ConfigurationBuilder.report;
import static de.kosit.validationtool.config.ConfigurationBuilder.scenario;
import static de.kosit.validationtool.config.ConfigurationBuilder.schema;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

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
        final Configuration config = 
                Configuration.create().name("Simple-API")
                     .with(scenario("simple")
                                .validate(schema("Sample Schema").schemaLocation(URI.create("simple.xsd")))
                           .with(report("Report für eRechnung").source("report.xsl"))
                           .acceptWith("count(//test:rejected) = 0")
                           .declareNamespace("cri", "http://www.xoev.de/de/validator/framework/1/createreportinput")
                           .declareNamespace("rpt", "http://validator.kosit.de/test-report")
                           .declareNamespace("test", "http://validator.kosit.de/test-sample")
                           .match("/test:simple")
//                           .description("awesome api")
                           )
                     .with(fallback().name("default").source("report.xsl"))
                             
                     .resolvingMode(ResolvingMode.STRICT_RELATIVE)
                     .useRepository(Simple.REPOSITORY_URI).build();
        //@formatter:on
        final DefaultCheck check = new DefaultCheck(config);
        final Result result = check.checkInput(InputFactory.read(Simple.SIMPLE_VALID));
        assertThat(result).isNotNull();
    }
}
