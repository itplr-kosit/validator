package org.kosit.validator.impl.conformatron.model;

import org.kosit.validator.impl.conformatron.util.ArtifactResolver;

import java.net.URI;
import java.util.Objects;

import org.conformatron.api.model.source.ICTValidationArtifactReference;

/**
 * Validator implementation of {@link ICTValidationArtifactReference}: a pure carrier for the reference to a validation
 * artifact (XSD, Schematron, precompiled XSLT), typically relative to the artifact repository.
 * <p>
 * The reference makes no promise about resolvability — turning it into a readable resource is the job of
 * {@link ArtifactResolver}, which confines resolution to the configured repository (security concern, see step-05
 * spec).
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class ValidationArtifactReference implements ICTValidationArtifactReference {

    private final URI reference;

    private ValidationArtifactReference(final URI reference) {
        this.reference = reference;
    }

    /**
     * @param reference the artifact reference; must not be {@code null}
     * @return the carrier for this reference
     */
    public static ValidationArtifactReference of(final URI reference) {
        if (reference == null) {
            throw new IllegalArgumentException("reference may not be null");
        }
        return new ValidationArtifactReference(reference);
    }

    /**
     * @param reference the artifact reference as declared in the scenario configuration (e.g. {@code "simple.sch"})
     * @return the carrier for this reference
     */
    public static ValidationArtifactReference of(final String reference) {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("reference may not be null or blank");
        }
        return of(URI.create(reference));
    }

    @Override
    public URI getValidationArtifactReference() {
        return this.reference;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof final ValidationArtifactReference o && this.reference.equals(o.reference);
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
