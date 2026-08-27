package org.kosit.base.error;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    /**
     * Returns a column number from which the error originates.
     *
     * @return The column number. Value &le; 0 mean none available.
     */
    long getColumnNumber();

    default boolean hasColumnNumber() {
        return getColumnNumber() > 0;
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
    Exception getLinkedException();

    /**
     * 
     * @return <code>true</code> if a linked exception is present, <code>false</code> if not.
     */
    default boolean hasLinkedException() {
        return getLinkedException() != null;
    }
}
