package org.kosit.validator.scenario.generic;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The kind of a single {@link Scenario}. Scenario configuration version 2 only knows {@link #XML} - version 3 also
 * knows {@link #PDF}.
 *
 * @author Philip Helger
 */
public enum EScenarioKind {

    /** A scenario that validates an XML document */
    XML("xml"),

    /** A scenario that validates a PDF document, delegating the embedded XML to a referenced {@link #XML} scenario */
    PDF("pdf");

    private final String id;

    EScenarioKind(@NonNull @Nonempty final String id) {
        this.id = id;
    }

    /**
     * @return the technical ID of this kind. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getID() {
        return this.id;
    }

    /**
     * @param id the ID to search. May be <code>null</code>.
     * @return <code>null</code> if no such kind exists.
     */
    public static @Nullable EScenarioKind getFromIDOrNull(final @Nullable String id) {
        for (final EScenarioKind e : values()) {
            if (e.id.equals(id)) {
                return e;
            }
        }
        return null;
    }
}
