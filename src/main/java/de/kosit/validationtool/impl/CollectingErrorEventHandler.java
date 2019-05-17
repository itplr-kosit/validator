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

package de.kosit.validationtool.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import lombok.Getter;

import de.kosit.validationtool.model.reportInput.XMLSyntaxError;
import de.kosit.validationtool.model.reportInput.XMLSyntaxErrorSeverity;

import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.XdmNode;

/**
 * Sammelt Fehler-Ereignisinformation beim Schema-Validieren und weiteren XML-basierten Aktionen
 *
 * @author Andreas Penski
 */
@Getter
public class CollectingErrorEventHandler implements ValidationEventHandler, ErrorHandler, MessageListener, ErrorListener {

    private static final int DEFAULT_ABORT_COUNT = 50;

    private final Collection<XMLSyntaxError> errors = new ArrayList<>();

    private final int stopProcessCount = DEFAULT_ABORT_COUNT;

    private static XMLSyntaxError createError(final XMLSyntaxErrorSeverity severity, final String message) {
        final XMLSyntaxError e = new XMLSyntaxError();
        e.setSeverityCode(severity);
        e.setMessage(message);
        return e;
    }

    private static XMLSyntaxError createError(final XMLSyntaxErrorSeverity severity, final SAXParseException exception) {
        final XMLSyntaxError e = createError(severity, exception.getMessage());
        e.setRowNumber(exception.getLineNumber());
        e.setColumnNumber(exception.getColumnNumber());
        return e;
    }

    private static XMLSyntaxError createError(final XMLSyntaxErrorSeverity severity, final TransformerException exception) {
        final XMLSyntaxError e = createError(severity, exception.getMessage());
        if (exception.getLocator() != null) {
            e.setRowNumber(exception.getLocator().getLineNumber());
            e.setColumnNumber(exception.getLocator().getColumnNumber());
        }
        return e;
    }

    private static XMLSyntaxErrorSeverity translateSeverity(final int severity) {
        switch (severity) {
            case ValidationEvent.WARNING:
                return XMLSyntaxErrorSeverity.SEVERITY_WARNING;
            case ValidationEvent.ERROR:
                return XMLSyntaxErrorSeverity.SEVERITY_ERROR;
            case ValidationEvent.FATAL_ERROR:
                return XMLSyntaxErrorSeverity.SEVERITY_FATAL_ERROR;
            default:
                throw new IllegalArgumentException("Unknown severity level " + severity);
        }
    }

    @Override
    public boolean handleEvent(final ValidationEvent event) {
        final XMLSyntaxError e = createError(translateSeverity(event.getSeverity()), event.getMessage());
        e.setColumnNumber(event.getLocator().getColumnNumber());
        e.setRowNumber(event.getLocator().getLineNumber());
        this.errors.add(e);
        return this.stopProcessCount != this.errors.size();
    }

    /**
     * Zeigt an, ob Validierungsfehler vorhanden sind.
     * 
     * @return true wenn mindestens ein Fehler vorhanden ist.
     */
    public boolean hasErrors() {
        return hasEvents() && this.errors.stream().anyMatch(e -> e.getSeverityCode() != XMLSyntaxErrorSeverity.SEVERITY_WARNING);
    }

    /**
     * Zeigt an, ob es Validierungs-Ereignisse gab.
     * 
     * @return true wenn mindestens ein Validierungsereignis aufgetreten ist
     */
    public boolean hasEvents() {
        return !this.errors.isEmpty();
    }

    @Override
    public void warning(final SAXParseException exception) throws SAXException {
        this.errors.add(createError(XMLSyntaxErrorSeverity.SEVERITY_WARNING, exception));
    }

    @Override
    public void error(final SAXParseException exception) throws SAXException {
        this.errors.add(createError(XMLSyntaxErrorSeverity.SEVERITY_ERROR, exception));
    }

    @Override
    public void fatalError(final SAXParseException exception) throws SAXException {
        this.errors.add(createError(XMLSyntaxErrorSeverity.SEVERITY_FATAL_ERROR, exception));
    }

    @Override
    public void message(final XdmNode content, final boolean terminate, final SourceLocator locator) {
        final XMLSyntaxError e = new XMLSyntaxError();
        if (locator != null) {
            e.setColumnNumber(locator.getColumnNumber());
            e.setRowNumber(locator.getLineNumber());
        }
        e.setMessage("Error procesing" + content.getStringValue());
        e.setSeverityCode(terminate ? XMLSyntaxErrorSeverity.SEVERITY_FATAL_ERROR : XMLSyntaxErrorSeverity.SEVERITY_WARNING);
    }

    @Override
    public void warning(final TransformerException exception) throws TransformerException {
        this.errors.add(createError(XMLSyntaxErrorSeverity.SEVERITY_WARNING, exception));
    }

    @Override
    public void error(final TransformerException exception) throws TransformerException {
        this.errors.add(createError(XMLSyntaxErrorSeverity.SEVERITY_ERROR, exception));
    }

    @Override
    public void fatalError(final TransformerException exception) throws TransformerException {
        this.errors.add(createError(XMLSyntaxErrorSeverity.SEVERITY_FATAL_ERROR, exception));
    }

    public String getErrorDescription() {
        final StringJoiner joiner = new StringJoiner("\n");
        this.errors.forEach(e -> joiner
                .add(e.getSeverityCode().value() + " " + e.getMessage() + " At row " + e.getRowNumber() + " at pos "
                        + e.getColumnNumber()));
        return joiner.toString();
    }
}