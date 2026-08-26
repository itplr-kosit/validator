package org.kosit.validator.cmd;

import java.io.StringWriter;
import java.util.List;

import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.tasks.BusinessReport;
import org.kosit.validator.impl.tasks.CheckTask;
import org.kosit.validator.impl.tasks.CreateReportsTask;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.validator.xvrl.XVRLReportBuilder;
import org.kosit.xvrl.model.XVRLReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;

/**
 * Prints the result document to stdout.
 *
 * @author Andreas Penski
 */
class PrintReportAction implements CheckTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrintReportAction.class);

    public static final Process.ProcessKey<Boolean, String> KEY = new Process.ProcessKey<>(Boolean.class, String.class);

    private final Processor processor;

    private static XVRLReportType createReport() {
        return XVRLReportBuilder.builder("Document wellformedness Validator").name("Print Report").setValid().build();
    }

    @Override
    public ProcessStepResult<Boolean, String> check(final Process results) {
        try {
            final StringWriter writer = new StringWriter();
            final Serializer serializer = this.processor.newSerializer(writer);
            final SingleProcessingResult<List<BusinessReport>, XMLSyntaxError> result = results.getResult(CreateReportsTask.KEY);
            for (final BusinessReport node : result.getObject()) {
                serializer.serializeNode(node.getContent());
            }
            Printer.writeOut(writer.toString());
        } catch (final SaxonApiException e) {
            LOGGER.error("Error while printing result to stdout", e);
        }
        return Util.createResult(KEY, true, createReport());
    }

    public PrintReportAction(final Processor processor) {
        this.processor = processor;
    }
}
