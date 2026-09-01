package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;

/**
 * The validity state of an {@link XvrlDigest}.
 *
 * @author Philip Helger
 */
public enum XvrlValidity {

    TRUE("true"),

    FALSE("false"),

    PARTIAL("partial"),

    UNDETERMINED("undetermined");

    private final String id;

    XvrlValidity(final String id) {
        this.id = id;
    }

    /**
     * @return The XVRL token of this validity as it is used in the XML representation. Never <code>null</code>.
     */
    public String getID() {
        return this.id;
    }

    /**
     * @param id the XVRL token to resolve. May be <code>null</code>.
     * @return the matching validity or <code>null</code> if the token is unknown.
     */
    public static @Nullable XvrlValidity getFromIDOrNull(final @Nullable String id) {
        return getFromIDOrDefault(id, null);
    }

    /**
     * @param id the XVRL token to resolve. May be <code>null</code>.
     * @param fallback the value to return if the token is unknown. May be <code>null</code>.
     * @return the matching validity or the provided fallback if the token is unknown.
     */
    public static @Nullable XvrlValidity getFromIDOrDefault(final @Nullable String id, final @Nullable XvrlValidity fallback) {
        if (id != null)
            for (final XvrlValidity e : values())
                if (e.id.equals(id))
                    return e;
        return fallback;
    }
}
