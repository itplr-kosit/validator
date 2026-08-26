package org.kosit.validator.docs;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.conformatron.api.model.source.CTReadResource;
import org.kosit.validator.api.VCheck;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.VResult;
import org.kosit.validator.impl.DefaultVCheck;
import org.kosit.validator.impl.TestEngineInformation;
import org.kosit.validator.impl.conformatron.source.ReadResource;
import org.kosit.validator.impl.conformatron.source.Resource;
import org.kosit.validator.impl.conformatron.source.ResourceHelper;
import org.kosit.validator.impl.saxon.ProcessorProvider;
import org.w3c.dom.Document;

/**
 * Example code that is used in the docs/api.md file
 */
public class StandardExample {

    public void run(final Path testDocument) throws URISyntaxException, IOException {
        // Load scenarios.xml from classpath
        final URL scenarios = this.getClass().getClassLoader().getResource("examples/simple/scenarios-with-relative-paths.xml");
        // Load the rest of the specific Validator configuration from classpath
        final VConfiguration config = VConfiguration.load(scenarios.toURI()).build(ProcessorProvider.getProcessor());
        // Use the default validation procedure
        final VCheck validator = new DefaultVCheck(new TestEngineInformation(), config);
        // Temporary file helper
        try ( ResourceHelper resHelper = new ResourceHelper() ) {
            // Validate a single document
            final CTReadResource document = ReadResource.of(Resource.of(testDocument), resHelper);
            // Get Result including information about the whole validation
            final VResult report = validator.checkInput(document);
            System.out.println("Is processing successful=" + report.isProcessingSuccessful());
            // Get report document if processing was successful
            Document result = null;
            if (report.isProcessingSuccessful()) {
                result = report.getReportDocument();
            }
            // continue processing results...
        }
    }

    public static void main(final String[] args) throws Exception {
        // Use e.g. "src/test/resources/examples/simple/input/foo.xml"
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
