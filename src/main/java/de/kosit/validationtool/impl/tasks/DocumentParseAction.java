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

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
 * Setzt Parsing-Funktionalitäten um. Prüft auf well-formedness
 * 
 * @author Andreas Penski
 */
@Slf4j
@RequiredArgsConstructor
public class DocumentParseAction implements CheckAction {

    private final Processor processor;

    /**
     * Parsed und überprüft ein übergebenes Dokument darauf ob es well-formed ist. Dies stellt den ersten
     * Verarbeitungsschritt des Prüf-Tools dar. Diese Funktion verzichtet explizit auf die Validierung gegenüber einem
     * Schema.
     * 
     * @param content ein Dokument
     * @return Ergebnis des Parsings inklusive etwaiger Fehler
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
            log.debug("Exception while parsing {}", content.getName(), e);
            final XMLSyntaxError error = new XMLSyntaxError();
            error.setSeverityCode(XMLSyntaxErrorSeverity.SEVERITY_FATAL_ERROR);
            error.setMessage(String.format("IOException while reading resource %s: %s", content.getName(), e.getMessage()));
            result = new Result<>(Collections.singleton(error));
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

}
