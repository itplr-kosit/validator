package org.kosit.validator.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.function.Consumer;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.kosit.validator.model.XmlSyntaxError;
import org.kosit.validator.model.XmlSyntaxErrorSeverity;
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

    private static XmlSyntaxError createError(final XmlSyntaxErrorSeverity severity, final String message) {
        final XmlSyntaxError e = new XmlSyntaxError();
        e.setSeverityCode(severity);
        e.setMessage(message);
        return e;
    }

    private static XmlSyntaxError createError(final XmlSyntaxErrorSeverity severity, final SAXParseException exception) {
        final XmlSyntaxError e = createError(severity, exception.getMessage());
        e.setRowNumber(Long.valueOf(exception.getLineNumber()));
        e.setColumnNumber(Long.valueOf(exception.getColumnNumber()));
        return e;
    }

    private static XmlSyntaxError createError(final XmlSyntaxErrorSeverity severity, final TransformerException exception) {
        final XmlSyntaxError e = createError(severity, exception.getMessage());
        if (exception.getLocator() != null) {
            e.setRowNumber(Long.valueOf(exception.getLocator().getLineNumber()));
            e.setColumnNumber(Long.valueOf(exception.getLocator().getColumnNumber()));
        }
        return e;
    }

    private static XmlSyntaxErrorSeverity translateSeverity(final int severity) {
        return switch (severity) {
            case ValidationEvent.WARNING -> XmlSyntaxErrorSeverity.SEVERITY_WARNING;
            case ValidationEvent.ERROR -> XmlSyntaxErrorSeverity.SEVERITY_ERROR;
            case ValidationEvent.FATAL_ERROR -> XmlSyntaxErrorSeverity.SEVERITY_FATAL_ERROR;
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
        return hasEvents() && this.errors.stream().anyMatch(e -> e.getSeverityCode() != XmlSyntaxErrorSeverity.SEVERITY_WARNING);
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
        this.errors.add(createError(XmlSyntaxErrorSeverity.SEVERITY_WARNING, exception));
    }

    @Override
    public void error(final SAXParseException exception) throws SAXException {
        this.errors.add(createError(XmlSyntaxErrorSeverity.SEVERITY_ERROR, exception));
    }

    @Override
    public void fatalError(final SAXParseException exception) throws SAXException {
        this.errors.add(createError(XmlSyntaxErrorSeverity.SEVERITY_FATAL_ERROR, exception));
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
        e.setSeverityCode(saxonMsg.isTerminate() ? XmlSyntaxErrorSeverity.SEVERITY_FATAL_ERROR : XmlSyntaxErrorSeverity.SEVERITY_WARNING);
        this.errors.add(e);
    }

    @Override
    public void warning(final TransformerException exception) throws TransformerException {
        this.errors.add(createError(XmlSyntaxErrorSeverity.SEVERITY_WARNING, exception));
    }

    @Override
    public void error(final TransformerException exception) throws TransformerException {
        this.errors.add(createError(XmlSyntaxErrorSeverity.SEVERITY_ERROR, exception));
    }

    @Override
    public void fatalError(final TransformerException exception) throws TransformerException {
        this.errors.add(createError(XmlSyntaxErrorSeverity.SEVERITY_FATAL_ERROR, exception));
    }

    public String getErrorDescription() {
        final StringJoiner joiner = new StringJoiner("\n");
        this.errors.forEach(e -> joiner.add(
                e.getSeverityCode().value() + " " + e.getMessage() + " At row " + e.getRowNumber() + " at pos " + e.getColumnNumber()));
        return joiner.toString();
    }

    public Collection<XmlSyntaxError> getErrors() {
        return this.errors;
    }
}
