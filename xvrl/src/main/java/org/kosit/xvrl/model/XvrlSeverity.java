package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;

/**
 * The severity of a single {@link XvrlDetection}.
 *
 * <p>
 * The declaration order is the one of the underlying XVRL schema and is deliberately kept, because
 * {@link Enum#ordinal()} is used to determine the worse of two severities.
 *
 * @author Philip Helger
 */
public enum XvrlSeverity {

    INFO("info"),

    WARNING("warning"),

    ERROR("error"),

    FATAL_ERROR("fatal-error"),

    UNSPECIFIED("unspecified");

    private final String id;

    XvrlSeverity(final String id) {
        this.id = id;
    }

    /**
     * @return The XVRL token of this severity as it is used in the XML representation. Never <code>null</code>.
     */
    public String getID() {
        return this.id;
    }

    /**
     * @return <code>true</code> if this severity denotes an error or a fatal error.
     */
    public boolean isError() {
        return this == ERROR || this == FATAL_ERROR;
    }

    /**
     * @param id the XVRL token to resolve. May be <code>null</code>.
     * @return the matching severity or <code>null</code> if the token is unknown.
     */
    public static @Nullable XvrlSeverity getFromIDOrNull(final @Nullable String id) {
        return getFromIDOrDefault(id, null);
    }

    /**
     * @param id the XVRL token to resolve. May be <code>null</code>.
     * @param fallback the value to return if the token is unknown. May be <code>null</code>.
     * @return the matching severity or the provided fallback if the token is unknown.
     */
    public static @Nullable XvrlSeverity getFromIDOrDefault(final @Nullable String id, final @Nullable XvrlSeverity fallback) {
        if (id != null)
            for (final XvrlSeverity e : values())
                if (e.id.equals(id))
                    return e;
        return fallback;
    }
}
