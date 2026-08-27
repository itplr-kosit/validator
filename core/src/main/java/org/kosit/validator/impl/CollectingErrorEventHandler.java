package org.kosit.validator.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.function.Consumer;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.kosit.base.error.DefaultSimpleError;
import org.kosit.base.error.SimpleError;
import org.kosit.base.error.SimpleErrorBuilder;
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

    private final Collection<SimpleError> errors = new ArrayList<>();

    private static SimpleError createSaxError(final CTStandardSeverity severity, final SAXParseException exception) {
        return DefaultSimpleError.builder().severity(severity).message(exception.getMessage()).location(exception)
                .linkedException(exception).build();
    }

    private static SimpleError createTransformError(final CTStandardSeverity severity, final TransformerException exception) {
        return DefaultSimpleError.builder().severity(severity).message(exception.getMessage()).location(exception.getLocator())
                .linkedException(exception).build();
    }

    private static CTStandardSeverity translateJaxbSeverity(final int severity) {
        return switch (severity) {
            case ValidationEvent.WARNING -> CTStandardSeverity.WARNING;
            case ValidationEvent.ERROR, ValidationEvent.FATAL_ERROR -> CTStandardSeverity.ERROR;
            default -> throw new IllegalArgumentException("Unknown severity level " + severity);
        };
    }

    @Override
    public boolean handleEvent(final ValidationEvent event) {
        final SimpleError e = DefaultSimpleError.builder().severity(translateJaxbSeverity(event.getSeverity())).message(event.getMessage())
                .location(event.getLocator().getURL(), event.getLocator().getLineNumber(), event.getLocator().getColumnNumber())
                .linkedException(event.getLinkedException()).build();
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
        this.errors.add(createSaxError(CTStandardSeverity.WARNING, exception));
    }

    @Override
    public void error(final SAXParseException exception) throws SAXException {
        this.errors.add(createSaxError(CTStandardSeverity.ERROR, exception));
    }

    @Override
    public void fatalError(final SAXParseException exception) throws SAXException {
        this.errors.add(createSaxError(CTStandardSeverity.ERROR, exception));
    }

    @Override
    public void accept(final Message saxonMsg) {
        // public void message(final XdmNode content, final QName errorCode, final boolean terminate, final
        // SourceLocator locator) {
        final SimpleError e = new SimpleErrorBuilder()
                .severity(saxonMsg.isTerminate() ? CTStandardSeverity.ERROR : CTStandardSeverity.WARNING)
                .location(saxonMsg.getLocation().getSystemId(), saxonMsg.getLocation().getLineNumber(),
                        saxonMsg.getLocation().getColumnNumber())
                .message("Error processing " + saxonMsg.getContent().getStringValue()).build();
        this.errors.add(e);
    }

    @Override
    public void warning(final TransformerException exception) throws TransformerException {
        this.errors.add(createTransformError(CTStandardSeverity.WARNING, exception));
    }

    @Override
    public void error(final TransformerException exception) throws TransformerException {
        this.errors.add(createTransformError(CTStandardSeverity.ERROR, exception));
    }

    @Override
    public void fatalError(final TransformerException exception) throws TransformerException {
        this.errors.add(createTransformError(CTStandardSeverity.ERROR, exception));
    }

    public String getErrorDescription() {

        final StringJoiner joiner = new StringJoiner("\n");
        this.errors.forEach(e -> joiner.add((e.getSeverity().isError() ? "[ERROR] " : e.getSeverity().isWarning() ? "[WARN] " : "")
                + e.getMessage() + " At line " + e.getLineNumber() + " at pos " + e.getColumnNumber()));
        return joiner.toString();
    }

    public Collection<SimpleError> getErrors() {
        return this.errors;
    }
}
