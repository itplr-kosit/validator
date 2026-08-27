package org.kosit.base.error;

import java.util.Objects;

import javax.xml.stream.Location;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * The default builder to build {@link DefaultSimpleError} instances.
 *
 * @author Philip Helger
 */
public class SimpleErrorBuilder {

    /** The severity that is used, if none is provided explicitly */
    public static final CTStandardSeverity DEFAULT_SEVERITY = CTStandardSeverity.ERROR;

    private @Nullable String systemId;

    private long lineNumber;

    private long columnNumber;

    private CTStandardSeverity severity = DEFAULT_SEVERITY;

    private @Nullable String message;

    private @Nullable Exception linkedException;

    /**
     * Default constructor using the default severity {@link #DEFAULT_SEVERITY}.
     */
    public SimpleErrorBuilder() {
    }

    /**
     * Constructor copying the data from an existing error.
     *
     * @param error the error to copy the data from. May not be <code>null</code>.
     */
    public SimpleErrorBuilder(@NonNull final SimpleError error) {
        Objects.requireNonNull(error, "Error must not be null");
        systemId(error.getSystemID());
        lineNumber(error.getLineNumber());
        columnNumber(error.getColumnNumber());
        severity(error.getSeverity());
        message(error.getMessage());
        linkedException(error.getLinkedException());
    }

    /**
     * Set the object in which the error was found.
     *
     * @param systemId the error source. May be <code>null</code>.
     * @return this for chaining
     */
    @NonNull
    public SimpleErrorBuilder systemId(final @Nullable String systemId) {
        this.systemId = systemId;
        return this;
    }

    /**
     * Set the line number from which the error originates.
     *
     * @param lineNumber the line number. Values &le; 0 mean none available.
     * @return this for chaining
     */
    @NonNull
    public SimpleErrorBuilder lineNumber(final long lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }

    /**
     * Set the column number from which the error originates.
     *
     * @param columnNumber the column number. Values &le; 0 mean none available.
     * @return this for chaining
     */
    @NonNull
    public SimpleErrorBuilder columnNumber(final long columnNumber) {
        this.columnNumber = columnNumber;
        return this;
    }

    /**
     * Set the error source, the line number and the column number from a SAX locator.
     *
     * @param locator the SAX locator to use. May be <code>null</code>.
     * @return this for chaining
     */
    @NonNull
    public SimpleErrorBuilder location(final @Nullable Locator locator) {
        return locator == null ? location(null, 0, 0) : location(locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
    }

    /**
     * Set the error source, the line number and the column number from a SAX parse exception.
     *
     * @param ex the SAX parse exception to extract the location from. May be <code>null</code>.
     * @return this for chaining
     */
    @NonNull
    public SimpleErrorBuilder location(final @Nullable SAXParseException ex) {
        return ex == null ? location(null, 0, 0) : location(ex.getSystemId(), ex.getLineNumber(), ex.getColumnNumber());
    }

    /**
     * Set the error source, the line number and the column number from a StAX location.
     *
     * @param location the StAX location to use. May be <code>null</code>.
     * @return this for chaining
     */
    @NonNull
    public SimpleErrorBuilder location(final @Nullable Location location) {
        return location == null ? location(null, 0, 0)
                : location(location.getSystemId(), location.getLineNumber(), location.getColumnNumber());
    }

    /**
     * Set the error source, the line number and the column number at once.
     *
     * @param systemId the error source. May be <code>null</code>.
     * @param lineNumber the line number. Values &le; 0 mean none available.
     * @param columnNumber the column number. Values &le; 0 mean none available.
     * @return this for chaining
     */
    @NonNull
    public SimpleErrorBuilder location(final @Nullable String systemId, final long lineNumber, final long columnNumber) {
        return systemId(systemId).lineNumber(lineNumber).columnNumber(columnNumber);
    }

    /**
     * Set the severity of the error.
     *
     * @param severity the severity to use. May not be <code>null</code>.
     * @return this for chaining
     */
    @NonNull
    public SimpleErrorBuilder severity(@NonNull final CTStandardSeverity severity) {
        this.severity = Objects.requireNonNull(severity, "Severity must not be null");
        return this;
    }

    /**
     * Set the main error message.
     *
     * @param message the error message. May be <code>null</code>, but must be set before {@link #build()} is called.
     * @return this for chaining
     */
    @NonNull
    public SimpleErrorBuilder message(final @Nullable String message) {
        this.message = message;
        return this;
    }

    /**
     * Set the exception that caused this error.
     *
     * @param linkedException the linked exception. May be <code>null</code>.
     * @return this for chaining
     */
    @NonNull
    public SimpleErrorBuilder linkedException(final @Nullable Exception linkedException) {
        this.linkedException = linkedException;
        return this;
    }

    /**
     * Build the final immutable error object.
     *
     * @return the created error. Never <code>null</code>.
     * @throws IllegalStateException if a mandatory field is missing
     */
    @NonNull
    public DefaultSimpleError build() {
        if (this.message == null) {
            throw new IllegalStateException("The message must be provided");
        }
        return new DefaultSimpleError(this.systemId, this.lineNumber, this.columnNumber, this.severity, this.message,
                this.linkedException);
    }
}
