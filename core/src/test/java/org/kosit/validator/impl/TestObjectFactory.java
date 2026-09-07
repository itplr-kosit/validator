package org.kosit.validator.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.conformatron.api.model.source.CTReadResource;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.TestHelper;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.tasks.BusinessReport;
import org.kosit.validator.impl.tasks.DocumentParseTask;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

/**
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

    public static String serialize(final List<BusinessReport> reports) {
        try ( final StringWriter writer = new StringWriter() ) {
            final Serializer serializer = getProcessor().newSerializer(writer);
            for (final BusinessReport report : reports) {
                final XdmNode node = report.getContent();
                serializer.serializeNode(node);
            }
            return writer.toString();
        } catch (final SaxonApiException | IOException e) {
            throw new IllegalStateException("Can not serialize document", e);
        }
    }

    public static SingleProcessingResult<XdmNode, SimpleError> parseDocument(final Processor processor, final CTReadResource input) {
        return new DocumentParseTask(processor).parseDocument(input);
    }

    public static SingleProcessingResult<XdmNode, SimpleError> parseDocument(final CTReadResource input) {
        return new DocumentParseTask(TestHelper.getTestProcessor()).parseDocument(input);
    }
}
