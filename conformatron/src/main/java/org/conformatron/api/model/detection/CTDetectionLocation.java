package org.conformatron.api.model.detection;

import org.conformatron.api.annotation.CheckForSigned;
import org.jspecify.annotations.NonNull;
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

    default boolean hasResourceId() {
        final var s = getResourceId();
        return s != null && !s.isEmpty();
    }

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

    /**
     * Simple method to check if resource ID, line number, column number or field name is present.
     *
     * @return <code>true</code> if at least one field is set, <code>false</code> otherwise.
     */
    default boolean hasAnyInformation() {
        return hasResourceId() || hasLineNumber() || hasColumnNumber();
    }

    @NonNull
    default String getAsString() {
        final StringBuilder ret = new StringBuilder();

        if (hasResourceId())
            ret.append(getResourceId());

        if (hasLineNumber()) {
            if (hasColumnNumber())
                ret.append("(").append(getLineNumber()).append(":").append(getColumnNumber()).append(")");
            else
                ret.append("(").append(getLineNumber()).append(":?)");
        } else {
            if (hasColumnNumber())
                ret.append("(?:").append(getColumnNumber()).append(")");
            // else: neither nor
        }
        return ret.toString();
    }
}
