package org.kosit.validator.impl;

import org.conformatron.api.model.source.CTReadResource;
import org.kosit.validator.TestHelper;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;

/**
 * Shared objects of the engine tests: the test processor, and parsing the way step 2 does it.
 *
 * @author Andreas Penski
 */
public class TestObjectFactory {

    private static Processor processor;

    public static Processor getProcessor() {
        if (processor == null) {
            processor = TestHelper.getTestProcessor();
        }
        return processor;
    }

    /**
     * Parses like step 2 of the pipeline — the secured parser, a result that carries the detections of a failure.
     *
     * @param input the document
     * @return the step-2 result
     */
    public static ParseXmlResult parseDocument(final CTReadResource input) {
        return new ParseXmlAction().execute(input);
    }

    /**
     * Parses like step 2 and wraps the DOM into the Saxon model of the given processor — what the scenario matching
     * works on.
     *
     * @param processor the processor whose model the node belongs to
     * @param input the document; must parse
     * @return the document as an XdmNode
     */
    public static XdmNode parse(final Processor processor, final CTReadResource input) {
        final ParseXmlResult result = parseDocument(input);
        if (!result.isSuccess()) {
            throw new IllegalStateException("Test document does not parse: " + input.getName());
        }
        return processor.newDocumentBuilder().wrap(result.getParsedSource().getParsedContent());
    }
}
