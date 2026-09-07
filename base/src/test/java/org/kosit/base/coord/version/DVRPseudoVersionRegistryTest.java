package org.kosit.base.coord.version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kosit.base.version.Version;

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

    @Test
    public void registerNullIsRejected() {
        final DVRPseudoVersionRegistry registry = DVRPseudoVersionRegistry.getInstance();
        assertThatThrownBy(() -> registry.registerPseudoVersion(null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("PseudoVersion");
    }

    @Test
    public void getFromIDOrNullWithNull() {
        assertThat(DVRPseudoVersionRegistry.getInstance().getFromIDOrNull(null)).isNull();
    }

    @Test
    public void registerAndReinitialize() {
        final DVRPseudoVersionRegistry registry = DVRPseudoVersionRegistry.getInstance();
        final IDVRPseudoVersion nightly = new DVRPseudoVersion("nightly", new IDVRPseudoVersionComparable() {

            public int compareToPseudoVersion(final IDVRPseudoVersion otherPseudoVersion) {
                return -1;
            }

            public int compareToVersion(final Version otherStaticVersion) {
                return -1;
            }
        });
        try {
            assertThat(registry.registerPseudoVersion(nightly)).isTrue();
            assertThat(registry.size()).isEqualTo(4);
            assertThat(registry.getFromIDOrNull("nightly")).isSameAs(nightly);
        } finally {
            // drop everything and re-run the SPI search
            registry.reinitialize();
        }

        assertThat(registry.size()).isEqualTo(3);
        assertThat(registry.getFromIDOrNull("nightly")).isNull();
    }

    @Test
    public void theDefaultSpiRegistersTheThreeKnownPseudoVersions() {
        final List<IDVRPseudoVersion> registered = new ArrayList<>();
        new DefaultPseudoVersionRegistrarSPIImpl().registerPseudoVersions(new IDVRPseudoVersionRegistry() {

            public boolean registerPseudoVersion(final IDVRPseudoVersion pseudoVersion) {
                return registered.add(pseudoVersion);
            }

            public IDVRPseudoVersion getFromIDOrNull(final String id) {
                return null;
            }
        });

        assertThat(registered).containsExactly(DVRPseudoVersionRegistry.OLDEST, DVRPseudoVersionRegistry.LATEST_RELEASE,
                DVRPseudoVersionRegistry.LATEST);
    }

    @Test
    public void asString() {
        assertThat(DVRPseudoVersionRegistry.getInstance().toString()).contains("map=").contains("latest");
    }
}
