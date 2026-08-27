package org.kosit.base.coord.version;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for class {@link DVRPseudoVersionRegistry}.
 *
 * @author Philip Helger
 */
public class DVRPseudoVersionRegistryTest {

    @Test
    public void basic() {
        final DVRPseudoVersionRegistry registry = DVRPseudoVersionRegistry.getInstance();
        assertThat(registry).isNotNull();

        // Registered via the SPI in META-INF/services
        assertThat(registry.size()).isEqualTo(3);

        assertThat(registry.getFromIDOrNull(DVRPseudoVersionRegistry.OLDEST.getID())).isNotNull();
        assertThat(registry.getFromIDOrNull(DVRPseudoVersionRegistry.LATEST_RELEASE.getID())).isNotNull();
        assertThat(registry.getFromIDOrNull(DVRPseudoVersionRegistry.LATEST.getID())).isNotNull();
        assertThat(registry.getFromIDOrNull("hoppla")).isNull();
    }

    @Test
    public void registerExistingIsRejected() {
        final DVRPseudoVersionRegistry registry = DVRPseudoVersionRegistry.getInstance();
        assertThat(registry.registerPseudoVersion(DVRPseudoVersionRegistry.LATEST)).isFalse();
        assertThat(registry.size()).isEqualTo(3);
    }
}
