package org.kosit.validator.docs;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.kosit.validator.api.VResult;
import org.kosit.validator.api.VCheck;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.VInput;
import org.kosit.validator.api.VInputFactory;
import org.kosit.validator.impl.DefaultVCheck;
import org.kosit.validator.impl.TestEngineInformation;
import org.kosit.validator.impl.xml.ProcessorProvider;
import org.w3c.dom.Document;

/**
 * Example code that is used in the docs/api.md file
 */
public class StandardExample {

    @SuppressWarnings("unused")
    public void run(final Path testDocument) throws URISyntaxException {
        // Load scenarios.xml from classpath
        final URL scenarios = this.getClass().getClassLoader().getResource("examples/simple/scenarios-with-relative-paths.xml");
        // Load the rest of the specific Validator configuration from classpath
        final VConfiguration config = VConfiguration.load(scenarios.toURI()).build(ProcessorProvider.getProcessor());
        // Use the default validation procedure
        final VCheck validator = new DefaultVCheck(new TestEngineInformation(), config);
        // Validate a single document
        final VInput document = VInputFactory.read(testDocument);
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
