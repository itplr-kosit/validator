package org.kosit.base.coord;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.kosit.base.coord.version.DVRVersionException;

/**
 * Test class for class {@link DVRCoordinate}.
 *
 * @author Philip Helger
 */
public class DVRCoordinateTest {

    private static void assertEqualContent(final DVRCoordinate left, final DVRCoordinate right) {
        assertThat(left).isEqualTo(right).hasSameHashCodeAs(right);
        assertThat(right).isEqualTo(left);
    }

    private static void assertDifferentContent(final DVRCoordinate left, final DVRCoordinate right) {
        assertThat(left).isNotEqualTo(right);
        assertThat(right).isNotEqualTo(left);
    }

    @Test
    public void basic() throws DVRVersionException {
        final DVRCoordinate id1 = DVRCoordinate.create("com.helger", "phive", "3.0.0.SNAPSHOT");
        assertThat(id1.getGroupID()).isEqualTo("com.helger");
        assertThat(id1.getArtifactID()).isEqualTo("phive");
        assertThat(id1.getVersionString()).isEqualTo("3-SNAPSHOT");
        assertThat(id1.getClassifier()).isNull();
        assertThat(id1.hasClassifier()).isFalse();

        assertEqualContent(id1, DVRCoordinate.create("com.helger", "phive", "3.0.0.SNAPSHOT"));
        assertDifferentContent(id1, DVRCoordinate.create("com.holger", "phive", "3.0.0.SNAPSHOT"));
        assertDifferentContent(id1, DVRCoordinate.create("com.helger", "phivengine", "3.0.0.SNAPSHOT"));
        assertDifferentContent(id1, DVRCoordinate.create("com.helger", "phive", "3.0.0"));
        assertDifferentContent(id1, DVRCoordinate.create("com.helger", "phive", "3.0.0.SNAPSHOT", "src"));
    }

    @Test
    public void parseID() throws DVRVersionException {
        final DVRCoordinate id1 = DVRCoordinate.create("com.helger", "phive", "3.0.0.SNAPSHOT");
        assertThat(id1.getGroupID()).isEqualTo("com.helger");
        assertThat(id1.getArtifactID()).isEqualTo("phive");
        assertThat(id1.getVersionString()).isEqualTo("3-SNAPSHOT");
        assertThat(id1.getClassifier()).isNull();

        final DVRCoordinate id2 = id1.getWithClassifier("test");
        assertThat(id2.getGroupID()).isEqualTo("com.helger");
        assertThat(id2.getArtifactID()).isEqualTo("phive");
        assertThat(id2.getVersionString()).isEqualTo("3-SNAPSHOT");
        assertThat(id2.getClassifier()).isEqualTo("test");
        assertThat(id2.hasClassifier()).isTrue();

        assertThat(DVRCoordinate.parseOrNull(id1.getAsSingleID())).isEqualTo(id1);
        assertThat(DVRCoordinate.parseOrNull(id1.getAsSingleID() + DVRCoordinate.PART_SEPARATOR)).isEqualTo(id1);
        assertThat(DVRCoordinate.parseOrNull(id2.getAsSingleID())).isEqualTo(id2);
        assertThat(DVRCoordinate.parseOrNull(null)).isNull();
        assertThat(DVRCoordinate.parseOrNull("a")).isNull();
        assertThat(DVRCoordinate.parseOrNull("a:b")).isNull();
        assertThat(DVRCoordinate.parseOrNull("a:b:c:d:e")).isNull();
        assertThat(DVRCoordinate.parseOrNull("a:b:c:d:e:f")).isNull();
        assertThat(DVRCoordinate.parseOrNull("::")).isNull();
        assertThat(DVRCoordinate.parseOrNull(":::")).isNull();
        assertThat(DVRCoordinate.parseOrNull("a:b:")).isNull();
    }

    @Test
    public void versionClassifier() throws DVRVersionException {
        // Both separators lead to the same Coordinate
        final DVRCoordinate id1 = DVRCoordinate.create("fr.ctc", "cdar", "1.4.0-03");
        assertThat(id1.getVersionString()).isEqualTo("1.4-03");
        assertThat(id1.getAsSingleID()).isEqualTo("fr.ctc:cdar:1.4-03");
        assertThat(DVRCoordinate.create("fr.ctc", "cdar", "1.4.0.03")).isEqualTo(id1);

        // The version with the classifier differs from the one without
        assertDifferentContent(id1, DVRCoordinate.create("fr.ctc", "cdar", "1.4.0"));

        // A Coordinate with a version classifier survives a single ID round trip, numeric classifiers included
        final DVRCoordinate id2 = DVRCoordinate.create("fr.ctc", "cdar", "1.4.0-hotfix03");
        assertThat(id2.getAsSingleID()).isEqualTo("fr.ctc:cdar:1.4-hotfix03");
        assertThat(DVRCoordinate.parseOrNull(id2.getAsSingleID())).isEqualTo(id2);
        assertThat(DVRCoordinate.parseOrNull(id1.getAsSingleID())).isEqualTo(id1);

        // All spellings lead to the same Coordinate
        assertThat(DVRCoordinate.parseOrNull("fr.ctc:cdar:1.4-03")).isEqualTo(id1);
        assertThat(DVRCoordinate.parseOrNull("fr.ctc:cdar:1.4.0-03")).isEqualTo(id1);
        assertThat(DVRCoordinate.parseOrNull("fr.ctc:cdar:1.4.0.03")).isEqualTo(id1);
    }

    @Test
    public void getWithAndCompare() throws DVRVersionException {
        final DVRCoordinate id1 = DVRCoordinate.create("com.helger", "phive", "1.0.0");
        assertThat(id1.getWithGroupID("com.helger")).isSameAs(id1);
        assertThat(id1.getWithArtifactID("phive")).isSameAs(id1);
        assertThat(id1.getWithClassifier(null)).isSameAs(id1);
        assertThat(id1.getWithVersion(id1.getVersionObj())).isSameAs(id1);

        assertThat(id1.getWithGroupID("com.helger2").getGroupID()).isEqualTo("com.helger2");
        assertThat(id1.getWithArtifactID("phive2").getArtifactID()).isEqualTo("phive2");
        assertThat(id1.getWithVersionLatest().getVersionString()).isEqualTo("latest");
        assertThat(id1.getWithVersionLatestRelease().getVersionString()).isEqualTo("latest-release");

        // Group ID, then artifact ID, then version, then classifier
        assertThat(id1).isEqualByComparingTo(id1);
        assertThat(id1).isLessThan(DVRCoordinate.create("com.helger2", "phive", "1.0.0"));
        assertThat(id1).isLessThan(DVRCoordinate.create("com.helger", "phive2", "1.0.0"));
        assertThat(id1).isLessThan(DVRCoordinate.create("com.helger", "phive", "1.0.1"));
        assertThat(id1).isLessThan(DVRCoordinate.create("com.helger", "phive", "1.0.0", "src"));
    }
}
