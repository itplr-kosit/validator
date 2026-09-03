package org.conformatron.api.model.validation;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Enum with all predefined validation types. Depending on this type, different implementation logic must be used!
 */
public enum CTStandardValidationType implements CTValidationType {

    /**
     * Validate XML syntax by parsing without assigned XSDs. This is the wellformedness check.
     */
    XML("xml", CTValidationStandard.XML, "XML Syntax", null),
    /** Validate XML against the rules of an XML Schema (XSD) */
    XSD("xsd", CTValidationStandard.XSD, "XML Schema", null),
    /**
     * Validate part of an XML against the rules of an XML Schema (XSD) - e.g. for extension/plugins. The context object
     * needed for this type is an <code>ValidationExecutorXSDPartial.ContextData</code>.
     */
    PARTIAL_XSD("partial-xsd", CTValidationStandard.XSD, "Partial XML Schema", null),

    /**
     * Pure Java implementation of Schematron - can only handle XPath 2
     */
    SCHEMATRON_PURE_XPATH2("schematron-pure-xpath2", CTValidationStandard.SCHEMATRON, "Schematron (pure; XPath 2.0)",
            CTSchematronEngine.PURE_XPATH),

    /**
     * Pure Java implementation of Schematron to XSLT 1.0 converter.
     */
    SCHEMATRON_PURE_XSLT1("schematron-pure-xslt1", CTValidationStandard.SCHEMATRON, "Schematron (pure; XSLT 1.0)",
            CTSchematronEngine.PURE_XSLT),

    /**
     * Pure Java implementation of Schematron to XSLT 2.0 converter.
     */
    SCHEMATRON_PURE_XSLT2("schematron-pure-xslt2", CTValidationStandard.SCHEMATRON, "Schematron (pure; XSLT 2.0)",
            CTSchematronEngine.PURE_XSLT),

    /**
     * Pure Java implementation of Schematron to XSLT 3.0 converter.
     */
    SCHEMATRON_PURE_XSLT3("schematron-pure-xslt3", CTValidationStandard.SCHEMATRON, "Schematron (pure; XSLT 3.0)",
            CTSchematronEngine.PURE_XSLT),

    /**
     * Convert Schematron to XSLT using ISO Schematron with XSLT 1.0 output
     */
    SCHEMATRON_SCH_ISO_XSLT1("schematron-sch-xslt1", CTValidationStandard.SCHEMATRON, "Schematron (ISO Schematron; XSLT 1.0)",
            CTSchematronEngine.ISO_SCHEMATRON),

    /**
     * Convert Schematron to XSLT using ISO Schematron with XSLT 2.0 output.
     */
    SCHEMATRON_SCH_ISO_XSLT2("schematron-sch-xslt2", CTValidationStandard.SCHEMATRON, "Schematron (ISO Schematron; XSLT 2.0)",
            CTSchematronEngine.ISO_SCHEMATRON),

    /**
     * Convert Schematron to XSLT using SchXslt1 with XSLT 1.0 output
     */
    SCHEMATRON_SCHXSLT1_XSLT1("schematron-schxslt-xslt1", CTValidationStandard.SCHEMATRON, "Schematron (SchXslt1; XSLT 1.0)",
            CTSchematronEngine.SCHXSLT1),

    /**
     * Convert Schematron to XSLT using SchXslt1 with XSLT 2.0 output
     */
    SCHEMATRON_SCHXSLT1_XSLT2("schematron-schxslt-xslt2", CTValidationStandard.SCHEMATRON, "Schematron (SchXslt1; XSLT 2.0)",
            CTSchematronEngine.SCHXSLT1),

    /**
     * Convert Schematron to XSLT using SchXslt2 with XSLT 3.0 output
     */
    SCHEMATRON_SCHXSLT2_XSLT3("schematron-schxslt2-xslt3", CTValidationStandard.SCHEMATRON, "Schematron (SchXslt2; XSLT 3.0)",
            CTSchematronEngine.SCHXSLT2),

    /**
     * Schematron validation with a pre-build XSLT v1 file
     */
    SCHEMATRON_XSLT1("schematron-xslt1", CTValidationStandard.SCHEMATRON, "Schematron (XSLT 1.0)", CTSchematronEngine.XSLT_PREBUILT),

    /**
     * Schematron validation with a pre-build XSLT v2 file
     */
    SCHEMATRON_XSLT2("schematron-xslt2", CTValidationStandard.SCHEMATRON, "Schematron (XSLT 2.0)", CTSchematronEngine.XSLT_PREBUILT),

    /**
     * Schematron validation with a pre-build XSLT v3 file
     */
    SCHEMATRON_XSLT3("schematron-xslt3", CTValidationStandard.SCHEMATRON, "Schematron (XSLT 3.0)", CTSchematronEngine.XSLT_PREBUILT);

    private final String id;

    private final CTValidationStandard standard;

    private final String name;

    private final CTSchematronEngine schematronEngine;

    CTStandardValidationType(@NonNull @Nonempty final String id, @NonNull final CTValidationStandard standard,
            @NonNull @Nonempty final String name, @Nullable final CTSchematronEngine schematronEngine) {
        this.id = id;
        this.standard = standard;
        this.name = name;
        this.schematronEngine = schematronEngine;
    }

    @NonNull
    @Nonempty
    public String getID() {
        return id;
    }

    @NonNull
    public CTValidationStandard getStandard() {
        return standard;
    }

    @NonNull
    @Nonempty
    public String getName() {
        return name;
    }

    public boolean isStopValidationOnError() {
        return standard.isXML() || standard.isXSD();
    }

    public boolean isContextRequired() {
        return this == PARTIAL_XSD;
    }

    @Nullable
    public CTSchematronEngine getSchematronEngine() {
        return schematronEngine;
    }
}
