/*
 * Copyright 2017-2026  Koordinierungsstelle für IT-Standards (KoSIT)
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
package org.kosit.validator.impl.conformatron.action.parsedoc.xml;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.source.CTReadResource;
import org.conformatron.api.model.source.CTValidationSource;
import org.kosit.conformatron.detection.DetectionList;
import org.kosit.conformatron.source.DomValidationSource;
import org.kosit.conformatron.source.ValidationSource;
import org.kosit.validator.impl.conformatron.action.parsedoc.AbstractParseDocumentAction;
import org.kost.validator.api.xml.XmlDetection;
import org.kost.validator.api.xml.XmlParser;
import org.w3c.dom.Document;

/**
 * First validator action built against the conformatron-api: step 2 of the canonical pipeline, {@code PARSE_DOCUMENT}
 * (see {@code conformatron-api/doc/steps/step-02-parse-document.md}).
 * <p>
 * Facade strategy: the legacy {@link VInput} abstraction keeps doing the heavy lifting of accessing the data. This
 * action retains the entire source document as an immutable byte array, computes the SHA-512 hash (via
 * {@link SourceDigest}, ADR-003) and parses the document into a W3C DOM <b>without line numbering</b>
 * (ADR-001/ADR-002), producing a {@link DomValidationSource}.
 * </p>
 * <p>
 * Output paths per step specification: success ({@code document-parsed}, INFO), well-formedness failure
 * ({@code not-wellformed}, one FATAL detection per parser error with line/column) and IO failure
 * ({@code source-read-error}, FATAL). Failures cancel the process; the detections still contribute to the (partial)
 * CVRL report. In the future we need to also cover each other detected syntaxes (e.g. JSON, edfact etc.) (result from
 * Step 1)
 * </p>
 *
 * @author Andreas Schmitz
 * @author Philip Helger
 */
public class ParseXmlAction extends AbstractParseDocumentAction {

    @Override
    public String getName() {
        return "ParseXML";
    }

    /**
     * Parses the document supplied by the legacy {@link VInput} and checks well-formedness.
     *
     * @param input the legacy input carrying the document
     * @return the result including the {@link DomValidationSource} on success and any detections
     */
    public ParseXmlResult execute(final CTReadResource input) {
        Objects.requireNonNull(input);

        final CTValidationSource validationSource = ValidationSource.completeXml(input);

        final AtomicReference<Document> docHolder = new AtomicReference<>();
        final DetectionList detections = XmlParser.parseXmlDom(input, docHolder::set);
        if (detections.containsNoError()) {
            // Parsing succeeded
            return new ParseXmlResult(CTStepResult.SUCCESS, new DetectionList(XmlDetection.success(validationSource)),
                    new DomValidationSource(validationSource, docHolder.get()));
        }
        // Parsing failed (for whatever reason)
        return new ParseXmlResult(CTStepResult.FAILURE, detections, DomValidationSource.unparsed(validationSource));
    }
}
