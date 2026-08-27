package org.kosit.base.coord;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.coord.version.DVRVersion;
import org.kosit.base.string.StringHelper;

/**
 * The DVR Coordinate represents the coordinate of a single technical artefact in a specific version.
 * <p>
 * This is the read-only interface for a single DVR Coordinate.
 *
 * @author Philip Helger
 */
public interface IDVRCoordinate {

    /**
     * @return the coordinate's group ID. May never be <code>null</code> nor empty.
     */
    @NonNull
    @Nonempty
    String getGroupID();

    /**
     * @return the coordinate's artifact ID. May never be <code>null</code> nor empty.
     */
    @NonNull
    @Nonempty
    String getArtifactID();

    /**
     * @return the coordinate's version as a single String. May never be <code>null</code> nor empty.
     */
    default @NonNull @Nonempty String getVersionString() {
        return getVersionObj().getAsString();
    }

    /**
     * @return the coordinate's version object. Never <code>null</code>.
     */
    @NonNull
    DVRVersion getVersionObj();

    /**
     * @return <code>true</code> if a classifier is present, <code>false</code> if not.
     */
    default boolean hasClassifier() {
        return StringHelper.isNotEmpty(getClassifier());
    }

    /**
     * @return the coordinate's optional classifier ID. May be <code>null</code> or empty.
     */
    @Nullable
    String getClassifier();

    /**
     * @return a joint String representation of the coordinates. The different parts are separated by a colon (:)
     *         character. Never <code>null</code> nor empty.
     */
    @NonNull
    @Nonempty
    String getAsSingleID();
}
