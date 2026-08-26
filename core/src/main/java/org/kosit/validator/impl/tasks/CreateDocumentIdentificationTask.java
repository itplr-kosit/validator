package org.kosit.validator.impl.tasks;

import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.builder;
import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.detectionBuilder;

import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.model.DocumentIdentificationType;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.xvrl.model.XVRLDetectionType;
import org.kosit.xvrl.model.XVRLDocumentType;
import org.kosit.xvrl.model.XVRLMetadataType;
import org.kosit.xvrl.model.XVRLReportType;

/**
 * Creates a document identification element for the report by using the generates hash.
 * 
 * @author Andreas Penski
 */
public class CreateDocumentIdentificationTask implements CheckTask {

    public static final Process.ProcessKey<DocumentIdentificationType, XMLSyntaxError> KEY = new Process.ProcessKey<>(
            DocumentIdentificationType.class, XMLSyntaxError.class);

    private static final String REPORT_NAME = "CreateDocument Identification Validator";

    private static XVRLReportType generateXVRLReport(
            final SingleProcessingResult<DocumentIdentificationType, XMLSyntaxError> currentResult) {
        if (currentResult.isValid()) {
            final DocumentIdentificationType result = currentResult.getObject();
            return builder(REPORT_NAME)
                    .add(detectionBuilder().addMessage(result.getDocumentReference()).severity(XVRLDetectionType.Severity.INFO)).build();
        }
        return builder(REPORT_NAME).addAll(currentResult.getErrors().stream().map(e -> detectionBuilder().addError(e)).toList()).build();

    }

    private static void addDocumentIdentification(final Process transporter) {
        final XVRLMetadataType metadata = transporter.getXvrlReportSummary().getMetadata();
        final XVRLDocumentType document = new XVRLDocumentType();
        document.setHref(transporter.getInput().getName());
        metadata.getDocuments().add(document);
    }

    @Override
    public ProcessStepResult<DocumentIdentificationType, XMLSyntaxError> check(final Process process) {
        final DocumentIdentificationType documentIdentificationType = new DocumentIdentificationType();
        final DocumentIdentificationType.DocumentHash documentHash = new DocumentIdentificationType.DocumentHash();
        documentHash.setHashAlgorithm(process.getInput().getHashAlgorithmName());
        documentHash.setHashValue(process.getInput().getHashBytes());
        documentIdentificationType.setDocumentHash(documentHash);
        documentIdentificationType.setDocumentReference(process.getInput().getName());
        addDocumentIdentification(process);

        final ProcessStepResult<DocumentIdentificationType, XMLSyntaxError> processStepResult = new ProcessStepResult<>(KEY);
        final SingleProcessingResult<DocumentIdentificationType, XMLSyntaxError> result = new SingleProcessingResult<>(
                documentIdentificationType);
        processStepResult.setResult(result);
        processStepResult.setReport(generateXVRLReport(result));
        return processStepResult;
    }
}
