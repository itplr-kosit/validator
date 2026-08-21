package org.kosit.validator.impl.tasks;

import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.builder;
import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.detection;

import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.model.DocumentIdentificationType;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.xvrl.model.Document;
import org.kosit.xvrl.model.XVRLDetection;
import org.kosit.xvrl.model.XVRLMetadata;
import org.kosit.xvrl.model.XVRLReport;

/**
 * Creates a document identification element for the report by using the generates hash.
 * 
 * @author Andreas Penski
 */
public class CreateDocumentIdentificationTask implements CheckTask {

    public static final Process.Key<DocumentIdentificationType, XMLSyntaxError> KEY = new Process.Key<>(DocumentIdentificationType.class,
            XMLSyntaxError.class);

    private static final String REPORT_NAME = "CreateDocument Identification Validator";

    private static XVRLReport generateXVRLReport(final Result<DocumentIdentificationType, XMLSyntaxError> currentResult) {
        if (currentResult.isValid()) {
            final DocumentIdentificationType result = currentResult.getObject();
            return builder(REPORT_NAME).add(detection().addMessage(result.getDocumentReference()).severity(XVRLDetection.Severity.INFO))
                    .build();
        }
        return builder(REPORT_NAME).addAll(currentResult.getErrors().stream().map(e -> detection().addError(e)).toList()).build();

    }

    private static void addDocumentIdentification(final Process transporter) {
        final XVRLMetadata metadata = transporter.getXvrlReportSummary().getMetadata();
        final Document document = new Document();
        document.setHref(transporter.getInput().getName());
        metadata.getDocuments().add(document);
    }

    @Override
    public ProcessStepResult<DocumentIdentificationType, XMLSyntaxError> check(final Process process) {
        final DocumentIdentificationType documentIdentificationType = new DocumentIdentificationType();
        final DocumentIdentificationType.DocumentHash documentHash = new DocumentIdentificationType.DocumentHash();
        documentHash.setHashAlgorithm(process.getInput().getDigestAlgorithm());
        documentHash.setHashValue(process.getInput().getHashCode());
        documentIdentificationType.setDocumentHash(documentHash);
        documentIdentificationType.setDocumentReference(process.getInput().getName());
        addDocumentIdentification(process);

        final ProcessStepResult<DocumentIdentificationType, XMLSyntaxError> processStepResult = new ProcessStepResult<>(KEY);
        final Result<DocumentIdentificationType, XMLSyntaxError> result = new Result<>(documentIdentificationType);
        processStepResult.setResult(result);
        processStepResult.setReport(generateXVRLReport(result));
        return processStepResult;
    }
}
