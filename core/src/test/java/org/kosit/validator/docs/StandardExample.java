package org.kosit.validator.docs;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.kosit.validator.impl.TestEngineInformation;
import org.w3c.dom.Document;

import org.kosit.validator.api.Check;
import org.kosit.validator.api.Configuration;
import org.kosit.validator.api.Input;
import org.kosit.validator.api.InputFactory;
import org.kosit.validator.api.Result;
import org.kosit.validator.impl.DefaultCheck;
import org.kosit.validator.impl.xml.ProcessorProvider;

/**
 * Example code that is used in the docs/api.md file
 */
public class StandardExample {

    public void run(final Path testDocument) throws URISyntaxException {
        // Load scenarios.xml from classpath
        final URL scenarios = this.getClass().getClassLoader().getResource("examples/simple/scenarios-with-relative-paths.xml");
        // Load the rest of the specific Validator configuration from classpath
        final Configuration config = Configuration.load(scenarios.toURI()).build(ProcessorProvider.getProcessor());
        // Use the default validation procedure
        final Check validator = new DefaultCheck(new TestEngineInformation(), config);
        // Validate a single document
        final Input document = InputFactory.read(testDocument);
        // Get Result including information about the whole validation
        final Result report = validator.checkInput(document);
        System.out.println("Is processing succesful=" + report.isProcessingSuccessful());
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