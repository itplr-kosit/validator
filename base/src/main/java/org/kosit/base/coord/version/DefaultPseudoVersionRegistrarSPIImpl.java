package org.kosit.base.coord.version;

import org.jspecify.annotations.NonNull;

/**
 * Default pseudo version registrar.
 *
 * @author Philip Helger
 */
public final class DefaultPseudoVersionRegistrarSPIImpl implements IDVRPseudoVersionRegistrarSPI {

    /**
     * Public no-argument constructor, used via reflection by the {@link java.util.ServiceLoader}.
     */
    public DefaultPseudoVersionRegistrarSPIImpl() {
    }

    public void registerPseudoVersions(@NonNull final IDVRPseudoVersionRegistry registry) {
        registry.registerPseudoVersion(DVRPseudoVersionRegistry.OLDEST);
        registry.registerPseudoVersion(DVRPseudoVersionRegistry.LATEST_RELEASE);
        registry.registerPseudoVersion(DVRPseudoVersionRegistry.LATEST);
    }
}
