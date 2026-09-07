package org.kosit.conformatron.validation;

import java.net.URI;
import java.util.Objects;

import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.model.validation.CTValidationArtifactReference;
import org.kosit.base.string.StringHelper;

/**
 * Validator implementation of {@link CTValidationArtifactReference}: a pure carrier for the reference to a validation
 * artifact (XSD, Schematron, precompiled XSLT), typically relative to the artifact repository.
 * <p>
 * The reference makes no promise about resolvability — turning it into a readable resource is the job of
 * {@code ArtifactResolver}, which confines resolution to the configured repository (security concern, see step-05
 * spec).
 * </p>
 *
 * @param reference the artifact reference; must not be {@code null}
 * @author Andreas Schmitz
 */
public final record ValidationArtifactReference(URI reference) implements CTValidationArtifactReference {

    public ValidationArtifactReference {
        Objects.requireNonNull(reference);
    }

    @Override
    public URI getValidationArtifactReference() {
        return reference;
    }

    /**
     * @param reference the artifact reference as declared in the scenario configuration (e.g. {@code "simple.sch"})
     * @return the carrier for this reference
     */
    public static ValidationArtifactReference of(@Nonempty final String reference) {
        if (StringHelper.isBlank(reference)) {
            throw new IllegalArgumentException("reference may not be null or blank");
        }
        return new ValidationArtifactReference(URI.create(reference));
    }
}
