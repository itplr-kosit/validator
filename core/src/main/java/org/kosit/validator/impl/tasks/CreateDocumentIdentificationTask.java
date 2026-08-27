package org.kosit.validator.impl.tasks;

import static org.kosit.validator.xvrl.XvrlDetectionBuilder.detectionBuilder;
import static org.kosit.validator.xvrl.XvrlReportBuilder.builder;

import org.kosit.validator.api.xmlerror.XmlSyntaxError;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.model.DocumentHash;
import org.kosit.validator.model.DocumentIdentificationType;
import org.kosit.xvrl.model.XvrlDocumentType;
import org.kosit.xvrl.model.XvrlMetadataType;
import org.kosit.xvrl.model.XvrlReportType;
import org.kosit.xvrl.model.XvrlSeverityType;

/**
 * Creates a document identification element for the report by using the generates hash.
 * 
 * @author Andreas Penski
 */
public class CreateDocumentIdentificationTask implements CheckTask {

    public static final Process.ProcessKey<DocumentIdentificationType, XmlSyntaxError> KEY = new Process.ProcessKey<>(
            DocumentIdentificationType.class, XmlSyntaxError.class);

    private static final String REPORT_NAME = "CreateDocument Identification Validator";

    private static XvrlReportType generateXvrlReport(
            final SingleProcessingResult<DocumentIdentificationType, XmlSyntaxError> currentResult) {
        if (currentResult.isValid()) {
            final DocumentIdentificationType result = currentResult.getObject();
            return builder(REPORT_NAME).add(detectionBuilder().addMessage(result.getDocumentReference()).severity(XvrlSeverityType.INFO))
                    .build();
        }
        return builder(REPORT_NAME).addAll(currentResult.getErrors().stream().map(e -> detectionBuilder().addError(e)).toList()).build();

    }

    private static void addDocumentIdentification(final Process transporter) {
        final XvrlMetadataType metadata = transporter.getXvrlReportSummary().getMetadata();
        final XvrlDocumentType document = new XvrlDocumentType();
        document.setHref(transporter.getInput().getName());
        metadata.getDocuments().add(document);
    }

    @Override
    public ProcessStepResult<DocumentIdentificationType, XmlSyntaxError> check(final Process process) {
        final DocumentIdentificationType documentIdentificationType = new DocumentIdentificationType();
        final DocumentHash documentHash = new DocumentHash();
        documentHash.setHashAlgorithm(process.getInput().getHashAlgorithmName());
        documentHash.setHashValue(process.getInput().getHashBytes());
        documentIdentificationType.setDocumentHash(documentHash);
        documentIdentificationType.setDocumentReference(process.getInput().getName());
        addDocumentIdentification(process);

        final ProcessStepResult<DocumentIdentificationType, XmlSyntaxError> processStepResult = new ProcessStepResult<>(KEY);
        final SingleProcessingResult<DocumentIdentificationType, XmlSyntaxError> result = new SingleProcessingResult<>(
                documentIdentificationType);
        processStepResult.setResult(result);
        processStepResult.setReport(generateXvrlReport(result));
        return processStepResult;
    }
}
