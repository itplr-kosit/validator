package de.kosit.validationtool.impl;

import de.kosit.validationtool.impl.tasks.CheckAction;
import de.kosit.validationtool.model.reportInput.DocumentIdentificationType;

/**
 * Creates a document identification element for the report by using the generates hash.
 * 
 * @author Andreas Penski
 */
class CreateDocumentIdentificationAction implements CheckAction {

    @Override
    public void check(final Bag transporter) {
        final DocumentIdentificationType i = new DocumentIdentificationType();
        final DocumentIdentificationType.DocumentHash h = new DocumentIdentificationType.DocumentHash();
        h.setHashAlgorithm(transporter.getInput().getDigestAlgorithm());
        h.setHashValue(transporter.getInput().getHashCode());
        i.setDocumentHash(h);
        i.setDocumentReference(transporter.getInput().getName());
        transporter.getReportInput().setDocumentIdentification(i);
    }
}
