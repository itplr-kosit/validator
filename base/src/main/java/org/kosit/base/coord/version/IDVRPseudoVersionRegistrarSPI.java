package org.kosit.base.coord.version;

import org.jspecify.annotations.NonNull;

/**
 * SPI pseudo version registration interface.
 *
 * @author Philip Helger
 */
public interface IDVRPseudoVersionRegistrarSPI {

    /**
     * Register all pseudo versions of this library to the provided registry.
     *
     * @param registry the registry to register to. Never <code>null</code>.
     */
    void registerPseudoVersions(@NonNull IDVRPseudoVersionRegistry registry);
}
