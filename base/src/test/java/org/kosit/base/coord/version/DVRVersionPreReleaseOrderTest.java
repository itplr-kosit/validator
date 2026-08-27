package org.kosit.base.coord.version;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

/**
 * Test class for the pre-release version qualifier ordering of {@link DVRVersion}.
 *
 * @author Philip Helger
 */
public class DVRVersionPreReleaseOrderTest {

    private static @NonNull DVRVersion parse(@NonNull final String version) {
        final DVRVersion ret = DVRVersion.parseOrNull(version);
        assertThat(ret).as("Failed to parse '%s'", version).isNotNull();
        return ret;
    }

    /**
     * Assert that the provided versions are in strictly ascending order. Every pair is checked in both directions, so
     * this also covers antisymmetry and transitivity.
     *
     * @param versions the versions in the expected ascending order.
     */
    private static void assertStrictlyAscending(@NonNull final String... versions) {
        final List<DVRVersion> list = new ArrayList<>();
        for (final String s : versions) {
            list.add(parse(s));
        }

        for (int i = 0; i < list.size(); ++i) {
            for (int j = 0; j < list.size(); ++j) {
                final int cmp = list.get(i).compareTo(list.get(j));
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

        // Sorting a scrambled copy must lead to the original order. Using a deterministic permutation - gcd (17, size)
        // is 1 for all sizes used here, so this really is a permutation
        final List<DVRVersion> scrambled = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i) {
            scrambled.add(list.get(i * 17 % list.size()));
        }
        assertThat(scrambled).hasSameSizeAs(list);
        Collections.sort(scrambled);
        assertThat(scrambled).isEqualTo(list);

        // All versions must remain distinguishable in a sorted set - if two of them compared as equal, one would
        // silently disappear
        final TreeSet<DVRVersion> sorted = new TreeSet<>(list);
        assertThat(sorted).hasSameSizeAs(list);
        assertThat(new ArrayList<>(sorted)).isEqualTo(list);
    }

    @Test
    public void fullQualifierChain() {
        // The complete ordering of one and the same numeric version
        assertStrictlyAscending("1.0.0-SNAPSHOT", "1.0.0-alpha", "1.0.0-alpha0", "1.0.0-alpha1", "1.0.0-alpha2", "1.0.0-alpha9",
                "1.0.0-alpha10", "1.0.0-beta", "1.0.0-beta1", "1.0.0-beta20", "1.0.0-milestone", "1.0.0-milestone1", "1.0.0-milestone2",
                "1.0.0-rc", "1.0.0-rc1", "1.0.0-rc2", "1.0.0-rc9", "1.0.0-rc10", "1.0.0-rc100",
                // The final release
                "1.0.0",
                // Everything unknown comes after the release and keeps being compared as a String
                "1.0.0-01", "1.0.0-1", "1.0.0-a", "1.0.0-alphax", "1.0.0-hotfix03", "1.0.0-zzz");
    }

    @Test
    public void numericPartsWinOverQualifier() {
        assertStrictlyAscending("0.9.9-zzz", "1.0.0-SNAPSHOT", "1.0.0-rc1", "1.0.0", "1.0.0-zzz", "1.0.1-SNAPSHOT");

        // A release candidate of the next version is still newer than the previous release
        assertStrictlyAscending("1.2.3", "1.2.4-alpha1", "1.2.4-rc1", "1.2.4");

        assertStrictlyAscending("0.9.0-rc", "1.0.0-alpha");
    }

    @Test
    public void rcBeforeRelease() {
        // The actual reason for this feature
        assertStrictlyAscending("1.0.0-RC2", "1.0.0");
        assertStrictlyAscending("1.15.0-rc", "1.15.0");
        assertStrictlyAscending("1.15.0-rc", "1.15.0", "1.15.1");
    }

    @Test
    public void caseInsensitiveRank() {
        // Different spellings must all have the same rank, hence they are all sorted between "beta9" and the release
        for (final String rc : new String[] { "RC1", "rc1", "Rc1", "rC1" }) {
            assertStrictlyAscending("1.0.0-beta9", "1.0.0-" + rc, "1.0.0");
        }

        for (final String snapshot : new String[] { "SNAPSHOT", "snapshot", "Snapshot", "SnApShOt" }) {
            assertStrictlyAscending("0.9.9", "1.0.0-" + snapshot, "1.0.0-alpha", "1.0.0");
        }

        for (final String alpha : new String[] { "ALPHA", "alpha", "Alpha" }) {
            assertStrictlyAscending("1.0.0-SNAPSHOT", "1.0.0-" + alpha, "1.0.0-beta", "1.0.0");
        }

        for (final String milestone : new String[] { "MILESTONE2", "milestone2", "MileStone2" }) {
            assertStrictlyAscending("1.0.0-beta", "1.0.0-" + milestone, "1.0.0-rc", "1.0.0");
        }
    }

    @Test
    public void differentSpellingsAreNeverEqual() {
        // Same rank and same number, but different Strings. They must not compare as equal, otherwise they would
        // collapse in a sorted set or map
        assertStrictlyAscending("1.0.0-RC1", "1.0.0-Rc1", "1.0.0-rC1", "1.0.0-rc1");
        assertStrictlyAscending("1.0.0-SNAPSHOT", "1.0.0-Snapshot", "1.0.0-snapshot");

        // Leading zeroes are irrelevant for the rank, but the versions are still different
        assertStrictlyAscending("1.0.0-rc01", "1.0.0-rc1");
        assertStrictlyAscending("1.0.0-rc001", "1.0.0-rc01", "1.0.0-rc1");

        // Cross check: the versions really are not equal
        assertThat(parse("1.0.0-RC1")).isNotEqualTo(parse("1.0.0-rc1"));
        assertThat(parse("1.0.0-rc01")).isNotEqualTo(parse("1.0.0-rc1"));
    }

    @Test
    public void unknownQualifiersKeepStringOrder() {
        // These are real world coordinates from phive-rules that must not be reinterpreted as pre-release versions

        // "a" is a revision letter, not "alpha"
        assertStrictlyAscending("1.3.6", "1.3.6-a");
        assertStrictlyAscending("1.3.5", "1.3.6", "1.3.6-a", "1.3.7");

        // "b" is a revision letter, not "beta"
        assertStrictlyAscending("1.3.6", "1.3.6-a", "1.3.6-b");

        // A numeric hotfix number supersedes the release
        assertStrictlyAscending("1.4.0", "1.4.0-03");
        assertStrictlyAscending("1.3.1", "1.4.0", "1.4.0-03", "1.4.1");

        // Zero padded numeric classifiers
        assertStrictlyAscending("2.0.3", "2.0.3-01", "2.0.3-02", "2.0.3-09", "2.0.3-10", "2.0.3-13");

        // Unpadded numeric classifiers are still compared as Strings, which is exactly why the padding is needed
        assertStrictlyAscending("2.0.3-13", "2.0.3-9");

        // Other qualifiers seen in the wild
        assertStrictlyAscending("1.4.0", "1.4.0-hotfix01", "1.4.0-hotfix02");
    }

    @Test
    public void alphaXIsNotAnAlpha() {
        // "alphax" is not a pre-release qualifier. The keyword must match as a whole, optionally followed by digits
        // only - any other trailing text makes it an ordinary qualifier
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("alphax")).isNull();
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("ALPHAX")).isNull();
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("AlphaX")).isNull();
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("alphax1")).isNull();
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("alpha1x")).isNull();
        assertThat(EDVRPreReleaseQualifier.ALPHA.getNumber("alphax")).isEqualTo(EDVRPreReleaseQualifier.NO_NUMBER);

        // Therefore "1.0.0-alphax" is ordered AFTER the release "1.0.0" ...
        assertStrictlyAscending("1.0.0", "1.0.0-alphax");

        // ... whereas a real "1.0.0-alpha" is ordered BEFORE the release
        assertStrictlyAscending("1.0.0-alpha", "1.0.0");

        // Both side by side
        assertStrictlyAscending("1.0.0-alpha", "1.0.0-alpha1", "1.0.0", "1.0.0-alphax");

        // The case insensitive matching must not change that
        assertStrictlyAscending("1.0.0-ALPHA", "1.0.0", "1.0.0-ALPHAX");

        // It is not a SNAPSHOT either, so it is treated like any other release and is accepted by the "latest-release"
        // pseudo version
        final DVRVersion ver = parse("1.0.0-alphax");
        assertThat(ver.isStaticSnapshotVersion()).isFalse();
        assertThat(DVRVersion.getStaticVersionAcceptor(null, false).test(ver)).isTrue();

        // Being an ordinary qualifier, it keeps being compared as a String
        assertStrictlyAscending("1.0.0-alphax", "1.0.0-alphay", "1.0.0-alphaz");
    }

    @Test
    public void partialKeywordsAreNotPreReleases() {
        // None of these is a pre-release qualifier, hence they all sort after the release, in String order
        assertStrictlyAscending("1.0.0", "1.0.0-alphabet");
        assertStrictlyAscending("1.0.0", "1.0.0-betamax");
        assertStrictlyAscending("1.0.0", "1.0.0-rcx");
        assertStrictlyAscending("1.0.0", "1.0.0-snapshot1");
        assertStrictlyAscending("1.0.0", "1.0.0-rc1a");

        // A separated number is not supported
        assertStrictlyAscending("1.0.0", "1.0.0-rc.1");

        // But the plain keyword still is
        assertStrictlyAscending("1.0.0-rc", "1.0.0", "1.0.0-rc.1");
    }

    @Test
    public void mixedLargeSetSorting() {
        // A larger set that spans several numeric versions, so that the sorting really exercises the merge path of the
        // sort algorithm
        assertStrictlyAscending("1.0.0-SNAPSHOT", "1.0.0-alpha1", "1.0.0-alpha10", "1.0.0-beta1", "1.0.0-milestone1", "1.0.0-rc1",
                "1.0.0-rc10", "1.0.0", "1.0.0-01", "1.0.0-a", "1.0.1-SNAPSHOT", "1.0.1-alpha1", "1.0.1-alpha10", "1.0.1-beta1",
                "1.0.1-milestone1", "1.0.1-rc1", "1.0.1-rc10", "1.0.1", "1.0.1-01", "1.0.1-a", "1.1.0-SNAPSHOT", "1.1.0-alpha1",
                "1.1.0-alpha10", "1.1.0-beta1", "1.1.0-milestone1", "1.1.0-rc1", "1.1.0-rc10", "1.1.0", "1.1.0-01", "1.1.0-a",
                "2.0.0-SNAPSHOT", "2.0.0-alpha1", "2.0.0-alpha10", "2.0.0-beta1", "2.0.0-milestone1", "2.0.0-rc1", "2.0.0-rc10", "2.0.0",
                "2.0.0-01", "2.0.0-a");
    }

    @Test
    public void pseudoVersionsUnaffected() {
        // A pre-release is still a static version, so all pseudo version rules continue to apply
        final DVRVersion oldest = DVRVersion.of(DVRPseudoVersionRegistry.OLDEST);
        final DVRVersion latest = DVRVersion.of(DVRPseudoVersionRegistry.LATEST);
        final DVRVersion latestRelease = DVRVersion.of(DVRPseudoVersionRegistry.LATEST_RELEASE);

        for (final String s : new String[] { "1.0.0-SNAPSHOT", "1.0.0-alpha1", "1.0.0-rc1", "1.0.0" }) {
            final DVRVersion ver = parse(s);
            assertThat(oldest.compareTo(ver)).as(s).isNegative();
            assertThat(ver.compareTo(oldest)).as(s).isPositive();
            assertThat(latest.compareTo(ver)).as(s).isPositive();
            assertThat(ver.compareTo(latest)).as(s).isNegative();
            assertThat(latestRelease.compareTo(ver)).as(s).isPositive();
            assertThat(ver.compareTo(latestRelease)).as(s).isNegative();
        }
    }

    @Test
    public void isStaticSnapshotVersion() {
        // The SNAPSHOT detection is case insensitive as well
        assertThat(parse("1.0.0-SNAPSHOT").isStaticSnapshotVersion()).isTrue();
        assertThat(parse("1.0.0-snapshot").isStaticSnapshotVersion()).isTrue();
        assertThat(parse("1.0.0-Snapshot").isStaticSnapshotVersion()).isTrue();
        assertThat(DVRVersion.isStaticSnapshotVersion("SNAPSHOT")).isTrue();
        assertThat(DVRVersion.isStaticSnapshotVersion("snapshot")).isTrue();

        // Everything else is not a snapshot
        assertThat(parse("1.0.0").isStaticSnapshotVersion()).isFalse();
        assertThat(parse("1.0.0-alpha1").isStaticSnapshotVersion()).isFalse();
        assertThat(parse("1.0.0-rc1").isStaticSnapshotVersion()).isFalse();
        assertThat(parse("1.0.0-snapshot1").isStaticSnapshotVersion()).isFalse();
        assertThat(DVRVersion.isStaticSnapshotVersion((String) null)).isFalse();
        assertThat(DVRVersion.isStaticSnapshotVersion("")).isFalse();
    }

    @Test
    public void staticVersionAcceptorOnlyFiltersSnapshots() {
        // Note: "latest-release" excludes SNAPSHOT versions only. The other pre-release qualifiers are deliberately
        // NOT excluded
        final var acceptor = DVRVersion.getStaticVersionAcceptor(null, false);
        assertThat(acceptor.test(parse("1.0.0-SNAPSHOT"))).isFalse();
        assertThat(acceptor.test(parse("1.0.0-snapshot"))).isFalse();
        assertThat(acceptor.test(parse("1.0.0-alpha1"))).isTrue();
        assertThat(acceptor.test(parse("1.0.0-rc1"))).isTrue();
        assertThat(acceptor.test(parse("1.0.0"))).isTrue();

        final var acceptorAll = DVRVersion.getStaticVersionAcceptor(null, true);
        assertThat(acceptorAll.test(parse("1.0.0-SNAPSHOT"))).isTrue();
        assertThat(acceptorAll.test(parse("1.0.0-rc1"))).isTrue();

        // Explicitly ignored versions - they are matched against the canonical String representation, so "1.0.0"
        // must be listed as "1"
        final var acceptorIgnore = DVRVersion.getStaticVersionAcceptor(Set.of("1"), true);
        assertThat(acceptorIgnore.test(parse("1.0.0"))).isFalse();
        assertThat(acceptorIgnore.test(parse("1.0.1"))).isTrue();
        assertThat(acceptorIgnore.test(parse("1.0.1-SNAPSHOT"))).isTrue();

        final var acceptorIgnoreNoSnapshot = DVRVersion.getStaticVersionAcceptor(Set.of("1"), false);
        assertThat(acceptorIgnoreNoSnapshot.test(parse("1.0.0"))).isFalse();
        assertThat(acceptorIgnoreNoSnapshot.test(parse("1.0.1"))).isTrue();
        assertThat(acceptorIgnoreNoSnapshot.test(parse("1.0.1-SNAPSHOT"))).isFalse();
    }

    @Test
    public void stringRepresentationIsUnchanged() {
        // The ordering is case insensitive, but the version itself is not touched - the original spelling must be
        // preserved and stay round trip safe
        for (final String qualifier : new String[] { "SNAPSHOT", "snapshot", "RC1", "rc1", "Alpha7", "milestone2", "03", "a" }) {
            final DVRVersion ver = parse("1.4.0-" + qualifier);
            assertThat(ver.getAsString()).isEqualTo("1.4-" + qualifier);
            assertThat(parse(ver.getAsString())).isEqualTo(ver);
        }
    }
}
