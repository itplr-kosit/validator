package org.conformatron.api.model.detection;

import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Defines a potentially multilingual detection text.
 */
public interface CTDetectionText {

    /**
     * @return All locales supported by this error text. May not be <code>null</code> but maybe empty.
     */
    @NonNull
    List<@NonNull Locale> getAllLocales();

    /**
     * @param aContentLocale The locale to be used for resolving. May not be <code>null</code>.
     * @return The display text of the object in the given locale. May be <code>null</code> if the text could not be
     *         resolved in the passed locale.
     */
    @Nullable
    String getDisplayText(@NonNull Locale aContentLocale);

    @Nullable
    default String getDisplayTextLocaleIndependent() {
        return getDisplayText(Locale.ROOT);
    }

    /**
     * @return <code>true</code> if the detection text is multilingual, <code>false</code> otherwise.
     */
    default boolean isMultilingual() {
        return getAllLocales().size() > 1;
    }
}
