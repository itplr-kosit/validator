package org.conformatron.api.model.validation;

import java.net.URI;

import org.jspecify.annotations.NonNull;

/**
 * A reference to a validation artifact (XSD, Schematron, precompiled XSLT) — a <b>pure carrier</b> for the resource
 * reference, nothing else.
 * <p>
 * Declared by a scenario ({@link org.conformatron.api.model.scenario.CTScenarioMatch#getArtifactReferences()}) and
 * resolved by step 5 ({@code RETRIEVE_ARTIFACTS}) into an {@link CTResolvedValidationArtifact}. References are
 * deliberately typed rather than plain strings so that resolution and the security rules around it (see below) have a
 * single, explicit input type.
 * </p>
 *
 * <p>
 * <b>Security:</b> a reference is untrusted input. How it is turned into a readable resource is the responsibility of
 * the resolving strategy of the executing validator, which must confine resolution to the configured repository (no
 * absolute or external fetches unless explicitly configured). The reference itself makes no promise about
 * resolvability.
 * </p>
 */
public interface CTValidationArtifactReference {

    /**
     * @return The reference to the validation artifact, typically relative to the artifact repository root. Maps to
     *         XVRL {@code <schema @href>}.
     */
    @NonNull
    URI getValidationArtifactReference();
}
