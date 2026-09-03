package org.conformatron.api.model.validation;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A validation artifact that has been resolved and loaded by step 5 ({@code RETRIEVE_ARTIFACTS}).
 * <p>
 * Carries the artifact reference metadata needed to populate {@code ICTReportMetadata} and the raw content needed by
 * step 6 ({@code PREPARE_RULES}).
 * </p>
 */
public interface CTResolvedArtifact {

    /**
     * @return The URI / path of the artifact relative to the repository root. Maps to XVRL {@code <schema @href>}.
     */
    @NonNull
    @Nonempty
    String getArtifactReference();

    /**
     * @return The validation type this artifact is intended for (e.g. {@code ECTValidationType.SCHEMATRON_SCHXSLT},
     *         {@code ECTValidationType.XSD}).
     */
    @NonNull
    CTStandardValidationType getValidationType();

    /**
     * @return The raw bytes of the artifact (e.g. the SCH or XSL file content). {@code null} if the artifact is already
     *         available as a precompiled handle (see {@link #getPrecompiledHandle()}).
     */
    @Nullable
    byte[] getContent();

    /**
     * @return An opaque handle to a precompiled artifact (e.g. a Saxon {@code XsltExecutable}), or {@code null} if the
     *         artifact is in source form and must be compiled by step 6.
     */
    @Nullable
    Object getPrecompiledHandle();

    /**
     * @return {@code true} if this artifact is already compiled and {@link #getPrecompiledHandle()} is non-null. Step 6
     *         will be a pass-through.
     */
    default boolean isPrecompiled() {
        return getPrecompiledHandle() != null;
    }
}
