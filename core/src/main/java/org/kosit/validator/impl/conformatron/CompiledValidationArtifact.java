package org.kosit.validator.impl.conformatron;

import org.conformatron.api.model.source.ICTCompiledValidationArtifact;
import org.conformatron.api.model.validation.ECTValidationType;

/**
 * Validator implementation of {@link ICTCompiledValidationArtifact}: an engine-ready compilation together with the
 * validation type that determines its concrete Java type (Saxon {@code XsltExecutable} for Schematron,
 * {@code javax.xml.validation.Schema} for XSD).
 *
 * @param <T> the engine-specific compilation type
 *
 * @author Andreas Schmitz
 */
public final class CompiledValidationArtifact<T> implements ICTCompiledValidationArtifact<T> {

    private final ECTValidationType validationType;

    private final T compilation;

    private CompiledValidationArtifact(final ECTValidationType validationType, final T compilation) {
        this.validationType = validationType;
        this.compilation = compilation;
    }

    /**
     * @param validationType the validation type this compilation belongs to
     * @param compilation the engine-specific compilation
     * @param <T> the engine-specific compilation type
     * @return the typed compiled artifact
     */
    public static <T> CompiledValidationArtifact<T> of(final ECTValidationType validationType, final T compilation) {
        if (validationType == null) {
            throw new IllegalArgumentException("validationType may not be null");
        }
        if (compilation == null) {
            throw new IllegalArgumentException("compilation may not be null");
        }
        return new CompiledValidationArtifact<>(validationType, compilation);
    }

    @Override
    public ECTValidationType getValidationType() {
        return this.validationType;
    }

    @Override
    public T getCompilation() {
        return this.compilation;
    }
}
