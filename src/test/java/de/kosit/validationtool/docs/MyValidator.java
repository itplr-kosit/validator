package de.kosit.validationtool.docs;

import static de.kosit.validationtool.config.ConfigurationBuilder.fallback;
import static de.kosit.validationtool.config.ConfigurationBuilder.report;
import static de.kosit.validationtool.config.ConfigurationBuilder.scenario;
import static de.kosit.validationtool.config.ConfigurationBuilder.schema;
import static de.kosit.validationtool.config.ConfigurationBuilder.schematron;

import java.net.URI;
import java.nio.file.Paths;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.DefaultCheck;
import de.kosit.validationtool.impl.xml.ProcessorProvider;

/**
 * Example code that is used in the docs/api.md file
 */
public class MyValidator {

    public static void main(final String[] args) {
        final Configuration config = Configuration.create().name("myconfiguration")
                .with(scenario("firstScenario").match("//myNode").validate(schema("Sample Schema").schemaLocation(URI.create("simple.xsd")))
                        .validate(schematron("my rules").source("myRules.xsl")).with(report("my report").source("report.xsl")))
                .with(fallback().name("default-report").source("fallback.xsl")).useRepository(Paths.get("/opt/myrepository"))
                .build(ProcessorProvider.getProcessor());
        final Check validator = new DefaultCheck(config);
        // .. run your checks
    }
}