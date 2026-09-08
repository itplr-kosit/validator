package org.kosit.validator.docs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.conformatron.api.model.source.CTReadResource;
import org.kosit.base.io.ResourceHelper;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.ConformanceValidation;
import org.kosit.validator.impl.TestEngineInformation;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kost.validator.api.saxon.ProcessorProvider;

/**
 * Example code that is used in the docs/api.md file: load a configuration, build the engine, validate a document, read
 * the verdict and write the report.
 */
public class StandardExample {

    public void run(final Path testDocument) throws URISyntaxException, IOException {
        // Load scenarios.xml from classpath
        final URL scenarios = this.getClass().getClassLoader().getResource("examples/simple/scenarios-with-relative-paths.xml");
        // Load the rest of the specific Validator configuration from classpath
        final VConfiguration config = VConfiguration.load(scenarios.toURI()).build(ProcessorProvider.getProcessor());
        // The engine over that configuration - the canonical pipeline, steps 2 to 9
        final ValidationEngine<ConformanceValidationResult> validator = new ConformanceValidation(new TestEngineInformation(),
                ProcessorProvider.getProcessor(), config);
        // Temporary file helper
        try ( ResourceHelper resHelper = new ResourceHelper() ) {
            // Validate a single document
            final CTReadResource document = ReadResource.of(Resource.of(testDocument), resHelper);
            // The result: the verdict of step 9, and the run behind it
            final ConformanceValidationResult result = validator.validate(document);
            System.out.println("Completed=" + result.isCompleted() + " decision=" + result.getDecision() + " - " + result.getRationale());
            // The report is a CVR - also for a cancelled run, which yields a partial report
            try ( OutputStream out = Files.newOutputStream(testDocument.resolveSibling(testDocument.getFileName() + "-cvr.xml")) ) {
                result.writeCvr(out);
            }
            // continue processing results...
        }
    }

    public static void main(final String[] args) throws Exception {
        // Use e.g. "test-data/src/main/resources/examples/simple/input/foo.xml"
        if (args.length == 0) {
            throw new IllegalStateException("Provide a test document filename on the commandline");
        }
        // Path of document for validation
        final Path testDoc = Paths.get(args[0]);
        final StandardExample example = new StandardExample();
        // run example validation
        example.run(testDoc);
    }
}
