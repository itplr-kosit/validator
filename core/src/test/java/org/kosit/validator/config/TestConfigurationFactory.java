package org.kosit.validator.config;

import static org.kosit.validator.config.ConfigurationBuilder.fallback;
import static org.kosit.validator.config.ConfigurationBuilder.report;
import static org.kosit.validator.config.ConfigurationBuilder.scenario;
import static org.kosit.validator.config.ConfigurationBuilder.schema;
import static org.kosit.validator.config.ConfigurationBuilder.schematron;

import java.net.URI;
import java.util.Date;

import org.kosit.base.uri.UriHelper;
import org.kosit.validator.TestHelper;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.testdata.TestResources;

/**
 * @author Andreas Penski
 */
public class TestConfigurationFactory {

    public static ConfigurationBuilder createSimpleConfiguration() {
        return VConfiguration.create().name("Simple-API").author("me").description("test desc").date(new Date())
                .with(createScenario().description("awesome scenario")).with(fallback().name("default").source("report.xsl"))
                .resolvingStrategy(TestHelper.getTestResolvingStrategy()).useRepository(TestResources.Simple.REPOSITORY_URI);
    }

    public static ConfigurationLoader loadSimpleConfiguration() {
        return VConfiguration.load(TestResources.Simple.SCENARIOS, TestResources.Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy());
    }

    public static ScenarioBuilder createScenario() {
        // the working schematron of the simple example, as scenarios.xml configures it - not the one raising error()
        return scenario("simple").validate(schema("Sample Schema").schemaLocation(URI.create("simple.xsd")))
                .validate(
                        schematron("Sample Schematron").source(UriHelper.resolve(TestResources.Simple.REPOSITORY_URI, "simple.xsl", true)))
                .with(report("Report for eInvoice").source("report.xsl")).acceptWith("count(//test:rejected) = 0")
                .declareNamespace("xvrl", "http://www.xproc.org/ns/xvrl").declareNamespace("rpt", "http://validator.kosit.de/test-report")
                .declareNamespace("test", "http://validator.kosit.de/test-sample").match("/test:simple");
    }
}
