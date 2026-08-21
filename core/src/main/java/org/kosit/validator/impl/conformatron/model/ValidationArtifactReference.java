package org.kosit.validator.impl.conformatron.model;

import java.net.URI;
import java.util.Objects;

import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.model.source.CTValidationArtifactReference;
import org.kosit.validator.impl.conformatron.util.ArtifactResolver;

/**
 * Validator implementation of {@link CTValidationArtifactReference}: a pure carrier for the reference to a validation
 * artifact (XSD, Schematron, precompiled XSLT), typically relative to the artifact repository.
 * <p>
 * The reference makes no promise about resolvability — turning it into a readable resource is the job of
 * {@link ArtifactResolver}, which confines resolution to the configured repository (security concern, see step-05
 * spec).
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class ValidationArtifactReference implements CTValidationArtifactReference {

    private final URI reference;

    /**
     * @param reference the artifact reference; must not be {@code null}
     * @return the carrier for this reference
     */
    public static ValidationArtifactReference of(final URI reference) {
        Objects.requireNonNull(reference);
        return new ValidationArtifactReference(reference);
    }

    /**
     * @param reference the artifact reference as declared in the scenario configuration (e.g. {@code "simple.sch"})
     * @return the carrier for this reference
     */
    public static ValidationArtifactReference of(@Nonempty final String reference) {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("reference may not be null or blank");
        }
        return of(URI.create(reference));
    }

    private ValidationArtifactReference(final URI reference) {
        this.reference = reference;
    }

    @Override
    public URI getValidationArtifactReference() {
        return this.reference;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this)
            return true;
        if (other == null || !other.getClass().equals(ValidationArtifactReference.class))
            return false;
        final ValidationArtifactReference rhs = (ValidationArtifactReference) other;
        return this.reference.equals(rhs.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.reference);
    }

    @Override
    public String toString() {
        return this.reference.toString();
    }
}
