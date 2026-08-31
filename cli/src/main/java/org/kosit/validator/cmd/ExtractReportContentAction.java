package org.kosit.validator.cmd;

import static org.kosit.validator.xvrl.XvrlReportBuilder.builder;

import java.nio.file.Path;
import java.util.List;

import org.kosit.base.error.SimpleError;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.tasks.BusinessReport;
import org.kosit.validator.impl.tasks.CheckTask;
import org.kosit.validator.impl.tasks.CreateReportsTask;
import org.kosit.validator.xvrl.XvrlDetectionBuilder;
import org.kosit.xvrl.model.XvrlReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

/**
 * Extracts generated documents from the report and persists them in the configured output directory.
 *
 * @author Andreas Penski
 */
class ExtractReportContentAction implements CheckTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractReportContentAction.class);

    public static final Process.ProcessKey<Boolean, String> KEY = new Process.ProcessKey<>(Boolean.class, String.class);

    private static final String REPORT_NAME = "Extract Create Report Content";

    private final Path outputDirectory;

    private Processor processor;

    public ExtractReportContentAction(final Processor processor, final Path outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.processor = processor;
    }

    private static XvrlReport generateXvrlReport(final SingleProcessingResult<Boolean, String> result) {
        if (result.isValid()) {
            return builder(REPORT_NAME).addDetection(XvrlDetectionBuilder.builder().addMessage("Extraction successful")).build();
        }
        return builder(REPORT_NAME).addDetections(result.getErrors().stream().map(e -> XvrlDetectionBuilder.builderError().addMessage(e)))
                .build();
    }

    @Override
    public ProcessStepResult<Boolean, String> check(final Process results) {
        final SingleProcessingResult<List<BusinessReport>, SimpleError> reportReposts = results.getResult(CreateReportsTask.KEY);
        reportReposts.getObject().forEach(entry -> {
            print(entry.getName(), entry.getContent());
        });
        final ProcessStepResult<Boolean, String> processStepResult = new ProcessStepResult<>(KEY);
        final SingleProcessingResult<Boolean, String> stepResult = new SingleProcessingResult<>(true);
        processStepResult.setResult(stepResult);
        processStepResult.setReport(generateXvrlReport(stepResult));
        return processStepResult;
    }

    private void print(final String origName, final XdmItem xdmItem) {
        final XdmNode node = (XdmNode) xdmItem;
        final String name = origName + "-Create_Report-result";
        final Path file = this.outputDirectory.resolve(name + ".xml");
        final Serializer serializer = this.processor.newSerializer(file.toFile());
        try {
            LOGGER.info("Writing create-report result \'{}\' to {}", name, file.toAbsolutePath());
            serializer.serializeNode(node);
        } catch (final SaxonApiException e) {
            LOGGER.error("Error extracting create-report content to {}", file.toAbsolutePath(), e);
        }
    }

    @Override
    public boolean isSkipped(final Process results) {
        final SingleProcessingResult<List<BusinessReport>, SimpleError> createReportResult = results.getResult(CreateReportsTask.KEY);
        if (createReportResult == null || createReportResult.getObject() == null) {
            LOGGER.warn("Can not extract create-report content. No report document found");
            return true;
        }
        return false;
    }

    public ExtractReportContentAction(final Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
