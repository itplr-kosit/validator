package org.conformatron.api.model.validation;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;

/**
 * Enum with all predefined syntaxes.
 *
 * @author Philip Helger
 */
public enum CTSyntax {

    XML("xml", "XML"), JSON("json", "JSON"), EDIFACT("edifact", "EDIFACT"), PDF("pdf", "PDF"), OTHER("other", "Other");

    private final String id;

    private final String name;

    CTSyntax(@NonNull @Nonempty final String id, @NonNull @Nonempty final String name) {
        this.id = id;
        this.name = name;
    }

    @NonNull
    @Nonempty
    public String getID() {
        return id;
    }

    @NonNull
    @Nonempty
    public String getName() {
        return name;
    }

    public boolean isXML() {
        return this == XML;
    }
}
