package org.kosit.base.coord.version;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link DVRVersion#compareToClassic(DVRVersion)} - the ordering as it was in ph-diver up to v4.2.1,
 * where "SNAPSHOT" was the only pre-release qualifier.
 *
 * @author Philip Helger
 */
public class DVRVersionClassicOrderTest {

    private static final Comparator<DVRVersion> CLASSIC = DVRVersion::compareToClassic;

    private static @NonNull DVRVersion parse(@NonNull final String version) {
        final DVRVersion ret = DVRVersion.parseOrNull(version);
        assertThat(ret).as("Failed to parse '%s'", version).isNotNull();
        return ret;
    }

    /**
     * Assert that the provided versions are in strictly ascending order according to the classic comparison. Every pair
     * is checked in both directions, so this also covers antisymmetry and transitivity.
     *
     * @param versions the versions in the expected ascending order.
     */
    private static void assertStrictlyAscendingClassic(@NonNull final String... versions) {
        final List<DVRVersion> list = new ArrayList<>();
        for (final String s : versions) {
            list.add(parse(s));
        }

        for (int i = 0; i < list.size(); ++i) {
            for (int j = 0; j < list.size(); ++j) {
                final int cmp = list.get(i).compareToClassic(list.get(j));
                final String msg = "'" + versions[i] + "' <=> '" + versions[j] + "' resulted in " + cmp;
                if (i < j) {
                    assertThat(cmp).as(msg).isNegative();
                } else if (i > j) {
                    assertThat(cmp).as(msg).isPositive();
                } else {
                    assertThat(cmp).as(msg).isZero();
                }
            }
        }

        // Sorting a scrambled copy must lead to the original order
        final List<DVRVersion> scrambled = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i) {
            scrambled.add(list.get(i * 17 % list.size()));
        }
        assertThat(scrambled).hasSameSizeAs(list);
        scrambled.sort(CLASSIC);
        assertThat(scrambled).isEqualTo(list);

        // All versions must remain distinguishable in a sorted set
        final TreeSet<DVRVersion> sorted = new TreeSet<>(CLASSIC);
        sorted.addAll(list);
        assertThat(sorted).hasSameSizeAs(list);
        assertThat(new ArrayList<>(sorted)).isEqualTo(list);
    }

    @Test
    public void snapshotIsTheOnlyPreRelease() {
        // SNAPSHOT still comes before the release ...
        assertStrictlyAscendingClassic("0.9.9", "1.0.0-SNAPSHOT", "1.0.0", "1.0.1");

        // ... but everything else comes after it, including all the qualifiers that are pre-release qualifiers in the
        // current ordering
        assertStrictlyAscendingClassic("1.0.0-SNAPSHOT", "1.0.0", "1.0.0-alpha1");
        assertStrictlyAscendingClassic("1.0.0-SNAPSHOT", "1.0.0", "1.0.0-beta1");
        assertStrictlyAscendingClassic("1.0.0-SNAPSHOT", "1.0.0", "1.0.0-milestone1");
        assertStrictlyAscendingClassic("1.0.0-SNAPSHOT", "1.0.0", "1.0.0-rc1");
        assertStrictlyAscendingClassic("1.0.0-SNAPSHOT", "1.0.0", "1.0.0-RC2");
    }

    @Test
    public void snapshotIsCaseSensitive() {
        // In the classic ordering only the exact spelling "SNAPSHOT" is a snapshot, everything else is an ordinary
        // qualifier that comes after the release
        assertStrictlyAscendingClassic("1.0.0-SNAPSHOT", "1.0.0", "1.0.0-Snapshot", "1.0.0-snapshot");
    }

    @Test
    public void qualifiersAreComparedAsStrings() {
        // No numeric comparison of the trailing number
        assertStrictlyAscendingClassic("1.0.0", "1.0.0-rc1", "1.0.0-rc10", "1.0.0-rc2", "1.0.0-rc9");

        // Uppercase before lowercase, as always for Strings
        assertStrictlyAscendingClassic("1.0.0", "1.0.0-RC1", "1.0.0-rc1");

        // The zero padding rule applies to every qualifier here
        assertStrictlyAscendingClassic("2.0.3", "2.0.3-01", "2.0.3-09", "2.0.3-13", "2.0.3-9");
    }

    @Test
    public void unknownQualifiersAreUnchanged() {
        // These behave identically in both orderings
        assertStrictlyAscendingClassic("1.3.5", "1.3.6", "1.3.6-a", "1.3.6-b", "1.3.7");
        assertStrictlyAscendingClassic("1.3.1", "1.4.0", "1.4.0-03", "1.4.1");
        assertStrictlyAscendingClassic("1.4.0", "1.4.0-hotfix01", "1.4.0-hotfix02");
    }

    @Test
    public void numericPartsWinOverQualifier() {
        assertStrictlyAscendingClassic("0.9.9-zzz", "1.0.0-SNAPSHOT", "1.0.0", "1.0.0-zzz", "1.0.1-SNAPSHOT");
        assertStrictlyAscendingClassic("1.2.3", "1.2.4-SNAPSHOT", "1.2.4", "1.2.4-rc1");
    }

    @Test
    public void pseudoVersionsAreUnaffected() {
        // The pseudo version handling is shared by both comparisons
        final DVRVersion oldest = DVRVersion.of(DVRPseudoVersionRegistry.OLDEST);
        final DVRVersion latest = DVRVersion.of(DVRPseudoVersionRegistry.LATEST);
        final DVRVersion latestRelease = DVRVersion.of(DVRPseudoVersionRegistry.LATEST_RELEASE);

        for (final String s : new String[] { "1.0.0-SNAPSHOT", "1.0.0-alpha1", "1.0.0-rc1", "1.0.0" }) {
            final DVRVersion ver = parse(s);
            assertThat(oldest.compareToClassic(ver)).as(s).isNegative();
            assertThat(ver.compareToClassic(oldest)).as(s).isPositive();
            assertThat(latest.compareToClassic(ver)).as(s).isPositive();
            assertThat(ver.compareToClassic(latest)).as(s).isNegative();
            assertThat(latestRelease.compareToClassic(ver)).as(s).isPositive();
            assertThat(ver.compareToClassic(latestRelease)).as(s).isNegative();

            // Identical to the current comparison
            assertThat(oldest.compareToClassic(ver)).as(s).isEqualTo(oldest.compareTo(ver));
            assertThat(ver.compareToClassic(latest)).as(s).isEqualTo(ver.compareTo(latest));
        }

        // Pseudo version vs. pseudo version
        assertThat(oldest.compareToClassic(latest)).isEqualTo(oldest.compareTo(latest));
        assertThat(latest.compareToClassic(latest)).isZero();
    }

    @Test
    public void differsFromCurrentOnlyForPreReleases() {
        // Where no pre-release qualifier other than the exact "SNAPSHOT" is involved, both comparisons must agree on
        // every pair
        final String[] neutral = { "0.9.9", "1.0.0-SNAPSHOT", "1.0.0", "1.0.0-01", "1.0.0-1", "1.0.0-9", "1.0.0-13", "1.0.0-a", "1.0.0-b",
                "1.0.0-alphax", "1.0.0-hotfix03", "1.0.0-zzz", "1.0.1", "2.0.0" };
        for (final String lhs : neutral) {
            for (final String rhs : neutral) {
                final DVRVersion verLhs = parse(lhs);
                final DVRVersion verRhs = parse(rhs);
                assertThat(Integer.signum(verLhs.compareToClassic(verRhs))).as("'%s' <=> '%s'", lhs, rhs)
                        .isEqualTo(Integer.signum(verLhs.compareTo(verRhs)));
            }
        }
    }

    @Test
    public void currentAndClassicDisagreeOnPreReleases() {
        // The whole point of the classic comparison - the two orderings are inverted for the pre-release qualifiers
        for (final String qualifier : new String[] { "alpha", "alpha1", "beta", "beta7", "milestone", "milestone2", "rc", "rc1", "RC2",
                "snapshot" }) {
            final DVRVersion pre = parse("1.0.0-" + qualifier);
            final DVRVersion release = parse("1.0.0");
            assertThat(pre.compareTo(release)).as("1.0.0-%s", qualifier).isNegative();
            assertThat(pre.compareToClassic(release)).as("1.0.0-%s", qualifier).isPositive();
        }

        // "rc9" vs. "rc10" is compared numerically now and as a String before
        final DVRVersion rc9 = parse("1.0.0-rc9");
        final DVRVersion rc10 = parse("1.0.0-rc10");
        assertThat(rc9.compareTo(rc10)).isNegative();
        assertThat(rc9.compareToClassic(rc10)).isPositive();

        // The exact spelling "SNAPSHOT" behaves identically in both
        final DVRVersion snapshot = parse("1.0.0-SNAPSHOT");
        final DVRVersion release = parse("1.0.0");
        assertThat(snapshot.compareTo(release)).isNegative();
        assertThat(snapshot.compareToClassic(release)).isNegative();
    }

    @Test
    public void classicMatchesPreviousImplementation() {
        // Cross check against the implementation of Version, which the classic comparison delegates to for all
        // non-snapshot versions
        final String[] versions = { "0.9.9", "1.0.0", "1.0.0-01", "1.0.0-a", "1.0.0-rc1", "1.0.0-zzz", "1.0.1", "2.0.0" };
        for (final String lhs : versions) {
            for (final String rhs : versions) {
                final DVRVersion verLhs = parse(lhs);
                final DVRVersion verRhs = parse(rhs);
                assertThat(Integer.signum(verLhs.compareToClassic(verRhs))).as("'%s' <=> '%s'", lhs, rhs)
                        .isEqualTo(Integer.signum(verLhs.getStaticVersion().compareTo(verRhs.getStaticVersion())));
            }
        }
    }
}
