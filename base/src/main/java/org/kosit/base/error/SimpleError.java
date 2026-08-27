package org.kosit.base.error;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Base interface for a single error
 * 
 * @author Philip Helger
 *
 */
public interface SimpleError {

    /**
     * @return The system ID of the object that contains the error
     */
    @Nullable
    String getSystemID();

    /**
     * Returns a line number from which the error originates.
     *
     * @return The line number. Value &le; 0 mean none available.
     */
    long getLineNumber();

    default boolean hasLineNumber() {
        return getLineNumber() > 0;
    }

    default @Nullable Long getLineNumberObj() {
        return hasLineNumber() ? Long.valueOf(getLineNumber()) : null;
    }

    /**
     * Returns a column number from which the error originates.
     *
     * @return The column number. Value &le; 0 mean none available.
     */
    long getColumnNumber();

    default boolean hasColumnNumber() {
        return getColumnNumber() > 0;
    }

    default @Nullable Long getColumnNumberObj() {
        return hasColumnNumber() ? Long.valueOf(getColumnNumber()) : null;
    }

    default boolean hasLineOrColumnNumber() {
        return hasLineNumber() || hasColumnNumber();
    }

    /**
     * 
     * @return The severity. May not be <code>null</code>.
     */
    @NonNull
    CTStandardSeverity getSeverity();

    /**
     * 
     * @return The error message. May not be <code>null</code>.
     */
    @NonNull
    String getMessage();

    /**
     * 
     * @return Optional exception linked to the error.
     */
    @Nullable
    Throwable getLinkedException();

    /**
     * 
     * @return <code>true</code> if a linked exception is present, <code>false</code> if not.
     */
    default boolean hasLinkedException() {
        return getLinkedException() != null;
    }

    /**
     * 
     * @return The error message including the location information as a single line. Never <code>null</code>.
     */
    @NonNull
    default String getAsString() {
        final StringBuilder ret = new StringBuilder(getMessage());
        if (hasLineNumber()) {
            if (hasColumnNumber())
                ret.append(" at line ").append(getLineNumber()).append(" at pos ").append(getColumnNumber());
            else
                ret.append(" at line ").append(getLineNumber());
        } else {
            if (hasColumnNumber())
                ret.append(" at line ? at pos ").append(getColumnNumber());
            // else: neither nor
        }
        return ret.toString();
    }

    /**
     * Log this error to the provided logger. Errors are logged on error level, everything else on warning level.
     *
     * @param logger the logger to log to. May not be <code>null</code>.
     */
    default void log(@NonNull final Logger logger) {
        if (getSeverity().isError()) {
            logger.error(getAsString());
        } else {
            logger.warn(getAsString());
        }
    }
}
