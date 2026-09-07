package org.kosit.base.coord.version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.kosit.base.version.Version;

/**
 * Test class for class {@link DVRPseudoVersion} and the pseudo versions of {@link DVRPseudoVersionRegistry}.
 *
 * @author Philip Helger
 */
public class DVRPseudoVersionTest {

    private static final class ConstantComparable implements IDVRPseudoVersionComparable {

        private final int result;

        private ConstantComparable(final int result) {
            this.result = result;
        }

        public int compareToPseudoVersion(final IDVRPseudoVersion otherPseudoVersion) {
            return this.result;
        }

        public int compareToVersion(final Version otherStaticVersion) {
            return this.result;
        }
    }

    private static final Version VERSION = new Version(1, 2, 3);

    @Test
    public void basic() {
        final IDVRPseudoVersionComparable comparable = new ConstantComparable(+1);
        final DVRPseudoVersion pv = new DVRPseudoVersion("nightly", comparable);

        assertThat(pv.getID()).isEqualTo("nightly");
        assertThat(pv.getPseudoVersionComparable()).isSameAs(comparable);
    }

    @Test
    public void constructorParametersAreChecked() {
        final IDVRPseudoVersionComparable comparable = new ConstantComparable(0);
        assertThatThrownBy(() -> new DVRPseudoVersion(null, comparable)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID must not be empty");
        assertThatThrownBy(() -> new DVRPseudoVersion("", comparable)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DVRPseudoVersion("nightly", null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Comparable");
    }

    @Test
    public void theSameIdIsAlwaysIdentical() {
        // the comparable is not asked at all for the same ID
        final DVRPseudoVersion pv = new DVRPseudoVersion("nightly", new ConstantComparable(+1));
        assertThat(pv.compareToPseudoVersion(new DVRPseudoVersion("nightly", new ConstantComparable(-1)))).isZero();
    }

    @Test
    public void comparingIsDelegatedToTheComparable() {
        final DVRPseudoVersion pv = new DVRPseudoVersion("nightly", new ConstantComparable(-1));
        assertThat(pv.compareToPseudoVersion(DVRPseudoVersionRegistry.LATEST)).isNegative();
        assertThat(pv.compareToVersion(VERSION)).isNegative();

        final DVRPseudoVersion greater = new DVRPseudoVersion("nightly", new ConstantComparable(+1));
        assertThat(greater.compareToPseudoVersion(DVRPseudoVersionRegistry.LATEST)).isPositive();
        assertThat(greater.compareToVersion(VERSION)).isPositive();
    }

    @Test
    public void comparingRejectsNull() {
        final DVRPseudoVersion pv = new DVRPseudoVersion("nightly", new ConstantComparable(0));
        assertThatThrownBy(() -> pv.compareToPseudoVersion(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> pv.compareToVersion(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void equalsAndHashCodeUseTheIdOnly() {
        final DVRPseudoVersion pv = new DVRPseudoVersion("nightly", new ConstantComparable(0));
        assertThat(pv).isEqualTo(pv);
        assertThat(pv).isEqualTo(new DVRPseudoVersion("nightly", new ConstantComparable(+1)));
        assertThat(pv).hasSameHashCodeAs(new DVRPseudoVersion("nightly", new ConstantComparable(+1)));
        assertThat(pv).isNotEqualTo(new DVRPseudoVersion("weekly", new ConstantComparable(0)));
        assertThat(pv).isNotEqualTo(null);
        assertThat(pv).isNotEqualTo("nightly");
    }

    @Test
    public void asString() {
        assertThat(new DVRPseudoVersion("nightly", new ConstantComparable(0)).toString()).contains("id=nightly").contains("comparable=");
    }

    @Test
    public void oldestIsSmallerThanEverything() {
        assertThat(DVRPseudoVersionRegistry.OLDEST.getID()).isEqualTo("oldest");
        assertThat(DVRPseudoVersionRegistry.OLDEST.compareToVersion(VERSION)).isNegative();
        assertThat(DVRPseudoVersionRegistry.OLDEST.compareToVersion(Version.DEFAULT_VERSION)).isNegative();
        assertThat(DVRPseudoVersionRegistry.OLDEST.compareToPseudoVersion(DVRPseudoVersionRegistry.LATEST)).isNegative();
        assertThat(DVRPseudoVersionRegistry.OLDEST.compareToPseudoVersion(DVRPseudoVersionRegistry.LATEST_RELEASE)).isNegative();
        assertThat(DVRPseudoVersionRegistry.OLDEST.compareToPseudoVersion(DVRPseudoVersionRegistry.OLDEST)).isZero();
    }

    @Test
    public void latestIsGreaterThanEverything() {
        assertThat(DVRPseudoVersionRegistry.LATEST.getID()).isEqualTo("latest");
        assertThat(DVRPseudoVersionRegistry.LATEST.compareToVersion(VERSION)).isPositive();
        assertThat(DVRPseudoVersionRegistry.LATEST.compareToPseudoVersion(DVRPseudoVersionRegistry.OLDEST)).isPositive();
        assertThat(DVRPseudoVersionRegistry.LATEST.compareToPseudoVersion(DVRPseudoVersionRegistry.LATEST_RELEASE)).isPositive();
        assertThat(DVRPseudoVersionRegistry.LATEST.compareToPseudoVersion(DVRPseudoVersionRegistry.LATEST)).isZero();
    }

    @Test
    public void latestReleaseIsBetweenTheStaticVersionsAndLatest() {
        assertThat(DVRPseudoVersionRegistry.LATEST_RELEASE.getID()).isEqualTo("latest-release");
        assertThat(DVRPseudoVersionRegistry.LATEST_RELEASE.compareToVersion(VERSION)).isPositive();
        assertThat(DVRPseudoVersionRegistry.LATEST_RELEASE.compareToPseudoVersion(DVRPseudoVersionRegistry.OLDEST)).isPositive();
        assertThat(DVRPseudoVersionRegistry.LATEST_RELEASE.compareToPseudoVersion(DVRPseudoVersionRegistry.LATEST)).isNegative();
        assertThat(DVRPseudoVersionRegistry.LATEST_RELEASE.compareToPseudoVersion(DVRPseudoVersionRegistry.LATEST_RELEASE)).isZero();
    }
}
