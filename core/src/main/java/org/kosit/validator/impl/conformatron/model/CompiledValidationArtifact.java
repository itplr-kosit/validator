package org.kosit.validator.impl.conformatron.model;

import org.conformatron.api.model.validation.CTCompiledValidationArtifact;
import org.conformatron.api.model.validation.CTValidationType;

/**
 * Validator implementation of {@link CTCompiledValidationArtifact}: an engine-ready compilation together with the
 * validation type that determines its concrete Java type (Saxon {@code XsltExecutable} for Schematron,
 * {@code javax.xml.validation.Schema} for XSD).
 *
 * @param <T> the engine-specific compilation type
 *
 * @author Andreas Schmitz
 */
public final class CompiledValidationArtifact<T> implements CTCompiledValidationArtifact<T> {

    private final CTValidationType validationType;

    private final T compilation;

    /**
     * @param validationType the validation type this compilation belongs to
     * @param compilation the engine-specific compilation
     * @param <T> the engine-specific compilation type
     * @return the typed compiled artifact
     */
    public static <T> CompiledValidationArtifact<T> of(final CTValidationType validationType, final T compilation) {
        if (validationType == null) {
            throw new IllegalArgumentException("validationType may not be null");
        }
        if (compilation == null) {
            throw new IllegalArgumentException("compilation may not be null");
        }
        return new CompiledValidationArtifact<>(validationType, compilation);
    }

    private CompiledValidationArtifact(final CTValidationType validationType, final T compilation) {
        this.validationType = validationType;
        this.compilation = compilation;
    }

    @Override
    public CTValidationType getValidationType() {
        return validationType;
    }

    @Override
    public T getCompilation() {
        return compilation;
    }
}
