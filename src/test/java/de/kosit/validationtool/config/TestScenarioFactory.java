package de.kosit.validationtool.config;

import static de.kosit.validationtool.config.ConfigurationBuilder.report;
import static de.kosit.validationtool.config.ConfigurationBuilder.scenario;
import static de.kosit.validationtool.config.ConfigurationBuilder.schema;

import java.net.URI;

/**
 * @author Andreas Penski
 */
public class TestScenarioFactory {

    public static ScenarioBuilder createScenario() {
        return scenario("simple").validate(schema("Sample Schema").schemaLocation(URI.create("simple.xsd")))
                .with(report("Report für eRechnung").source("report.xsl")).acceptWith("count(//test:rejected) = 0")
                .declareNamespace("cri", "http://www.xoev.de/de/validator/framework/1/createreportinput")
                .declareNamespace("rpt", "http://validator.kosit.de/test-report")
                .declareNamespace("test", "http://validator.kosit.de/test-sample").match("/test:simple");
    }
}
