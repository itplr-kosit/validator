package org.kosit.base.coord;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.conformatron.api.annotation.Nonnegative;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.string.StringHelper;

/**
 * Helper class to check DVR Coordinate consistency. It is provided in its own package, to avoid cyclic package
 * dependencies between "coord" and "version".
 *
 * @author Philip Helger
 */
public final class DVRValidityHelper {

    private static final Map<String, Pattern> REGEX_CACHE = new ConcurrentHashMap<>();

    private static boolean isValidPart(@NonNull final String part, final @Nonnegative int minLen, final @Nonnegative int maxLen) {
        if (minLen > maxLen) {
            throw new IllegalArgumentException("Min length (" + minLen + ") must be <= Max length (" + maxLen + ")");
        }
        return REGEX_CACHE.computeIfAbsent("[a-zA-Z0-9_\\-\\.]{" + minLen + "," + maxLen + "}", Pattern::compile).matcher(part).matches();
    }

    /**
     * Check if the provided part is a syntactically valid coordinate Group ID.
     *
     * @param part the part to be checked. May be <code>null</code>.
     * @return <code>true</code> if it is valid, <code>false</code> if not.
     */
    public static boolean isValidCoordinateGroupID(final @Nullable String part) {
        if (StringHelper.isEmpty(part)) {
            return false;
        }
        return isValidPart(part, DVRGlobalCoordinateSettings.getGroupIDMinLen(), DVRGlobalCoordinateSettings.getGroupIDMaxLen());
    }

    /**
     * Check if the provided part is a syntactically valid coordinate Artifact ID.
     *
     * @param part the part to be checked. May be <code>null</code>.
     * @return <code>true</code> if it is valid, <code>false</code> if not.
     */
    public static boolean isValidCoordinateArtifactID(final @Nullable String part) {
        if (StringHelper.isEmpty(part)) {
            return false;
        }
        return isValidPart(part, DVRGlobalCoordinateSettings.getArtifactIDMinLen(), DVRGlobalCoordinateSettings.getArtifactIDMaxLen());
    }

    /**
     * Check if the provided part is a syntactically valid coordinate Version.
     *
     * @param part the part to be checked. May be <code>null</code>.
     * @return <code>true</code> if it is valid, <code>false</code> if not.
     */
    public static boolean isValidCoordinateVersion(final @Nullable String part) {
        if (StringHelper.isEmpty(part)) {
            return false;
        }
        return isValidPart(part, DVRGlobalCoordinateSettings.getVersionMinLen(), DVRGlobalCoordinateSettings.getVersionMaxLen());
    }

    /**
     * Check if the provided part is a syntactically valid coordinate Classifier.
     *
     * @param part the part to be checked. May be <code>null</code>.
     * @return <code>true</code> if it is valid, <code>false</code> if not.
     */
    public static boolean isValidCoordinateClassifier(final @Nullable String part) {
        // Classifier is optional
        if (StringHelper.isEmpty(part)) {
            return true;
        }
        return isValidPart(part, DVRGlobalCoordinateSettings.getClassifierMinLen(), DVRGlobalCoordinateSettings.getClassifierMaxLen());
    }

    private DVRValidityHelper() {
    }
}
