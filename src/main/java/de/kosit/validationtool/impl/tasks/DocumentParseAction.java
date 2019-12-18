/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.impl.tasks;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.ValidationResultsWellformedness;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;
import de.kosit.validationtool.model.reportInput.XMLSyntaxErrorSeverity;

import net.sf.saxon.s9api.DocumentBuilder;
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

    /**
     * Parsed und überprüft ein übergebenes Dokument darauf ob es well-formed ist. Dies stellt den ersten
     * Verarbeitungsschritt des Prüf-Tools dar. Diese Funktion verzichtet explizit auf die Validierung gegenüber einem
     * Schema.
     * 
     * @param content ein Dokument
     * @return Ergebnis des Parsings inklusive etwaiger Fehler
     */
    public static Result<XdmNode, XMLSyntaxError> parseDocument(final Input content) {
        if (content == null) {
            throw new IllegalArgumentException("Input may not be null");
        }
        Result<XdmNode, XMLSyntaxError> result;
        try {
            final DocumentBuilder builder = ObjectFactory.createProcessor().newDocumentBuilder();
            builder.setLineNumbering(true);
            final XdmNode doc = builder.build(content.getSource());
            result = new Result<>(doc, Collections.emptyList());
        } catch (final SaxonApiException | IOException e) {
            log.debug("Exception while parsing {}", content.getName(), e);
            final XMLSyntaxError error = new XMLSyntaxError();
            error.setSeverityCode(XMLSyntaxErrorSeverity.SEVERITY_FATAL_ERROR);
            error.setMessage(String.format("IOException while reading resource %s: %s", content.getName(), e.getMessage()));
            result = new Result<>(Collections.singleton(error));
        }

        return result;
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
