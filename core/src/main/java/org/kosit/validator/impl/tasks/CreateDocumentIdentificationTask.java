package org.kosit.validator.impl.tasks;

import static org.kosit.validator.xvrl.XvrlReportBuilder.builder;

import org.kosit.base.error.SimpleError;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.model.DocumentHash;
import org.kosit.validator.model.DocumentIdentificationType;
import org.kosit.validator.xvrl.XvrlDetectionBuilder;
import org.kosit.xvrl.model.XvrlDocument;
import org.kosit.xvrl.model.XvrlReport;

/**
 * Creates a document identification element for the report by using the generates hash.
 * 
 * @author Andreas Penski
 */
public class CreateDocumentIdentificationTask implements CheckTask {

    public static final Process.ProcessKey<DocumentIdentificationType, SimpleError> KEY = new Process.ProcessKey<>(
            DocumentIdentificationType.class, SimpleError.class);

    private static final String REPORT_NAME = "CreateDocument Identification Validator";

    private static XvrlReport generateXvrlReport(final SingleProcessingResult<DocumentIdentificationType, SimpleError> currentResult) {
        if (currentResult.isValid()) {
            final DocumentIdentificationType result = currentResult.getObject();
            return builder(REPORT_NAME).addDetection(XvrlDetectionBuilder.builderInfo().addMessage(result.documentReference())).build();
        }
        return builder(REPORT_NAME).addDetections(currentResult.getErrors().stream().map(e -> XvrlDetectionBuilder.builder().addError(e)))
                .build();

    }

    private static void addDocumentIdentification(final Process transporter) {
        transporter.setMetadata(
                transporter.getMetadata().toBuilder().addDocument(XvrlDocument.builder(transporter.getInput().getName())).build());
    }

    @Override
    public ProcessStepResult<DocumentIdentificationType, SimpleError> check(final Process process) {
        addDocumentIdentification(process);

        final ProcessStepResult<DocumentIdentificationType, SimpleError> processStepResult = new ProcessStepResult<>(KEY);
        final DocumentIdentificationType documentIdentificationType = new DocumentIdentificationType(
                new DocumentHash(process.getInput().getHashAlgorithmName(), process.getInput().getHashBytes()),
                process.getInput().getName());
        final SingleProcessingResult<DocumentIdentificationType, SimpleError> result = new SingleProcessingResult<>(
                documentIdentificationType);
        processStepResult.setResult(result);
        processStepResult.setReport(generateXvrlReport(result));
        return processStepResult;
    }
}
