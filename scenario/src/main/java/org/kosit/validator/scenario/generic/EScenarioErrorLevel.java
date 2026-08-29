package org.kosit.validator.scenario.generic;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The error level of a {@link ScenarioCustomErrorLevel}. The IDs are identical in both scenario configuration versions.
 *
 * @author Philip Helger
 */
public enum EScenarioErrorLevel {

    /** The finding is an error */
    ERROR("error"),

    /** The finding is a warning */
    WARNING("warning"),

    /** The finding is informational only */
    INFORMATION("information");

    private final String id;

    EScenarioErrorLevel(@NonNull @Nonempty final String id) {
        this.id = id;
    }

    /**
     * @return the ID as used in the XML representation. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getID() {
        return this.id;
    }

    /**
     * @param id the ID to search. May be <code>null</code>.
     * @return <code>null</code> if no such error level exists.
     */
    public static @Nullable EScenarioErrorLevel getFromIDOrNull(final @Nullable String id) {
        for (final EScenarioErrorLevel e : values()) {
            if (e.id.equals(id)) {
                return e;
            }
        }
        return null;
    }
}
