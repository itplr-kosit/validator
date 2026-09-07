package org.kosit.base.coord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class DVRGlobalCoordinateSettingsTest {

    @AfterEach
    public void restoreDefaults() {
        DVRGlobalCoordinateSettings.setGroupIDMaxLen(DVRGlobalCoordinateSettings.DEFAULT_GROUP_ID_MAX_LEN);
        DVRGlobalCoordinateSettings.setArtifactIDMaxLen(DVRGlobalCoordinateSettings.DEFAULT_ARTIFACT_ID_MAX_LEN);
        DVRGlobalCoordinateSettings.setVersionMaxLen(DVRGlobalCoordinateSettings.DEFAULT_VERSION_MAX_LEN);
        DVRGlobalCoordinateSettings.setClassifierMaxLen(DVRGlobalCoordinateSettings.DEFAULT_CLASSIFIER_MAX_LEN);
    }

    @Test
    public void defaults() {
        assertThat(DVRGlobalCoordinateSettings.getGroupIDMinLen()).isEqualTo(DVRGlobalCoordinateSettings.DEFAULT_MIN_LEN);
        assertThat(DVRGlobalCoordinateSettings.getGroupIDMaxLen()).isEqualTo(DVRGlobalCoordinateSettings.DEFAULT_GROUP_ID_MAX_LEN);
        assertThat(DVRGlobalCoordinateSettings.getArtifactIDMinLen()).isEqualTo(DVRGlobalCoordinateSettings.DEFAULT_MIN_LEN);
        assertThat(DVRGlobalCoordinateSettings.getArtifactIDMaxLen()).isEqualTo(DVRGlobalCoordinateSettings.DEFAULT_ARTIFACT_ID_MAX_LEN);
        assertThat(DVRGlobalCoordinateSettings.getVersionMinLen()).isEqualTo(DVRGlobalCoordinateSettings.DEFAULT_MIN_LEN);
        assertThat(DVRGlobalCoordinateSettings.getVersionMaxLen()).isEqualTo(DVRGlobalCoordinateSettings.DEFAULT_VERSION_MAX_LEN);
        assertThat(DVRGlobalCoordinateSettings.getClassifierMinLen()).isEqualTo(DVRGlobalCoordinateSettings.DEFAULT_MIN_LEN);
        assertThat(DVRGlobalCoordinateSettings.getClassifierMaxLen()).isEqualTo(DVRGlobalCoordinateSettings.DEFAULT_CLASSIFIER_MAX_LEN);
    }

    @Test
    public void setMaxLengths() {
        DVRGlobalCoordinateSettings.setGroupIDMaxLen(10);
        DVRGlobalCoordinateSettings.setArtifactIDMaxLen(11);
        DVRGlobalCoordinateSettings.setVersionMaxLen(12);
        DVRGlobalCoordinateSettings.setClassifierMaxLen(13);

        assertThat(DVRGlobalCoordinateSettings.getGroupIDMaxLen()).isEqualTo(10);
        assertThat(DVRGlobalCoordinateSettings.getArtifactIDMaxLen()).isEqualTo(11);
        assertThat(DVRGlobalCoordinateSettings.getVersionMaxLen()).isEqualTo(12);
        assertThat(DVRGlobalCoordinateSettings.getClassifierMaxLen()).isEqualTo(13);
    }

    @Test
    public void settingTheSameValueAgainIsAllowed() {
        DVRGlobalCoordinateSettings.setGroupIDMaxLen(DVRGlobalCoordinateSettings.getGroupIDMaxLen());
        assertThat(DVRGlobalCoordinateSettings.getGroupIDMaxLen()).isEqualTo(DVRGlobalCoordinateSettings.DEFAULT_GROUP_ID_MAX_LEN);
    }

    @Test
    public void maxLengthMustBeGreaterThanZero() {
        for (final int invalid : new int[] { 0, -1 }) {
            assertThatThrownBy(() -> DVRGlobalCoordinateSettings.setGroupIDMaxLen(invalid)).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("MaxLen must be > 0");
            assertThatThrownBy(() -> DVRGlobalCoordinateSettings.setArtifactIDMaxLen(invalid)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> DVRGlobalCoordinateSettings.setVersionMaxLen(invalid)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> DVRGlobalCoordinateSettings.setClassifierMaxLen(invalid)).isInstanceOf(IllegalArgumentException.class);
        }
        // nothing was changed
        assertThat(DVRGlobalCoordinateSettings.getGroupIDMaxLen()).isEqualTo(DVRGlobalCoordinateSettings.DEFAULT_GROUP_ID_MAX_LEN);
    }
}
