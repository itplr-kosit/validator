package org.conformatron.api.model.detection;

import org.conformatron.api.annotation.CheckForSigned;
import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.annotation.Nonnegative;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Interface representing a single error level.
 */
public interface CTSeverity {

    @NonNull
    @Nonempty
    String getID();

    /**
     * @return The numeric level of this error level. Must be &ge; 0. The higher the numeric level, the higher the
     *         priority of the error level. So e.g. ERROR has a higher/larger/greater numerical level than WARNING.
     */
    @Nonnegative
    int getNumericLevel();

    /**
     * @return {@code true} if this severity is <code>WARNING</code>.
     */
    default boolean isWarning() {
        return getNumericLevel() == CTStandardSeverity.WARNING.getNumericLevel();
    }

    /**
     * @return {@code true} if this severity is <code>ERROR</code>.
     */
    default boolean isError() {
        return getNumericLevel() == CTStandardSeverity.ERROR.getNumericLevel();
    }

    @CheckForSigned
    default int compareTo(@NonNull final CTSeverity aErrorLevel) {
        return Integer.compare(getNumericLevel(), aErrorLevel.getNumericLevel());
    }

    @Nullable
    static CTSeverity getWorst(@Nullable final CTSeverity aLevel1, @Nullable final CTSeverity aLevel2) {
        // Identity equals is okay
        if (aLevel1 == aLevel2)
            return aLevel1;
        if (aLevel1 == null)
            return aLevel2;
        if (aLevel2 == null)
            return aLevel1;
        return aLevel1.compareTo(aLevel2) > 0 ? aLevel1 : aLevel2;
    }
}
