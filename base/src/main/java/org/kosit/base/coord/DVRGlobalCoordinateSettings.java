package org.kosit.base.coord;

import org.conformatron.api.annotation.Nonnegative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains global settings for every DVR coordinate used. It modifies the validity of all DVR coordinates
 * around, so handle with care.
 *
 * @author Philip Helger
 */
public final class DVRGlobalCoordinateSettings {

    public static final int DEFAULT_MIN_LEN = 1;

    public static final int DEFAULT_GROUP_ID_MAX_LEN = 64;

    public static final int DEFAULT_ARTIFACT_ID_MAX_LEN = 64;

    public static final int DEFAULT_VERSION_MAX_LEN = 64;

    public static final int DEFAULT_CLASSIFIER_MAX_LEN = 64;

    private static final Logger LOGGER = LoggerFactory.getLogger(DVRGlobalCoordinateSettings.class);

    private static int groupIDMinLen = DEFAULT_MIN_LEN;

    private static int groupIDMaxLen = DEFAULT_GROUP_ID_MAX_LEN;

    private static int artifactIDMinLen = DEFAULT_MIN_LEN;

    private static int artifactIDMaxLen = DEFAULT_ARTIFACT_ID_MAX_LEN;

    private static int versionMinLen = DEFAULT_MIN_LEN;

    private static int versionMaxLen = DEFAULT_VERSION_MAX_LEN;

    private static int classifierMinLen = DEFAULT_MIN_LEN;

    private static int classifierMaxLen = DEFAULT_CLASSIFIER_MAX_LEN;

    public static @Nonnegative int getGroupIDMinLen() {
        return groupIDMinLen;
    }

    public static @Nonnegative int getGroupIDMaxLen() {
        return groupIDMaxLen;
    }

    public static void setGroupIDMaxLen(final @Nonnegative int maxLen) {
        requireGT0(maxLen);
        if (maxLen != groupIDMaxLen) {
            LOGGER.warn("Changed the maximum group ID length of DVR Coordinate from " + groupIDMaxLen + " to " + maxLen);
            groupIDMaxLen = maxLen;
        }
    }

    public static @Nonnegative int getArtifactIDMinLen() {
        return artifactIDMinLen;
    }

    public static @Nonnegative int getArtifactIDMaxLen() {
        return artifactIDMaxLen;
    }

    public static void setArtifactIDMaxLen(final @Nonnegative int maxLen) {
        requireGT0(maxLen);
        if (maxLen != artifactIDMaxLen) {
            LOGGER.warn("Changed the maximum artifact ID length of DVR Coordinate from " + artifactIDMaxLen + " to " + maxLen);
            artifactIDMaxLen = maxLen;
        }
    }

    public static @Nonnegative int getVersionMinLen() {
        return versionMinLen;
    }

    public static @Nonnegative int getVersionMaxLen() {
        return versionMaxLen;
    }

    public static void setVersionMaxLen(final @Nonnegative int maxLen) {
        requireGT0(maxLen);
        if (maxLen != versionMaxLen) {
            LOGGER.warn("Changed the maximum version length of DVR Coordinate from " + versionMaxLen + " to " + maxLen);
            versionMaxLen = maxLen;
        }
    }

    public static @Nonnegative int getClassifierMinLen() {
        return classifierMinLen;
    }

    public static @Nonnegative int getClassifierMaxLen() {
        return classifierMaxLen;
    }

    public static void setClassifierMaxLen(final @Nonnegative int maxLen) {
        requireGT0(maxLen);
        if (maxLen != classifierMaxLen) {
            LOGGER.warn("Changed the maximum classifier length of DVR Coordinate from " + classifierMaxLen + " to " + maxLen);
            classifierMaxLen = maxLen;
        }
    }

    private static void requireGT0(final int maxLen) {
        if (maxLen <= 0) {
            throw new IllegalArgumentException("MaxLen must be > 0 but is " + maxLen);
        }
    }

    private DVRGlobalCoordinateSettings() {
    }
}
