package org.kosit.base.coord.version;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.kosit.base.version.Version;

/**
 * Test class for class {@link DVRVersion}.
 *
 * @author Philip Helger
 */
public class DVRVersionTest {

    private static void assertEqualContent(final DVRVersion left, final DVRVersion right) {
        assertThat(left).isEqualTo(right).hasSameHashCodeAs(right);
        assertThat(right).isEqualTo(left);
    }

    private static void assertDifferentContent(final DVRVersion left, final DVRVersion right) {
        assertThat(left).isNotEqualTo(right);
        assertThat(right).isNotEqualTo(left);
    }

    @Test
    public void basic() {
        // Valid static
        DVRVersion ver = DVRVersion.parseOrNull("1.2.3");
        assertThat(ver).isNotNull();
        assertThat(ver.isStaticVersion()).isTrue();
        assertThat(ver.getStaticVersion()).isNotNull();
        assertThat(ver.isPseudoVersion()).isFalse();
        assertThat(ver.getPseudoVersion()).isNull();
        assertThat(ver.getAsString()).isEqualTo("1.2.3");

        // Valid static
        ver = DVRVersion.parseOrNull("1.2.3.a");
        assertThat(ver).isNotNull();
        assertThat(ver.isStaticVersion()).isTrue();
        assertThat(ver.getAsString()).isEqualTo("1.2.3-a");

        // Valid static
        ver = DVRVersion.parseOrNull("1.2.3.4.5.6.7.8");
        assertThat(ver).isNotNull();
        assertThat(ver.isStaticVersion()).isTrue();
        assertThat(ver.getAsString()).isEqualTo("1.2.3-4.5.6.7.8");

        // Valid static
        ver = DVRVersion.parseOrNull("1.2.3-a");
        assertThat(ver).isNotNull();
        assertThat(ver.isStaticVersion()).isTrue();
        assertThat(ver.getAsString()).isEqualTo("1.2.3-a");

        // Invalid static
        assertThat(DVRVersion.parseOrNull("0.09.5")).isNull();

        // Valid pseudo version
        ver = DVRVersion.parseOrNull("latest");
        assertThat(ver).isNotNull();
        assertThat(ver.isStaticVersion()).isFalse();
        assertThat(ver.getStaticVersion()).isNull();
        assertThat(ver.isPseudoVersion()).isTrue();
        assertThat(ver.getPseudoVersion()).isNotNull();
        assertThat(ver.getAsString()).isEqualTo("latest");

        // Invalid pseudo version - it is taken as a static version qualifier
        ver = DVRVersion.parseOrNull("blafoo");
        assertThat(ver).isNotNull();
        assertThat(ver.isStaticVersion()).isTrue();
        assertThat(ver.getStaticVersion()).isNotNull();
        assertThat(ver.isPseudoVersion()).isFalse();
        assertThat(ver.getPseudoVersion()).isNull();
        assertThat(ver.getAsString()).isEqualTo("blafoo");

        final Version staticVer = ver.getStaticVersion();
        assertThat(staticVer.getMajor()).isZero();
        assertThat(staticVer.getMinor()).isZero();
        assertThat(staticVer.getMicro()).isZero();
        assertThat(staticVer.getQualifier()).isEqualTo("blafoo");
    }

    @Test
    public void parseEmpty() {
        assertThat(DVRVersion.parseOrNull(null)).isNull();
        assertThat(DVRVersion.parseOrNull("")).isNull();
    }

    @Test
    public void versionClassifier() {
        // A version classifier may be separated with "-" or with "."
        for (final String version : new String[] { "1.4.0-03", "1.4.0.03" }) {
            final DVRVersion ver = DVRVersion.parseOrNull(version);
            assertThat(ver).isNotNull();
            assertThat(ver.isStaticVersion()).isTrue();

            final Version staticVer = ver.getStaticVersion();
            assertThat(staticVer.getMajor()).isEqualTo(1);
            assertThat(staticVer.getMinor()).isEqualTo(4);
            assertThat(staticVer.getMicro()).isZero();
            assertThat(staticVer.getQualifier()).isEqualTo("03");

            // Trailing zero micro version is not part of the string representation
            assertThat(ver.getAsString()).isEqualTo("1.4-03");
        }

        // Every version classifier can be read back from the string representation, numeric ones included
        for (final String classifier : new String[] { "SNAPSHOT", "RC1", "hotfix03", "03", "3" }) {
            final DVRVersion ver = DVRVersion.parseOrNull("1.4.0-" + classifier);
            assertThat(ver).isNotNull();
            assertThat(ver.getAsString()).isEqualTo("1.4-" + classifier);
            assertThat(DVRVersion.parseOrNull(ver.getAsString())).isEqualTo(ver);
        }

        // The short form of a numeric version classifier is only reachable via the strict layout, because
        // Version.parse would take the "03" as the micro version number
        assertThat(DVRVersion.parseOrNull("1.4-03")).isEqualTo(DVRVersion.parseOrNull("1.4.0-03"));
        assertThat(DVRVersion.parseOrNull("1.4-3")).isEqualTo(DVRVersion.parseOrNull("1.4.0-3"));

        // A numeric version classifier is something different than a micro version
        assertThat(DVRVersion.parseOrNull("1.4.3").getAsString()).isEqualTo("1.4.3");
        assertThat(DVRVersion.parseOrNull("1.4.3")).isNotEqualTo(DVRVersion.parseOrNull("1.4-3"));

        // Backwards compatibility: for "1.4-1.2.3" both interpretations are possible, and the established one wins
        // over the strict layout
        final DVRVersion ambiguous = DVRVersion.parseOrNull("1.4-1.2.3");
        assertThat(ambiguous).isNotNull();
        assertThat(ambiguous.getStaticVersion().getMajor()).isEqualTo(1);
        assertThat(ambiguous.getStaticVersion().getMinor()).isZero();
        assertThat(ambiguous.getStaticVersion().getMicro()).isZero();
        assertThat(ambiguous.getStaticVersion().getQualifier()).isEqualTo("4-1.2.3");
    }

    @Test
    public void getAsString() {
        assertThat(DVRVersion.parseOrNull("1.2.3.bla").getAsString()).isEqualTo("1.2.3-bla");
        assertThat(DVRVersion.parseOrNull("1.2.3").getAsString()).isEqualTo("1.2.3");
        assertThat(DVRVersion.parseOrNull("1.2").getAsString()).isEqualTo("1.2");
        assertThat(DVRVersion.parseOrNull("1").getAsString()).isEqualTo("1");
        assertThat(DVRVersion.parseOrNull("1.0").getAsString()).isEqualTo("1");
        assertThat(DVRVersion.parseOrNull("1.0.0").getAsString()).isEqualTo("1");
        assertThat(DVRVersion.parseOrNull("0.1").getAsString()).isEqualTo("0.1");
    }

    @Test
    public void zero() {
        assertThat(DVRVersion.parseOrNull("0").getAsString()).isEqualTo("0");
        assertThat(DVRVersion.parseOrNull("0.0").getAsString()).isEqualTo("0");
        assertThat(DVRVersion.parseOrNull("0.0.0").getAsString()).isEqualTo("0");
    }

    @Test
    public void equalsHashcode() {
        final DVRVersion ver = DVRVersion.parseOrNull("1.2.3");
        assertEqualContent(ver, DVRVersion.parseOrNull("1.2.3"));
        assertDifferentContent(ver, DVRVersion.parseOrNull("1.2.4"));
        assertDifferentContent(ver, DVRVersion.parseOrNull("1.1.3"));
        assertDifferentContent(ver, DVRVersion.parseOrNull("2.2.3"));
        assertDifferentContent(ver, DVRVersion.parseOrNull("1.2"));
        assertDifferentContent(ver, DVRVersion.parseOrNull("1.2.3.bla"));
        assertDifferentContent(ver, DVRVersion.latest());

        assertEqualContent(DVRVersion.latest(), DVRVersion.of(DVRPseudoVersionRegistry.LATEST));
        assertDifferentContent(DVRVersion.latest(), DVRVersion.latestRelease());
    }

    @Test
    public void compare() {
        final DVRVersion ver0 = DVRVersion.of(DVRPseudoVersionRegistry.OLDEST);
        final DVRVersion ver1 = DVRVersion.parseOrNull("1.2");
        final DVRVersion ver2 = DVRVersion.parseOrNull("1.2.3");
        final DVRVersion ver3 = DVRVersion.parseOrNull("1.2.4");
        final DVRVersion ver4 = DVRVersion.parseOrNull("1.3");
        final DVRVersion ver5 = DVRVersion.parseOrNull("2023.5");
        final DVRVersion ver6 = DVRVersion.of(DVRPseudoVersionRegistry.LATEST_RELEASE);
        final DVRVersion ver7 = DVRVersion.of(DVRPseudoVersionRegistry.LATEST);

        final DVRVersion[] ascending = { ver0, ver1, ver2, ver3, ver4, ver5, ver6, ver7 };
        for (int i = 0; i < ascending.length; ++i) {
            for (int j = 0; j < ascending.length; ++j) {
                final int cmp = ascending[i].compareTo(ascending[j]);
                if (i < j) {
                    assertThat(cmp).as("%s <=> %s", ascending[i], ascending[j]).isNegative();
                } else if (i > j) {
                    assertThat(cmp).as("%s <=> %s", ascending[i], ascending[j]).isPositive();
                } else {
                    assertThat(cmp).as("%s <=> %s", ascending[i], ascending[j]).isZero();
                }
            }
        }
    }

    @Test
    public void snapshot() {
        final DVRVersion ver1 = DVRVersion.parseOrNull("0.9.9");
        final DVRVersion ver2 = DVRVersion.parseOrNull("1.0.0-SNAPSHOT");
        final DVRVersion ver3 = DVRVersion.parseOrNull("1.0.0");
        final DVRVersion ver4 = DVRVersion.parseOrNull("1.0.1");

        assertThat(ver1).isEqualByComparingTo(ver1).isLessThan(ver2).isLessThan(ver3).isLessThan(ver4);
        assertThat(ver2).isGreaterThan(ver1).isEqualByComparingTo(ver2).isLessThan(ver3).isLessThan(ver4);
        assertThat(ver3).isGreaterThan(ver1).isGreaterThan(ver2).isEqualByComparingTo(ver3).isLessThan(ver4);
        assertThat(ver4).isGreaterThan(ver1).isGreaterThan(ver2).isGreaterThan(ver3).isEqualByComparingTo(ver4);
    }

    @Test
    public void isValidStaticVersion() {
        for (final String version : new String[] { "1.0.0", "1.0", "1", "1.0.0-SNAPSHOT", "1.0-SNAPSHOT", "1-SNAPSHOT", "1.0.0.SNAPSHOT",
                "1.0.SNAPSHOT", "1.SNAPSHOT", "1.4.0-03", "1.4-03" }) {
            assertThat(DVRVersion.isValidStaticVersion(version)).as(version).isTrue();
        }

        for (final String version : new String[] { null, "", "0.09.5", "a:b" }) {
            assertThat(DVRVersion.isValidStaticVersion(version)).as(String.valueOf(version)).isFalse();
        }
    }
}
