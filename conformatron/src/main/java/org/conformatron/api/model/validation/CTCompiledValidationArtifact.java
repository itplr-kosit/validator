package org.conformatron.api.model.validation;

import org.jspecify.annotations.NonNull;

/**
 * An engine-ready, compiled validation artifact — the typed replacement for the former opaque {@code Object} handle.
 * <p>
 * The compilation itself stays engine-specific (a Saxon {@code XsltExecutable}, a JAXP {@code Schema}, …), but it is no
 * longer untyped: implementations bind {@code T} to their engine type, and {@link #getValidationType()} tells callers
 * which binding to expect.
 * </p>
 *
 * @param <T> the engine-specific compilation type
 */
public interface CTCompiledValidationArtifact<T> {

    /**
     * @return The validation type this artifact was compiled for. Determines the concrete type of
     *         {@link #getCompilation()}.
     */
    @NonNull
    CTValidationType getValidationType();

    /**
     * @return The engine-specific compilation, ready for execution by step 7 ({@code APPLY_RULES}).
     */
    @NonNull
    T getCompilation();
}
