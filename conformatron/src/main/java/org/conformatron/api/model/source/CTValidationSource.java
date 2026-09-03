package org.conformatron.api.model.source;

import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.model.validation.CTSyntax;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents the raw input resource entering the validation pipeline.
 * <p>
 * Created by step 1 ({@code DETECT_SYNTAX}) and consumed by step 2 ({@code PARSE_DOCUMENT}). Orientation: phive
 * {@code IValidationSource}.
 * </p>
 * <ul>
 * <li>{@link #getName()} identifies the resource for logging and report metadata.</li>
 * <li>{@link #getDetectedSyntax()} carries the syntax detected in the first chunk; {@code null} before step 1
 * completes.</li>
 * <li>{@link #isComplete()} distinguishes a full document from a partial one (e.g. a fragment used in extension
 * validation).</li>
 * </ul>
 */
public interface CTValidationSource {

    /**
     * @return The underlying resource.
     */
    @NonNull
    @Nonempty
    CTReadResource getReadResource();

    @NonNull
    default String getName() {
        return getReadResource().getName();
    }

    /**
     * @return The syntax detected during step 1, or {@code null} if detection has not run yet / failed. Implementations
     *         of step 2 must only be called when this is non-null.
     */
    @Nullable
    CTSyntax getDetectedSyntax();

    /**
     * @return {@code true} if this source represents a complete document; {@code false} if it is a partial/fragment
     *         source (e.g. for extension validation). Parsers may behave differently for partial sources.
     */
    boolean isComplete();
}
