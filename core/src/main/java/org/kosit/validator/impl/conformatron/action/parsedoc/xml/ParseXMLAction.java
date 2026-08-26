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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.source.CTReadResource;
import org.conformatron.api.model.source.CTValidationSource;
import org.kosit.base.xml.XMLHelper;
import org.kosit.validator.impl.conformatron.action.parsedoc.AbstractParseDocumentAction;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.source.DomValidationSource;
import org.kosit.validator.impl.conformatron.source.ValidationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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
public class ParseXMLAction extends AbstractParseDocumentAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseXMLAction.class);

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
    public ParseXMLResult execute(final CTReadResource input) {
        Objects.requireNonNull(input);

        final CTValidationSource validationSource = ValidationSource.completeXML(input);
        final List<CTDetection> errors = new ArrayList<>();
        try {
            // Setup XML reader
            final DocumentBuilder builder = XMLHelper.createSafeDocumentBuilder();
            builder.setErrorHandler(new CollectingErrorHandler(validationSource.getName(), errors));

            // Main reading
            final Document document = builder.parse(input.getSourceStream(), validationSource.getName());
            if (errors.isEmpty()) {
                // Parsing succeeded
                return new ParseXMLResult(CTStepResult.SUCCESS, DetectionList.of(XMLDetection.success(validationSource)),
                        new DomValidationSource(validationSource, document));
            }
        } catch (final SAXParseException e) {
            // already collected by CollectingErrorHandler#fatalError unless thrown directly
            if (errors.stream().noneMatch(d -> d.getLinkedException() == e)) {
                errors.add(XMLDetection.errorNotWellformed(validationSource.getName(), e));
            }
        } catch (final SAXException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exception while parsing {}", validationSource.getName(), e);
            }
            errors.add(XMLDetection.errorNotWellformed(validationSource.getName(), e));
        } catch (final IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("IOException while parsing {}", validationSource.getName(), e);
            }
            errors.add(XMLDetection.ioError(validationSource.getName(), e));
        }

        // Parsing failed (for whatever reason)
        return new ParseXMLResult(CTStepResult.FAILURE, new DetectionList(errors), DomValidationSource.unparsed(validationSource));
    }
}
