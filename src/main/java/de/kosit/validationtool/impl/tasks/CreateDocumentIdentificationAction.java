/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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

import de.kosit.validationtool.model.reportInput.DocumentIdentificationType;

/**
 * Creates a document identification element for the report by using the generates hash.
 * 
 * @author Andreas Penski
 */
public class CreateDocumentIdentificationAction implements CheckAction {

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
