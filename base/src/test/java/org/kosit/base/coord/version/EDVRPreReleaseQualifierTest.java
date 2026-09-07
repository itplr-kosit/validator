package org.kosit.base.coord.version;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Test class for class {@link EDVRPreReleaseQualifier}.
 *
 * @author Philip Helger
 */
public class EDVRPreReleaseQualifierTest {

    @Test
    public void getID() {
        for (final EDVRPreReleaseQualifier e : EDVRPreReleaseQualifier.values()) {
            assertThat(e.getID()).isNotEmpty();
            // All IDs are lower case, because the matching lower cases the input
            assertThat(e.getID()).isEqualTo(e.getID().toLowerCase(Locale.ROOT));
        }

        assertThat(EDVRPreReleaseQualifier.SNAPSHOT.getID()).isEqualTo("snapshot");
        assertThat(EDVRPreReleaseQualifier.ALPHA.getID()).isEqualTo("alpha");
        assertThat(EDVRPreReleaseQualifier.BETA.getID()).isEqualTo("beta");
        assertThat(EDVRPreReleaseQualifier.MILESTONE.getID()).isEqualTo("milestone");
        assertThat(EDVRPreReleaseQualifier.RC.getID()).isEqualTo("rc");
    }

    @Test
    public void rank() {
        // The rank defines the ordering
        final EDVRPreReleaseQualifier[] expected = { EDVRPreReleaseQualifier.SNAPSHOT, EDVRPreReleaseQualifier.ALPHA,
                EDVRPreReleaseQualifier.BETA, EDVRPreReleaseQualifier.MILESTONE, EDVRPreReleaseQualifier.RC };
        assertThat(EDVRPreReleaseQualifier.values()).hasSameSizeAs(expected);

        // All ranks must be strictly ascending in the expected order
        for (int i = 1; i < expected.length; ++i) {
            assertThat(expected[i - 1].getRank()).as("%s vs. %s", expected[i - 1].getID(), expected[i].getID())
                    .isLessThan(expected[i].getRank());
        }

        // All ranks must be distinct and positive
        final Set<Integer> ranks = new HashSet<>();
        for (final EDVRPreReleaseQualifier e : EDVRPreReleaseQualifier.values()) {
            assertThat(e.getRank()).as(e.getID()).isPositive();
            assertThat(ranks.add(Integer.valueOf(e.getRank()))).as("Rank " + e.getRank() + " is used more than once").isTrue();
        }
    }

    @Test
    public void maxRank() {
        // MAX_RANK must be the highest rank of all qualifiers
        for (final EDVRPreReleaseQualifier e : EDVRPreReleaseQualifier.values()) {
            assertThat(e.getRank()).as(e.getID()).isLessThanOrEqualTo(EDVRPreReleaseQualifier.MAX_RANK);
        }

        // ... and it must be reached by exactly one of them
        int count = 0;
        for (final EDVRPreReleaseQualifier e : EDVRPreReleaseQualifier.values()) {
            if (e.getRank() == EDVRPreReleaseQualifier.MAX_RANK) {
                count++;
            }
        }
        assertThat(count).isEqualTo(1);

        // The last pre-release qualifier before the release is the release candidate
        assertThat(EDVRPreReleaseQualifier.MAX_RANK).isEqualTo(EDVRPreReleaseQualifier.RC.getRank());
    }

    @Test
    public void numberSupported() {
        // Only SNAPSHOT must be used standalone
        assertThat(EDVRPreReleaseQualifier.SNAPSHOT.isNumberSupported()).isFalse();
        assertThat(EDVRPreReleaseQualifier.ALPHA.isNumberSupported()).isTrue();
        assertThat(EDVRPreReleaseQualifier.BETA.isNumberSupported()).isTrue();
        assertThat(EDVRPreReleaseQualifier.MILESTONE.isNumberSupported()).isTrue();
        assertThat(EDVRPreReleaseQualifier.RC.isNumberSupported()).isTrue();
    }

    @Test
    public void getFromQualifierExactMatch() {
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("snapshot")).isSameAs(EDVRPreReleaseQualifier.SNAPSHOT);
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("alpha")).isSameAs(EDVRPreReleaseQualifier.ALPHA);
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("beta")).isSameAs(EDVRPreReleaseQualifier.BETA);
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("milestone")).isSameAs(EDVRPreReleaseQualifier.MILESTONE);
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("rc")).isSameAs(EDVRPreReleaseQualifier.RC);
    }

    @Test
    public void getFromQualifierCaseInsensitive() {
        for (final String s : new String[] { "SNAPSHOT", "snapshot", "Snapshot", "SnApShOt", "sNAPSHOT" }) {
            assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull(s)).as(s).isSameAs(EDVRPreReleaseQualifier.SNAPSHOT);
        }

        for (final String s : new String[] { "RC", "rc", "Rc", "rC" }) {
            assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull(s)).as(s).isSameAs(EDVRPreReleaseQualifier.RC);
        }

        for (final String s : new String[] { "RC2", "rc2", "Rc2", "rC2" }) {
            assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull(s)).as(s).isSameAs(EDVRPreReleaseQualifier.RC);
        }

        for (final String s : new String[] { "ALPHA1", "alpha1", "Alpha1" }) {
            assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull(s)).as(s).isSameAs(EDVRPreReleaseQualifier.ALPHA);
        }

        for (final String s : new String[] { "BETA", "beta", "BeTa" }) {
            assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull(s)).as(s).isSameAs(EDVRPreReleaseQualifier.BETA);
        }

        for (final String s : new String[] { "MILESTONE", "milestone", "MileStone" }) {
            assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull(s)).as(s).isSameAs(EDVRPreReleaseQualifier.MILESTONE);
        }
    }

    @Test
    public void getFromQualifierCaseInsensitiveInTurkishLocale() {
        // "MILESTONE" contains an "I". In the Turkish locale the lower case of "I" is the dotless "i", so the matching
        // must not use the default locale
        final Locale oldDefault = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"));
            assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("MILESTONE")).isSameAs(EDVRPreReleaseQualifier.MILESTONE);
            assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("MILESTONE3")).isSameAs(EDVRPreReleaseQualifier.MILESTONE);
            assertThat(EDVRPreReleaseQualifier.MILESTONE.getNumber("MILESTONE3")).isEqualTo(3);
            assertThat(DVRVersion.isStaticSnapshotVersion("SNAPSHOT")).isTrue();
        } finally {
            Locale.setDefault(oldDefault);
        }
    }

    @Test
    public void getFromQualifierWithNumber() {
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("alpha0")).isSameAs(EDVRPreReleaseQualifier.ALPHA);
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("alpha1")).isSameAs(EDVRPreReleaseQualifier.ALPHA);
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("alpha10")).isSameAs(EDVRPreReleaseQualifier.ALPHA);
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("alpha007")).isSameAs(EDVRPreReleaseQualifier.ALPHA);
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("beta99")).isSameAs(EDVRPreReleaseQualifier.BETA);
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("milestone3")).isSameAs(EDVRPreReleaseQualifier.MILESTONE);
        assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull("rc123")).isSameAs(EDVRPreReleaseQualifier.RC);
    }

    @Test
    public void getFromQualifierNoMatch() {
        // Empty, single letter abbreviations, partial keywords, no whole match, separators, SNAPSHOT with a number,
        // numbers that do not fit into an int and plain qualifiers used in the wild
        for (final String s : new String[] { null, "", "a", "b", "m", "a1", "b2", "al", "alph", "mile", "r", "alphax", "ALPHAX", "alphax1",
                "alpha1x", "alphabet", "betamax", "rcx", "rc1a", "xrc", "prerc1", "snapshotty", "rc-1", "rc.1", "rc_1", "snapshot1",
                "SNAPSHOT2", "rc99999999999999", "03", "3", "hotfix03", "bla", "4.5.6.7.8" }) {
            assertThat(EDVRPreReleaseQualifier.getFromQualifierOrNull(s)).as(String.valueOf(s)).isNull();
        }
    }

    @Test
    public void getNumber() {
        // No number present
        assertThat(EDVRPreReleaseQualifier.RC.getNumber("rc")).isEqualTo(EDVRPreReleaseQualifier.NO_NUMBER);
        assertThat(EDVRPreReleaseQualifier.RC.getNumber("RC")).isEqualTo(EDVRPreReleaseQualifier.NO_NUMBER);
        assertThat(EDVRPreReleaseQualifier.ALPHA.getNumber("alpha")).isEqualTo(EDVRPreReleaseQualifier.NO_NUMBER);

        // Number present
        assertThat(EDVRPreReleaseQualifier.RC.getNumber("rc0")).isZero();
        assertThat(EDVRPreReleaseQualifier.RC.getNumber("rc1")).isEqualTo(1);
        assertThat(EDVRPreReleaseQualifier.RC.getNumber("RC2")).isEqualTo(2);
        assertThat(EDVRPreReleaseQualifier.RC.getNumber("Rc10")).isEqualTo(10);
        assertThat(EDVRPreReleaseQualifier.RC.getNumber("rc123")).isEqualTo(123);
        assertThat(EDVRPreReleaseQualifier.ALPHA.getNumber("alpha007")).isEqualTo(7);
        assertThat(EDVRPreReleaseQualifier.BETA.getNumber("BETA99")).isEqualTo(99);
        assertThat(EDVRPreReleaseQualifier.MILESTONE.getNumber("milestone3")).isEqualTo(3);

        // Leading zeroes are irrelevant for the number itself
        assertThat(EDVRPreReleaseQualifier.RC.getNumber("rc01")).isEqualTo(EDVRPreReleaseQualifier.RC.getNumber("rc1"));

        // Qualifier of a different pre-release qualifier
        assertThat(EDVRPreReleaseQualifier.RC.getNumber("alpha1")).isEqualTo(EDVRPreReleaseQualifier.NO_NUMBER);
        assertThat(EDVRPreReleaseQualifier.ALPHA.getNumber("rc1")).isEqualTo(EDVRPreReleaseQualifier.NO_NUMBER);

        // SNAPSHOT never has a number
        assertThat(EDVRPreReleaseQualifier.SNAPSHOT.getNumber("snapshot")).isEqualTo(EDVRPreReleaseQualifier.NO_NUMBER);
        assertThat(EDVRPreReleaseQualifier.SNAPSHOT.getNumber("snapshot1")).isEqualTo(EDVRPreReleaseQualifier.NO_NUMBER);

        // Unparsable
        assertThat(EDVRPreReleaseQualifier.RC.getNumber(null)).isEqualTo(EDVRPreReleaseQualifier.NO_NUMBER);
        assertThat(EDVRPreReleaseQualifier.RC.getNumber("")).isEqualTo(EDVRPreReleaseQualifier.NO_NUMBER);
        assertThat(EDVRPreReleaseQualifier.RC.getNumber("rcx")).isEqualTo(EDVRPreReleaseQualifier.NO_NUMBER);
        assertThat(EDVRPreReleaseQualifier.RC.getNumber("rc99999999999999")).isEqualTo(EDVRPreReleaseQualifier.NO_NUMBER);
    }

    @Test
    public void noQualifierIsPrefixOfAnother() {
        // The matching relies on the fact that no keyword is a prefix of another one - otherwise the iteration order
        // would matter
        for (final EDVRPreReleaseQualifier e1 : EDVRPreReleaseQualifier.values()) {
            for (final EDVRPreReleaseQualifier e2 : EDVRPreReleaseQualifier.values()) {
                if (e1 != e2) {
                    assertThat(e1.getID().startsWith(e2.getID())).as(e1.getID() + " vs. " + e2.getID()).isFalse();
                }
            }
        }
    }
}
