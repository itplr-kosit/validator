package org.conformatron.api.model.validation;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;

/**
 * Enum with all predefined validation standards.
 *
 * @author Philip Helger
 */
public enum CTValidationStandard {

    XML("xml", "XML"), XSD("xsd", "XML Schema"), SCHEMATRON("sch", "Schematron"), EDIFACT("edifact", "EDIFACT"), PDF("pdf",
            "PDF"), OTHER("other", "Other");

    private final String id;

    private final String name;

    CTValidationStandard(@NonNull @Nonempty final String id, @NonNull @Nonempty final String name) {
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

    public boolean isXSD() {
        return this == XSD;
    }

    public boolean isSchematron() {
        return this == SCHEMATRON;
    }

    public boolean isEdifact() {
        return this == EDIFACT;
    }
}
