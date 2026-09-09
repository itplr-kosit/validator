package org.kosit.validator.docs;

import static org.kosit.validator.config.ConfigurationBuilder.scenario;
import static org.kosit.validator.config.ConfigurationBuilder.schema;
import static org.kosit.validator.config.ConfigurationBuilder.schematron;

import java.net.URI;
import java.nio.file.Paths;

import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.ConformanceValidation;
import org.kosit.validator.impl.TestEngineInformation;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kost.validator.api.saxon.ProcessorProvider;

/**
 * Example code that is used in the docs/api.md file: a configuration assembled in Java, and the engine built over it.
 */
public class MyValidator {

    @SuppressWarnings("unused")
    public static void main(final String[] args) {
        final ScenarioSet config = ScenarioSet.create().name("myconfiguration")
                .with(scenario("firstScenario").match("//myNode").validate(schema("Sample Schema").schemaLocation(URI.create("simple.xsd")))
                        .validate(schematron("my rules").source("myRules.xsl").compiler("schxslt")))
                .useRepository(Paths.get("/opt/myrepository")).build(ProcessorProvider.getProcessor());
        // the engine: configuration is a construction concern, validate(...) takes nothing but the document
        final ValidationEngine<ConformanceValidationResult> validator = new ConformanceValidation(new TestEngineInformation(),
                ProcessorProvider.getProcessor(), config);
        // .. run your checks
    }
}
