package org.kosit.base.coord.version;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Base interface for a Pseudo version registry. Implementations are not thread safe.
 *
 * @author Philip Helger
 */
public interface IDVRPseudoVersionRegistry {

    /**
     * Register the provided pseudo version.
     *
     * @param pseudoVersion the pseudo version to register. Must not be <code>null</code>.
     * @return <code>true</code> if it was added, <code>false</code> if another pseudo version with the same ID was
     *         already present.
     */
    boolean registerPseudoVersion(@NonNull IDVRPseudoVersion pseudoVersion);

    /**
     * Try to resolve the pseudo version with the provided ID.
     *
     * @param id the pseudo version ID to look up. May be <code>null</code>.
     * @return <code>null</code> if no such pseudo version is present.
     */
    @Nullable
    IDVRPseudoVersion getFromIDOrNull(@Nullable String id);
}
