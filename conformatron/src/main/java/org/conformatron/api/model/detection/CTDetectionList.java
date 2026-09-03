package org.conformatron.api.model.detection;

import java.util.List;
import java.util.function.Predicate;

import org.conformatron.api.annotation.Nonnegative;
import org.jspecify.annotations.NonNull;

/**
 * Contains a list of {@link CTDetection} objects.
 */
public interface CTDetectionList {

    @NonNull
    List<CTDetection> getAll();

    @Nonnegative
    int getCount();

    @NonNull
    List<CTDetection> getAll(@NonNull Predicate<? super CTDetection> aFilter);

    @Nonnegative
    int getCount(@NonNull Predicate<? super CTDetection> aFilter);

    @NonNull
    default List<CTDetection> getAllErrors() {
        return getAll(x -> x.getSeverity().isError());
    }

    @NonNull
    default List<CTDetection> getAllWarnings() {
        return getAll(x -> x.getSeverity().isWarning());
    }

    @Nonnegative
    default int getErrorCount() {
        return getCount(x -> x.getSeverity().isError());
    }

    @Nonnegative
    default int getWarningCount() {
        return getCount(x -> x.getSeverity().isWarning());
    }

    default boolean containsOnlyError() {
        return getCount() == getErrorCount();
    }

    default boolean containsOnlyWarning() {
        return getCount() == getWarningCount();
    }

    default boolean containsAtLeastOneError() {
        return getErrorCount() > 0;
    }

    default boolean containsAtLeastOneWarning() {
        return getWarningCount() > 0;
    }

    default boolean containsNoError() {
        return getErrorCount() == 0;
    }

    default boolean containsNoWarning() {
        return getWarningCount() == 0;
    }

    @NonNull
    CTSeverity getWorstSeverity();
}
