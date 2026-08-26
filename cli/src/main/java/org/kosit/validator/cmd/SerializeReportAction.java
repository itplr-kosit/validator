package org.kosit.validator.cmd;

import static org.kosit.validator.xvrl.XVRLReportBuilder.builder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.tasks.CheckTask;
import org.kosit.validator.impl.tasks.CreateReportsTask;
import org.kosit.validator.xvrl.XVRLReportBuilder;
import org.kosit.validator.xvrl.XvrlDetectionBuilder;
import org.kosit.xvrl.impl.XvrlConversionService;
import org.kosit.xvrl.model.ObjectFactory;
import org.kosit.xvrl.model.XVRLDetectionType;
import org.kosit.xvrl.model.XVRLReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes the validation result as an XML document to a defined location.
 *
 * @author Andreas Penski
 */
class SerializeReportAction implements CheckTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializeReportAction.class);

    public static final Process.ProcessKey<Boolean, String> KEY = new Process.ProcessKey<>(Boolean.class, String.class);

    private static final String REPORT_NAME = "Serialize Report";

    private final Path outputDirectory;

    private final NamingStrategy namingStrategy;

    private static XVRLReportType generateXVRLReport(final SingleProcessingResult<Boolean, String> result) {
        if (result.isValid()) {
            return builder(REPORT_NAME).add(XvrlDetectionBuilder.detectionBuilder().addMessage("Serialization successful")
                    .severity(XVRLDetectionType.Severity.INFO)).build();
        }
        return XVRLReportBuilder.builder(REPORT_NAME)
                .addAll(result.getErrors().stream().map(e -> XvrlDetectionBuilder.detectionBuilder().addError(e))).build();
    }

    public SerializeReportAction(final Path outputDirectory, final NamingStrategy namingStrategy) {
        this.outputDirectory = outputDirectory;
        this.namingStrategy = namingStrategy;
    }

    @Override
    public ProcessStepResult<Boolean, String> check(final Process process) {
        final Path file = this.outputDirectory.resolve(this.namingStrategy.createName(process.getName()));
        try {
            LOGGER.info("Serializing result to {}", file.toAbsolutePath());
            final String xml = new XvrlConversionService().writeXml(new ObjectFactory().createReports(process.getXvrlReportSummary()));
            Files.write(file, xml.getBytes());
        } catch (final IOException e) {
            LOGGER.error("Can not serialize result report to {}", file.toAbsolutePath(), e);
        }
        final ProcessStepResult<Boolean, String> processStepResult = new ProcessStepResult<>(KEY);
        final SingleProcessingResult<Boolean, String> stepResult = new SingleProcessingResult<>(null, null);
        processStepResult.setResult(stepResult);
        processStepResult.setReport(generateXVRLReport(stepResult));
        return processStepResult;
    }

    @Override
    public boolean isSkipped(final Process results) {
        if (results.getResult(CreateReportsTask.KEY) == null) {
            LOGGER.warn("Can not serialize result report. No document found");
            return true;
        }
        return false;
    }
}
