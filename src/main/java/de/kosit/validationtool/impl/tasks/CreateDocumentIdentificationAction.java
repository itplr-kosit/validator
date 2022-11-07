/*
 * Copyright 2017-2021  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kosit.validationtool.impl.tasks;

import static de.kosit.validationtool.impl.xvrl.XVRLReportBuilder.builder;
import static de.kosit.validationtool.impl.xvrl.XVRLReportBuilder.detection;

import java.util.stream.Collectors;

import de.kosit.validationtool.impl.model.ProcessStepResult;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.DocumentIdentificationType;
import de.kosit.validationtool.model.XMLSyntaxError;
import de.kosit.validationtool.model.xvrl.Document;
import de.kosit.validationtool.model.xvrl.XVRLDetection;
import de.kosit.validationtool.model.xvrl.XVRLMetadata;
import de.kosit.validationtool.model.xvrl.XVRLReport;

/**
 * Creates a document identification element for the report by using the generates hash.
 * 
 * @author Andreas Penski
 */
public class CreateDocumentIdentificationAction implements CheckAction {

    public static final Process.Key<DocumentIdentificationType, XMLSyntaxError> KEY = new Process.Key<>(DocumentIdentificationType.class,
            XMLSyntaxError.class);

    private static final String REPORT_NAME = "CreateDocument Identification Validator";

    private static XVRLReport generateXVRLReport(final Result<DocumentIdentificationType, XMLSyntaxError> currentResult) {
        if (currentResult.isValid()) {
            final DocumentIdentificationType result = currentResult.getObject();
            return builder(REPORT_NAME).add(detection().addMessage(result.getDocumentReference()).severity(XVRLDetection.Severity.INFO))
                    .build();
        }
        return builder(REPORT_NAME)
                .addAll(currentResult.getErrors().stream().map(e -> detection().addError(e)).collect(Collectors.toList())).build();

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
