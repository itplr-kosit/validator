package org.kosit.validator.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.function.Consumer;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.kosit.validator.api.xmlerror.XmlSeverity;
import org.kosit.validator.api.xmlerror.XmlSyntaxError;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import net.sf.saxon.s9api.Message;

/**
 * Collects error event information during schema validation and other XML-based actions.
 *
 * @author Andreas Penski
 */
public class CollectingErrorEventHandler implements ValidationEventHandler, ErrorHandler, ErrorListener, Consumer<Message> {

    private static final int DEFAULT_ABORT_COUNT = 50;

    private static final int STOP_PROCESS_COUNT = DEFAULT_ABORT_COUNT;

    private final Collection<XmlSyntaxError> errors = new ArrayList<>();

    private static XmlSyntaxError createError(final XmlSeverity severity, final String message) {
        final XmlSyntaxError e = new XmlSyntaxError();
        e.setSeverity(severity);
        e.setMessage(message);
        return e;
    }

    private static XmlSyntaxError createError(final XmlSeverity severity, final SAXParseException exception) {
        final XmlSyntaxError e = createError(severity, exception.getMessage());
        e.setRowNumber(Long.valueOf(exception.getLineNumber()));
        e.setColumnNumber(Long.valueOf(exception.getColumnNumber()));
        return e;
    }

    private static XmlSyntaxError createError(final XmlSeverity severity, final TransformerException exception) {
        final XmlSyntaxError e = createError(severity, exception.getMessage());
        if (exception.getLocator() != null) {
            e.setRowNumber(Long.valueOf(exception.getLocator().getLineNumber()));
            e.setColumnNumber(Long.valueOf(exception.getLocator().getColumnNumber()));
        }
        return e;
    }

    private static XmlSeverity translateSeverity(final int severity) {
        return switch (severity) {
            case ValidationEvent.WARNING -> XmlSeverity.WARNING;
            case ValidationEvent.ERROR -> XmlSeverity.ERROR;
            case ValidationEvent.FATAL_ERROR -> XmlSeverity.FATAL_ERROR;
            default -> throw new IllegalArgumentException("Unknown severity level " + severity);
        };
    }

    @Override
    public boolean handleEvent(final ValidationEvent event) {
        final XmlSyntaxError e = createError(translateSeverity(event.getSeverity()), event.getMessage());
        e.setColumnNumber(Long.valueOf(event.getLocator().getColumnNumber()));
        e.setRowNumber(Long.valueOf(event.getLocator().getLineNumber()));
        this.errors.add(e);
        return STOP_PROCESS_COUNT != this.errors.size();
    }

    /**
     * Indicates whether validation errors are present.
     *
     * @return true if at least one error is present.
     */
    public boolean hasErrors() {
        return hasEvents() && this.errors.stream().anyMatch(e -> e.getSeverity().isError());
    }

    /**
     * Indicates whether validation events occurred.
     *
     * @return true if at least one validation event has occurred
     */
    public boolean hasEvents() {
        return !this.errors.isEmpty();
    }

    @Override
    public void warning(final SAXParseException exception) throws SAXException {
        this.errors.add(createError(XmlSeverity.WARNING, exception));
    }

    @Override
    public void error(final SAXParseException exception) throws SAXException {
        this.errors.add(createError(XmlSeverity.ERROR, exception));
    }

    @Override
    public void fatalError(final SAXParseException exception) throws SAXException {
        this.errors.add(createError(XmlSeverity.FATAL_ERROR, exception));
    }

    @Override
    public void accept(final Message saxonMsg) {
        // public void message(final XdmNode content, final QName errorCode, final boolean terminate, final
        // SourceLocator locator) {
        final XmlSyntaxError e = new XmlSyntaxError();
        final var loc = saxonMsg.getLocation();
        if (loc != null) {
            if (loc.getLineNumber() >= 0)
                e.setRowNumber(Long.valueOf(loc.getLineNumber()));
            if (loc.getColumnNumber() >= 0)
                e.setColumnNumber(Long.valueOf(loc.getColumnNumber()));
        }
        e.setMessage("Error processing " + saxonMsg.getContent().getStringValue());
        e.setSeverity(saxonMsg.isTerminate() ? XmlSeverity.FATAL_ERROR : XmlSeverity.WARNING);
        this.errors.add(e);
    }

    @Override
    public void warning(final TransformerException exception) throws TransformerException {
        this.errors.add(createError(XmlSeverity.WARNING, exception));
    }

    @Override
    public void error(final TransformerException exception) throws TransformerException {
        this.errors.add(createError(XmlSeverity.ERROR, exception));
    }

    @Override
    public void fatalError(final TransformerException exception) throws TransformerException {
        this.errors.add(createError(XmlSeverity.FATAL_ERROR, exception));
    }

    public String getErrorDescription() {
        final StringJoiner joiner = new StringJoiner("\n");
        this.errors.forEach(e -> joiner.add(
                e.getSeverity().getLogPrefix() + " " + e.getMessage() + " At row " + e.getRowNumber() + " at pos " + e.getColumnNumber()));
        return joiner.toString();
    }

    public Collection<XmlSyntaxError> getErrors() {
        return this.errors;
    }
}
