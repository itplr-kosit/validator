package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;

/**
 * The worst detection severity summarized in an {@link XvrlDigest}.
 *
 * @author Philip Helger
 */
public enum XvrlWorst {

    FATAL_ERROR("fatal-error"),

    ERROR("error"),

    WARNING("warning"),

    INFO("info"),

    NOTHING("nothing"),

    UNSPECIFIED("unspecified");

    private final String id;

    XvrlWorst(final String id) {
        this.id = id;
    }

    /**
     * @return The XVRL token of this value as it is used in the XML representation. Never <code>null</code>.
     */
    public String getID() {
        return this.id;
    }

    /**
     * @param id the XVRL token to resolve. May be <code>null</code>.
     * @return the matching value or <code>null</code> if the token is unknown.
     */
    public static @Nullable XvrlWorst getFromIDOrNull(final @Nullable String id) {
        return getFromIDOrDefault(id, null);
    }

    /**
     * @param id the XVRL token to resolve. May be <code>null</code>.
     * @param fallback the value to return if the token is unknown. May be <code>null</code>.
     * @return the matching value or the provided fallback if the token is unknown.
     */
    public static @Nullable XvrlWorst getFromIDOrDefault(final @Nullable String id, final @Nullable XvrlWorst fallback) {
        if (id != null)
            for (final XvrlWorst e : values())
                if (e.id.equals(id))
                    return e;
        return fallback;
    }
}
