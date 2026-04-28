package org.kosit.validator.docs;

import static org.kosit.validator.config.ConfigurationBuilder.fallback;
import static org.kosit.validator.config.ConfigurationBuilder.report;
import static org.kosit.validator.config.ConfigurationBuilder.scenario;
import static org.kosit.validator.config.ConfigurationBuilder.schema;
import static org.kosit.validator.config.ConfigurationBuilder.schematron;

import java.net.URI;
import java.nio.file.Paths;

import org.kosit.validator.api.Check;
import org.kosit.validator.api.Configuration;
import org.kosit.validator.impl.DefaultCheck;
import org.kosit.validator.impl.TestEngineInformation;
import org.kosit.validator.impl.xml.ProcessorProvider;

/**
 * Example code that is used in the docs/api.md file
 */
public class MyValidator {

    @SuppressWarnings("unused")
    public static void main(final String[] args) {
        final Configuration config = Configuration.create().name("myconfiguration")
                .with(scenario("firstScenario").match("//myNode").validate(schema("Sample Schema").schemaLocation(URI.create("simple.xsd")))
                        .validate(schematron("my rules").source("myRules.xsl")).with(report("my report").source("report.xsl")))
                .with(fallback().name("default-report").source("fallback.xsl")).useRepository(Paths.get("/opt/myrepository"))
                .build(ProcessorProvider.getProcessor());
        final Check validator = new DefaultCheck(new TestEngineInformation(), config);
        // .. run your checks
    }
}