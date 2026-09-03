package org.conformatron.api.model.validation;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A validation artifact that has been resolved and loaded by step 5 ({@code RETRIEVE_ARTIFACTS}).
 * <p>
 * Carries the reference metadata needed to populate {@code ICTReportMetadata} and the content needed by step 6
 * ({@code PREPARE_RULES}). Step 5 turns the scenario's {@link CTValidationArtifactReference} list into a list of these
 * objects; steps 6 and 7 never see raw references again, which is what makes them self-contained (no repository
 * access).
 * </p>
 * <p>
 * An artifact is in exactly one of two states: <b>source form</b> ({@link #getContent()} set, to be compiled by step 6)
 * or <b>precompiled</b> ({@link #getCompiledArtifact()} set, step 6 is a typed pass-through).
 * </p>
 *
 * <p>
 * <b>Security:</b> resolution of the reference is confined by the executing validator's resolving strategy — see
 * {@link CTValidationArtifactReference}.
 * </p>
 */
public interface CTResolvedValidationArtifact {

    /**
     * @return The reference this artifact was resolved from. Maps to XVRL {@code <schema @href>}.
     */
    @NonNull
    CTValidationArtifactReference getReference();

    /**
     * @return The validation type this artifact is intended for (e.g.
     *         {@code CTStandardValidationType.SCHEMATRON_SCHXSLT}, {@code CTStandardValidationType.XSD}).
     */
    @NonNull
    CTValidationType getValidationType();

    /**
     * @return The raw bytes of the artifact (e.g. the SCH or XSL file content). {@code null} if the artifact is already
     *         available as a compiled artifact (see {@link #getCompiledArtifact()}).
     */
    @Nullable
    byte[] getContent();

    /**
     * @return The already-compiled artifact, or {@code null} if this artifact is in source form and must be compiled by
     *         step 6.
     */
    @Nullable
    CTCompiledValidationArtifact<?> getCompiledArtifact();

    /**
     * @return {@code true} if this artifact is already compiled and {@link #getCompiledArtifact()} is non-null. Step 6
     *         will be a pass-through.
     */
    default boolean isPrecompiled() {
        return getCompiledArtifact() != null;
    }
}
