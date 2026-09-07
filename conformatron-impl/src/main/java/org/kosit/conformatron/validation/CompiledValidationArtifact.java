package org.kosit.conformatron.validation;

import java.util.Objects;

import org.conformatron.api.model.validation.CTCompiledValidationArtifact;
import org.conformatron.api.model.validation.CTValidationType;
import org.jspecify.annotations.NonNull;

/**
 * Validator implementation of {@link CTCompiledValidationArtifact}: an engine-ready compilation together with the
 * validation type that determines its concrete Java type (Saxon {@code XsltExecutable} for Schematron,
 * {@code javax.xml.validation.Schema} for XSD).
 *
 * @param <T> the engine-specific compilation type
 *
 * @param validationType validation type
 * @param compilation compilation object
 * @author Andreas Schmitz
 */
public final record CompiledValidationArtifact<T> (CTValidationType validationType,
        T compilation) implements CTCompiledValidationArtifact<T> {

    public CompiledValidationArtifact {
        Objects.requireNonNull(validationType);
        Objects.requireNonNull(compilation);
    }

    public @NonNull CTValidationType getValidationType() {
        return validationType;
    }

    public @NonNull T getCompilation() {
        return compilation;
    }
}
