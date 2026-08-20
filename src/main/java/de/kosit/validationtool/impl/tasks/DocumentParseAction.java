/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.impl.input.XdmNodeInput;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.ValidationResultsWellformedness;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;
import de.kosit.validationtool.model.reportInput.XMLSyntaxErrorSeverity;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

/**
 * Implements parsing functionality. Checks for well-formedness.
 *
 * @author Andreas Penski
 */
public class DocumentParseAction implements CheckAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParseAction.class);

    private final Processor processor;

    /**
     * Parses and checks a given document for well-formedness. This is the first processing step of the validation tool.
     * This function explicitly omits validation against a schema.
     *
     * @param content a document
     * @return parsing result including any errors
     */
    public Result<XdmNode, XMLSyntaxError> parseDocument(final Input content) {
        if (content == null) {
            throw new IllegalArgumentException("Input may not be null");
        }
        Result<XdmNode, XMLSyntaxError> result;
        try {
            if (content instanceof XdmNodeInput && hasCompatibleConfiguration((XdmNodeInput) content)) {
                // parsing not neccessary
                result = new Result<>(((XdmNodeInput) content).getNode());
            } else {
                final DocumentBuilder builder = this.processor.newDocumentBuilder();
                builder.setLineNumbering(true);
                final XdmNode doc = builder.build(content.getSource());
                result = new Result<>(doc, Collections.emptyList());
            }
        } catch (final SaxonApiException | IOException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Exception while parsing {}", content.getName(), e);
            final XMLSyntaxError error = new XMLSyntaxError();
            error.setSeverityCode(XMLSyntaxErrorSeverity.SEVERITY_FATAL_ERROR);
            error.setMessage("IOException while reading resource " + content.getName() + ": " + e.getMessage());
            result = new Result<>(Arrays.asList(error));
        }
        return result;
    }

    private boolean hasCompatibleConfiguration(final XdmNodeInput content) {
        return content.getNode().getProcessor().getUnderlyingConfiguration().isCompatible(this.processor.getUnderlyingConfiguration());
    }

    @Override
    public void check(final Bag results) {
        final Result<XdmNode, XMLSyntaxError> parserResult = parseDocument(results.getInput());
        final ValidationResultsWellformedness v = new ValidationResultsWellformedness();
        results.setParserResult(parserResult);
        v.getXmlSyntaxError().addAll(parserResult.getErrors());
        results.getReportInput().setValidationResultsWellformedness(v);
        if (parserResult.isInvalid()) {
            results.stopProcessing(parserResult.getErrors().stream().map(XMLSyntaxError::getMessage).collect(Collectors.toList()));
        }
    }

    public DocumentParseAction(final Processor processor) {
        this.processor = processor;
    }
}
