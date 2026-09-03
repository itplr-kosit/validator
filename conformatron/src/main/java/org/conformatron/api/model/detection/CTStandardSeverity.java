package org.conformatron.api.model.detection;

import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.annotation.Nonnegative;
import org.jspecify.annotations.NonNull;

/**
 * Represents a generic severity specialised for the validation of data. This type of severity is not best matching for
 * e.g. application errors or log levels.
 */
public enum CTStandardSeverity implements CTSeverity {

    /** For objects without severity */
    NONE("none", 0),
    /** Warning level. */
    WARNING("warn", 100),
    /** Error level */
    ERROR("error", 200);

    /** Lowest error level within this enum */
    public static final CTStandardSeverity LOWEST = NONE;

    /** Highest error level within this enum */
    public static final CTStandardSeverity HIGHEST = ERROR;

    private final String id;

    private final int numericLevel;

    CTStandardSeverity(@NonNull @Nonempty final String id, @Nonnegative final int numericLevel) {
        this.id = id;
        this.numericLevel = numericLevel;
    }

    @NonNull
    @Nonempty
    public String getID() {
        return id;
    }

    @Nonnegative
    public int getNumericLevel() {
        return numericLevel;
    }

    public boolean isWarning() {
        return this == WARNING;
    }

    public boolean isError() {
        return this == ERROR;
    }

    @NonNull
    public static CTStandardSeverity from(final int nLevel) {
        // Check highest to lowest severity
        if (nLevel >= CTStandardSeverity.ERROR.getNumericLevel())
            return CTStandardSeverity.ERROR;

        if (nLevel >= CTStandardSeverity.WARNING.getNumericLevel())
            return CTStandardSeverity.WARNING;

        // Everything below info has no severity to us
        return CTStandardSeverity.NONE;
    }

    @NonNull
    public static CTStandardSeverity of(final java.util.logging.@NonNull Level aLevel) {
        final int nLevel = aLevel.intValue();

        // java.util.logging has no "fatal" — map SEVERE to ERROR
        if (nLevel >= java.util.logging.Level.SEVERE.intValue() && nLevel != java.util.logging.Level.OFF.intValue())
            return CTStandardSeverity.ERROR;

        if (nLevel >= java.util.logging.Level.WARNING.intValue())
            return CTStandardSeverity.WARNING;

        // Everything below info has no severity to us
        return CTStandardSeverity.NONE;
    }
}
