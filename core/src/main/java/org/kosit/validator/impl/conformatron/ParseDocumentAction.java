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
package org.kosit.validator.impl.conformatron;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;

import org.conformatron.api.model.action.ECTCanonicalAction;
import org.conformatron.api.model.action.ECTActionType;
import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.action.ICTAction;
import org.conformatron.api.model.detection.ECTSeverity;
import org.conformatron.api.model.detection.ICTDetection;
import org.conformatron.api.model.detection.ICTDetectionList;
import org.conformatron.api.model.source.ICTValidationSource;
import org.kosit.validator.api.Input;
import org.kosit.validator.impl.input.StreamHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * First validator action built against the conformatron-api: step 2 of the canonical pipeline, {@code PARSE_DOCUMENT}
 * (see {@code conformatron-api/doc/steps/step-02-parse-document.md}).
 * <p>
 * Facade strategy: the legacy {@link Input} abstraction keeps doing the heavy lifting of accessing the data. This
 * action retains the entire source document as an immutable byte array, computes the SHA-512 hash (via
 * {@link SourceDigest}, ADR-003) and parses the document into a W3C DOM <b>without line numbering</b>
 * (ADR-001/ADR-002), producing a {@link DomValidationSource}.
 * </p>
 * <p>
 * Output paths per step specification: success ({@code document-parsed}, INFO), well-formedness failure
 * ({@code not-wellformed}, one FATAL detection per parser error with line/column) and IO failure
 * ({@code source-read-error}, FATAL). Failures cancel the process; the detections still contribute to the (partial)
 * CVRL report.
 * </p>
 *
 * @author Andreas Schmitz
 */
public class ParseDocumentAction implements ICTAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseDocumentAction.class);

    /** Detection code on success. */
    public static final String CODE_DOCUMENT_PARSED = "document-parsed";

    /** Detection code for well-formedness errors. */
    public static final String CODE_NOT_WELLFORMED = "not-wellformed";

    /** Detection code for IO failures while reading the source. */
    public static final String CODE_SOURCE_READ_ERROR = "source-read-error";

    /**
     * Result of a single execution of this action.
     *
     * @param status success or failure (failure cancels the process)
     * @param parsedSource the parsed source. On a well-formedness failure it still carries source metadata, bytes and
     *            SHA-512 hash for document identity in the partial CVRL — only without parsed content
     *            ({@code isParsed() == false}). {@code null} only when the source could not be read at all.
     * @param detections this execution's contribution to the report; never {@code null}
     */
    public record ParseDocumentResult(ECTStepResult status, DomValidationSource parsedSource, ICTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == ECTStepResult.SUCCESS;
        }
    }

    @Override
    public String getName() {
        return ECTCanonicalAction.PARSE_DOCUMENT.getCanonicalName();
    }

    @Override
    public ECTActionType getType() {
        return ECTCanonicalAction.PARSE_DOCUMENT.getDefaultType();
    }

    /**
     * Parses the document supplied by the legacy {@link Input} and checks well-formedness.
     *
     * @param input the legacy input carrying the document
     * @return the result including the {@link DomValidationSource} on success and any detections
     */
    public ParseDocumentResult execute(final Input input) {
        if (input == null) {
            throw new IllegalArgumentException("Input may not be null");
        }
        final ValidationSource source = ValidationSource.of(input);
        final byte[] bytes;
        try {
            bytes = readBytes(input);
        } catch (final IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exception while reading {}", input.getName(), e);
            }
            final Detection detection = new Detection(ECTSeverity.FATAL_ERROR, CODE_SOURCE_READ_ERROR,
                    DetectionLocation.ofResource(input.getName()),
                    "IOException while reading resource " + input.getName() + ": " + e.getMessage(), e);
            return new ParseDocumentResult(ECTStepResult.FAILURE, null, DetectionList.of(detection));
        }
        return parse(source, bytes);
    }

    /**
     * Parses the given retained document bytes into a DOM without line numbering.
     *
     * @param source the validation source metadata (from step 1)
     * @param bytes the entire source document; retained as immutable byte array
     * @return the result including the {@link DomValidationSource} on success and any detections
     */
    public ParseDocumentResult parse(final ICTValidationSource source, final byte[] bytes) {
        final List<ICTDetection> errors = new ArrayList<>();
        try {
            final DocumentBuilder builder = createDocumentBuilder();
            builder.setErrorHandler(new CollectingErrorHandler(source.getName(), errors));
            final Document dom = builder.parse(new ByteArrayInputStream(bytes), source.getName());
            if (!errors.isEmpty()) {
                return new ParseDocumentResult(ECTStepResult.FAILURE, DomValidationSource.unparsed(source, bytes),
                        new DetectionList(errors));
            }
            final DomValidationSource parsed = new DomValidationSource(source, bytes, dom);
            final Detection info = Detection.of(ECTSeverity.INFO, CODE_DOCUMENT_PARSED, DetectionLocation.ofResource(source.getName()),
                    "sha512=" + parsed.getSha512Hash());
            return new ParseDocumentResult(ECTStepResult.SUCCESS, parsed, DetectionList.of(info));
        } catch (final SAXParseException e) {
            // already collected by CollectingErrorHandler#fatalError unless thrown directly
            if (errors.stream().noneMatch(d -> d.getLinkedException() == e)) {
                errors.add(fatal(source.getName(), e));
            }
            return new ParseDocumentResult(ECTStepResult.FAILURE, DomValidationSource.unparsed(source, bytes), new DetectionList(errors));
        } catch (final SAXException | ParserConfigurationException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exception while parsing {}", source.getName(), e);
            }
            errors.add(new Detection(ECTSeverity.FATAL_ERROR, CODE_NOT_WELLFORMED, DetectionLocation.ofResource(source.getName()),
                    e.getMessage(), e));
            return new ParseDocumentResult(ECTStepResult.FAILURE, DomValidationSource.unparsed(source, bytes), new DetectionList(errors));
        } catch (final IOException e) {
            errors.add(new Detection(ECTSeverity.FATAL_ERROR, CODE_SOURCE_READ_ERROR, DetectionLocation.ofResource(source.getName()),
                    e.getMessage(), e));
            return new ParseDocumentResult(ECTStepResult.FAILURE, null, new DetectionList(errors));
        }
    }

    private static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder();
    }

    private static byte[] readBytes(final Input input) throws IOException {
        final Source source = input.getSource();
        final byte[] bytes = StreamHelper.tryReadBytes(source);
        if (bytes == null) {
            throw new IOException("Unsupported source type " + source.getClass().getName() + " for input " + input.getName());
        }
        return bytes;
    }

    private static Detection fatal(final String resourceId, final SAXParseException e) {
        return new Detection(ECTSeverity.FATAL_ERROR, CODE_NOT_WELLFORMED,
                new DetectionLocation(resourceId, e.getLineNumber(), e.getColumnNumber()), e.getMessage(), e);
    }

    /**
     * Collects every well-formedness error as a FATAL detection (one detection per parser error, with line/column)
     * instead of aborting on the first one.
     */
    private record CollectingErrorHandler(String resourceId, List<ICTDetection> errors) implements ErrorHandler {

        @Override
        public void warning(final SAXParseException e) {
            // well-formedness only: parser warnings do not affect the outcome of this step
        }

        @Override
        public void error(final SAXParseException e) {
            this.errors.add(fatal(this.resourceId, e));
        }

        @Override
        public void fatalError(final SAXParseException e) throws SAXException {
            this.errors.add(fatal(this.resourceId, e));
            throw e;
        }
    }
}
