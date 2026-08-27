package org.kosit.base.coord;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.kosit.base.string.StringHelper;

/**
 * Test class for class {@link DVRValidityHelper}.
 *
 * @author Philip Helger
 */
public class DVRValidityHelperTest {

    @Test
    public void isValid() {
        assertThat(DVRValidityHelper.isValidCoordinateGroupID("com")).isTrue();
        assertThat(DVRValidityHelper.isValidCoordinateGroupID("com.helger")).isTrue();
        assertThat(DVRValidityHelper.isValidCoordinateGroupID("01234")).isTrue();
        assertThat(DVRValidityHelper.isValidCoordinateGroupID("1.2.3.4.5")).isTrue();
        assertThat(DVRValidityHelper.isValidCoordinateGroupID("ph-as4")).isTrue();
        assertThat(DVRValidityHelper.isValidCoordinateGroupID("-.___")).isTrue();

        assertThat(DVRValidityHelper.isValidCoordinateGroupID(null)).isFalse();
        assertThat(DVRValidityHelper.isValidCoordinateGroupID("")).isFalse();
        assertThat(DVRValidityHelper.isValidCoordinateGroupID("ä")).isFalse();
        assertThat(DVRValidityHelper.isValidCoordinateGroupID("a:b")).isFalse();

        // Max length
        assertThat(
                DVRValidityHelper.isValidCoordinateGroupID(StringHelper.repeat('a', DVRGlobalCoordinateSettings.DEFAULT_GROUP_ID_MAX_LEN)))
                        .isTrue();
        assertThat(DVRValidityHelper
                .isValidCoordinateGroupID(StringHelper.repeat('a', DVRGlobalCoordinateSettings.DEFAULT_GROUP_ID_MAX_LEN + 1))).isFalse();
    }

    @Test
    public void maxGroupIDLen() {
        assertThat(DVRCoordinate.parseOrNull("group:artifact:1.0:classifier")).isNotNull();
        final int old = DVRGlobalCoordinateSettings.getGroupIDMaxLen();
        DVRGlobalCoordinateSettings.setGroupIDMaxLen(1);
        try {
            // Too long
            assertThat(DVRCoordinate.parseOrNull("group:artifact:1.0:classifier")).isNull();
            // Valid
            assertThat(DVRCoordinate.parseOrNull("g:artifact:1.0:classifier")).isNotNull();
        } finally {
            DVRGlobalCoordinateSettings.setGroupIDMaxLen(old);
        }
    }

    @Test
    public void maxArtifactIDLen() {
        assertThat(DVRCoordinate.parseOrNull("group:artifact:1.0:classifier")).isNotNull();
        final int old = DVRGlobalCoordinateSettings.getArtifactIDMaxLen();
        DVRGlobalCoordinateSettings.setArtifactIDMaxLen(1);
        try {
            // Too long
            assertThat(DVRCoordinate.parseOrNull("group:artifact:1.0:classifier")).isNull();
            // Valid
            assertThat(DVRCoordinate.parseOrNull("group:a:1.0:classifier")).isNotNull();
        } finally {
            DVRGlobalCoordinateSettings.setArtifactIDMaxLen(old);
        }
    }

    @Test
    public void maxVersionLen() {
        assertThat(DVRCoordinate.parseOrNull("group:artifact:1.0:classifier")).isNotNull();
        final int old = DVRGlobalCoordinateSettings.getVersionMaxLen();
        DVRGlobalCoordinateSettings.setVersionMaxLen(1);
        try {
            // Too long
            assertThat(DVRCoordinate.parseOrNull("group:artifact:1.0:classifier")).isNull();
            // Valid
            assertThat(DVRCoordinate.parseOrNull("group:artifact:1:classifier")).isNotNull();
        } finally {
            DVRGlobalCoordinateSettings.setVersionMaxLen(old);
        }
    }

    @Test
    public void maxClassifierLen() {
        assertThat(DVRCoordinate.parseOrNull("group:artifact:1.0:classifier")).isNotNull();
        final int old = DVRGlobalCoordinateSettings.getClassifierMaxLen();
        DVRGlobalCoordinateSettings.setClassifierMaxLen(1);
        try {
            // Too long
            assertThat(DVRCoordinate.parseOrNull("group:artifact:1.0:classifier")).isNull();
            // Valid
            assertThat(DVRCoordinate.parseOrNull("group:artifact:1.0:c")).isNotNull();
        } finally {
            DVRGlobalCoordinateSettings.setClassifierMaxLen(old);
        }
    }
}
