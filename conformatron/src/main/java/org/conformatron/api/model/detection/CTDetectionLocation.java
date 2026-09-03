package org.conformatron.api.model.detection;

import org.conformatron.api.annotation.CheckForSigned;
import org.jspecify.annotations.Nullable;

/**
 * Interface indication the position of something within a single resource
 */
public interface CTDetectionLocation {

    /** Constant for an illegal row or column number */
    int ILLEGAL_NUMBER = -1;

    /**
     * @return The ID of the resource where the detection occurred. May be <code>null</code>.
     */
    @Nullable
    String getResourceId();

    /**
     * @return The 1-based line number {@link #ILLEGAL_NUMBER} if no line number is present.
     */
    @CheckForSigned
    int getLineNumber();

    default boolean hasLineNumber() {
        return getLineNumber() != ILLEGAL_NUMBER;
    }

    /**
     * @return The 1-based column number {@link #ILLEGAL_NUMBER} if no column number is present.
     */
    @CheckForSigned
    int getColumnNumber();

    default boolean hasColumnNumber() {
        return getColumnNumber() != ILLEGAL_NUMBER;
    }
}
