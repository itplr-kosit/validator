package org.kosit.validator.cmd;

import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.builder;
import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.detection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.tasks.CheckTask;
import org.kosit.validator.impl.tasks.CreateReportsTask;
import org.kosit.validator.impl.xvrl.XVRLReportBuilder;
import org.kosit.xvrl.impl.XvrlConversionService;
import org.kosit.xvrl.model.XVRLDetection;
import org.kosit.xvrl.model.XVRLReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes the validation result as an XML document to a defined location.
 *
 * @author Andreas Penski
 */
class SerializeReportAction implements CheckTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializeReportAction.class);

    public static final Process.Key<Boolean, String> KEY = new Process.Key<>(Boolean.class, String.class);

    private static final String REPORT_NAME = "Serialize Report";

    private final Path outputDirectory;

    private final XvrlConversionService conversionService;

    private final NamingStrategy namingStrategy;

    private static XVRLReport generateXVRLReport(final Result<Boolean, String> result) {
        if (result.isValid()) {
            return builder(REPORT_NAME).add(detection().addMessage("Serialization successful").severity(XVRLDetection.Severity.INFO))
                    .build();
        }
        return XVRLReportBuilder.builder(REPORT_NAME).addAll(result.getErrors().stream().map(e -> detection().addError(e))).build();
    }

    @Override
    public ProcessStepResult<Boolean, String> check(final Process process) {
        final Path file = this.outputDirectory.resolve(this.namingStrategy.createName(process.getName()));
        try {
            LOGGER.info("Serializing result to {}", file.toAbsolutePath());
            final String xml = this.conversionService.writeXml(process.getXvrlReportSummary());
            Files.write(file, xml.getBytes());
        } catch (final IOException e) {
            LOGGER.error("Can not serialize result report to {}", file.toAbsolutePath(), e);
        }
        final ProcessStepResult<Boolean, String> processStepResult = new ProcessStepResult<>(KEY);
        final Result<Boolean, String> stepResult = new Result<>();
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

    public SerializeReportAction(final Path outputDirectory, final XvrlConversionService conversionService,
            final NamingStrategy namingStrategy) {
        this.outputDirectory = outputDirectory;
        this.conversionService = conversionService;
        this.namingStrategy = namingStrategy;
    }
}
