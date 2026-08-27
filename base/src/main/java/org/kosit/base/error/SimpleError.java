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
     * @return The object in which the error was found
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
     * @return The mandatory severity. May not be <code>null</code>.
     */
    @NonNull
    CTStandardSeverity getSeverity();

    /**
     * 
     * @return The main error message. May not be <code>null</code>.
     */
    @NonNull
    String getMessage();

    @Nullable
    Exception getLinkedException();

    default boolean hasLinkedException() {
        return getLinkedException() != null;
    }
}
