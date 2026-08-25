package org.kosit.validator.impl.conformatron.model;

import java.util.Objects;

import org.conformatron.api.model.validation.CTCompiledValidationArtifact;
import org.conformatron.api.model.validation.CTResolvedValidationArtifact;
import org.conformatron.api.model.validation.CTValidationArtifactReference;
import org.conformatron.api.model.validation.CTValidationType;

/**
 * Validator implementation of {@link CTResolvedValidationArtifact} (conformatron-api step 5,
 * {@code RETRIEVE_ARTIFACTS}).
 * <p>
 * An instance is in exactly one of two states, mirroring the API contract:
 * </p>
 * <ul>
 * <li><b>source form</b> ({@link #loaded}): the content was loaded from the repository and step 6 compiles it;</li>
 * <li><b>precompiled</b> ({@link #precompiled}): an engine-ready compilation already exists (e.g. an
 * {@code XsltExecutable} from the legacy {@code Scenario}) and step 6 is a typed pass-through.</li>
 * </ul>
 * Instances are immutable; the content is defensively copied on construction and cloned on access.
 *
 * @author Andreas Schmitz
 */
public final class ResolvedValidationArtifact implements CTResolvedValidationArtifact {

    private final CTValidationArtifactReference reference;

    private final CTValidationType validationType;

    private final byte[] content;

    private final CTCompiledValidationArtifact<?> compiledArtifact;

    private ResolvedValidationArtifact(final CTValidationArtifactReference reference, final CTValidationType validationType,
            final byte[] content, final CTCompiledValidationArtifact<?> compiledArtifact) {
        Objects.requireNonNull(reference);
        Objects.requireNonNull(validationType);
        this.reference = reference;
        this.validationType = validationType;
        this.content = content == null ? null : content.clone();
        this.compiledArtifact = compiledArtifact;
    }

    /**
     * Creates an artifact in source form, as loaded from the repository by step 5.
     *
     * @param reference the reference this artifact was resolved from
     * @param validationType the validation type the artifact is intended for
     * @param content the artifact content
     * @return the resolved artifact
     */
    public static ResolvedValidationArtifact loaded(final CTValidationArtifactReference reference, final CTValidationType validationType,
            final byte[] content) {
        Objects.requireNonNull(content);
        return new ResolvedValidationArtifact(reference, validationType, content, null);
    }

    /**
     * Creates an already-compiled artifact — step 6 will pass it through unchanged. Used by the facade, where the
     * legacy {@code Scenario} already holds compiled executables.
     *
     * @param reference the reference this artifact belongs to
     * @param compiledArtifact the engine-ready compilation
     * @return the resolved artifact
     */
    public static ResolvedValidationArtifact precompiled(final CTValidationArtifactReference reference,
            final CTCompiledValidationArtifact<?> compiledArtifact) {
        Objects.requireNonNull(compiledArtifact);
        return new ResolvedValidationArtifact(reference, compiledArtifact.getValidationType(), null, compiledArtifact);
    }

    @Override
    public CTValidationArtifactReference getReference() {
        return this.reference;
    }

    @Override
    public CTValidationType getValidationType() {
        return this.validationType;
    }

    @Override
    public byte[] getContent() {
        return this.content == null ? null : this.content.clone();
    }

    @Override
    public CTCompiledValidationArtifact<?> getCompiledArtifact() {
        return this.compiledArtifact;
    }
}
