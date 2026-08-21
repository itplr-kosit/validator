package org.kosit.validator.config;

import static org.kosit.validator.config.ConfigurationBuilder.fallback;
import static org.kosit.validator.config.ConfigurationBuilder.report;
import static org.kosit.validator.config.ConfigurationBuilder.scenario;
import static org.kosit.validator.config.ConfigurationBuilder.schema;
import static org.kosit.validator.config.ConfigurationBuilder.schematron;

import java.net.URI;
import java.util.Date;

import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.ResolvingMode;

/**
 * @author Andreas Penski
 */
public class TestConfigurationFactory {

    public static ConfigurationBuilder createSimpleConfiguration() {
        return VConfiguration.create().name("Simple-API").author("me").description("test desc").date(new Date())
                .with(createScenario().description("awesome scenario")).with(fallback().name("default").source("report.xsl"))

                .resolvingMode(ResolvingMode.STRICT_RELATIVE).useRepository(Simple.REPOSITORY_URI);
    }

    public static ConfigurationLoader loadSimpleConfiguration() {
        return VConfiguration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI);
    }

    public static ScenarioBuilder createScenario() {
        return scenario("simple").validate(schema("Sample Schema").schemaLocation(URI.create("simple.xsd")))
                .validate(schematron("Sample Schematron").source(Simple.SCHEMATRON))
                .with(report("Report for eInvoice").source("report.xsl")).acceptWith("count(//test:rejected) = 0")
                .declareNamespace("xvrl", "http://www.xproc.org/ns/xvrl").declareNamespace("rpt", "http://validator.kosit.de/test-report")
                .declareNamespace("test", "http://validator.kosit.de/test-sample").match("/test:simple");
    }
}
