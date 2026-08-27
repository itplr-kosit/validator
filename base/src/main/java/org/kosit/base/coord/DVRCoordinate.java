package org.kosit.base.coord;

import java.util.List;
import java.util.Objects;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.coord.version.DVRPseudoVersionRegistry;
import org.kosit.base.coord.version.DVRVersion;
import org.kosit.base.coord.version.DVRVersionException;
import org.kosit.base.coord.version.IDVRPseudoVersion;
import org.kosit.base.string.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DVR Coordinate represents the coordinate of a single technical artefact in a specific version. This class is
 * immutable.
 *
 * @author Philip Helger
 */
public final class DVRCoordinate implements IDVRCoordinate, Comparable<DVRCoordinate> {

    /** The separator char between ID elements */
    public static final char PART_SEPARATOR = ':';

    private static final Logger LOGGER = LoggerFactory.getLogger(DVRCoordinate.class);

    private final String groupID;

    private final String artifactID;

    private final DVRVersion version;

    private final @Nullable String classifier;

    // status vars
    private final int hashCode;

    /**
     * Constructor without classifier. All parameters must match the constraints from
     * {@link DVRValidityHelper#isValidCoordinateGroupID(String)},
     * {@link DVRValidityHelper#isValidCoordinateArtifactID(String)} and
     * {@link DVRValidityHelper#isValidCoordinateVersion(String)}.
     *
     * @param groupID the group ID. May neither be <code>null</code> nor empty.
     * @param artifactID the artifact ID. May neither be <code>null</code> nor empty.
     * @param version the version object. May not be <code>null</code>.
     */
    public DVRCoordinate(@NonNull @Nonempty final String groupID, @NonNull @Nonempty final String artifactID,
            @NonNull final DVRVersion version) {
        this(groupID, artifactID, version, null);
    }

    /**
     * Constructor. All parameters must match the constraints from
     * {@link DVRValidityHelper#isValidCoordinateGroupID(String)},
     * {@link DVRValidityHelper#isValidCoordinateArtifactID(String)},
     * {@link DVRValidityHelper#isValidCoordinateVersion(String)} and
     * {@link DVRValidityHelper#isValidCoordinateClassifier(String)}.
     *
     * @param groupID the group ID. May neither be <code>null</code> nor empty.
     * @param artifactID the artifact ID. May neither be <code>null</code> nor empty.
     * @param version the version object. May not be <code>null</code>.
     * @param classifier the classifier. May be <code>null</code>.
     */
    public DVRCoordinate(@NonNull @Nonempty final String groupID, @NonNull @Nonempty final String artifactID,
            @NonNull final DVRVersion version, final @Nullable String classifier) {
        if (StringHelper.isEmpty(groupID)) {
            throw new IllegalArgumentException("GroupID must not be empty");
        }
        if (!DVRValidityHelper.isValidCoordinateGroupID(groupID)) {
            throw new IllegalArgumentException("GroupID '" + groupID + "' is invalid");
        }
        if (StringHelper.isEmpty(artifactID)) {
            throw new IllegalArgumentException("ArtifactID must not be empty");
        }
        if (!DVRValidityHelper.isValidCoordinateArtifactID(artifactID)) {
            throw new IllegalArgumentException("ArtifactID '" + artifactID + "' is invalid");
        }
        ObjectHelper.requireNonNull(version, "Version");
        if (!DVRValidityHelper.isValidCoordinateVersion(version.getAsString())) {
            throw new IllegalArgumentException("Version '" + version + "' is invalid");
        }
        if (!DVRValidityHelper.isValidCoordinateClassifier(classifier)) {
            throw new IllegalArgumentException("Classifier '" + classifier + "' is invalid");
        }
        this.groupID = groupID;
        this.artifactID = artifactID;
        this.version = version;
        // Unify "" and null
        this.classifier = StringHelper.isNotEmpty(classifier) ? classifier : null;
        // Cache for improved performance
        this.hashCode = Objects.hash(this.groupID, this.artifactID, this.version, this.classifier);
    }

    public @NonNull @Nonempty String getGroupID() {
        return this.groupID;
    }

    public @NonNull @Nonempty String getArtifactID() {
        return this.artifactID;
    }

    public @NonNull DVRVersion getVersionObj() {
        return this.version;
    }

    public @Nullable String getClassifier() {
        return this.classifier;
    }

    public @NonNull DVRCoordinate getWithGroupID(final @Nullable String newGroupID) {
        if (Objects.equals(this.groupID, newGroupID)) {
            return this;
        }
        return new DVRCoordinate(newGroupID, this.artifactID, this.version, this.classifier);
    }

    public @NonNull DVRCoordinate getWithArtifactID(final @Nullable String newArtifactID) {
        if (Objects.equals(this.artifactID, newArtifactID)) {
            return this;
        }
        return new DVRCoordinate(this.groupID, newArtifactID, this.version, this.classifier);
    }

    public @NonNull DVRCoordinate getWithVersion(@NonNull final DVRVersion newVersion) {
        if (Objects.equals(this.version, newVersion)) {
            return this;
        }
        return new DVRCoordinate(this.groupID, this.artifactID, newVersion, this.classifier);
    }

    public @NonNull DVRCoordinate getWithVersion(@NonNull final IDVRPseudoVersion pseudoVersion) {
        return getWithVersion(DVRVersion.of(pseudoVersion));
    }

    public @NonNull DVRCoordinate getWithVersionLatest() {
        return getWithVersion(DVRPseudoVersionRegistry.LATEST);
    }

    public @NonNull DVRCoordinate getWithVersionLatestRelease() {
        return getWithVersion(DVRPseudoVersionRegistry.LATEST_RELEASE);
    }

    public @NonNull DVRCoordinate getWithClassifier(final @Nullable String newClassifier) {
        if (Objects.equals(this.classifier, newClassifier)) {
            return this;
        }
        return new DVRCoordinate(this.groupID, this.artifactID, this.version, newClassifier);
    }

    public static @NonNull @Nonempty String getAsSingleID(@NonNull @Nonempty final String groupID,
            @NonNull @Nonempty final String artifactID, @NonNull @Nonempty final String version, final @Nullable String classifier) {
        final StringBuilder ret = new StringBuilder().append(groupID).append(PART_SEPARATOR).append(artifactID).append(PART_SEPARATOR)
                .append(version);
        if (StringHelper.isNotEmpty(classifier)) {
            ret.append(PART_SEPARATOR).append(classifier);
        }
        return ret.toString();
    }

    public @NonNull @Nonempty String getAsSingleID() {
        return getAsSingleID(this.groupID, this.artifactID, getVersionString(), this.classifier);
    }

    public static int compare(@NonNull final DVRCoordinate left, @NonNull final DVRCoordinate right) {
        int ret = left.groupID.compareTo(right.groupID);
        if (ret == 0) {
            ret = left.artifactID.compareTo(right.artifactID);
            if (ret == 0) {
                ret = left.version.compareTo(right.version);
                if (ret == 0) {
                    // Null-safe compare
                    ret = ObjectHelper.compare(left.classifier, right.classifier);
                }
            }
        }
        return ret;
    }

    public int compareTo(@NonNull final DVRCoordinate other) {
        return compare(this, other);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final DVRCoordinate rhs = (DVRCoordinate) o;
        return this.groupID.equals(rhs.groupID) && this.artifactID.equals(rhs.artifactID) && this.version.equals(rhs.version)
                && Objects.equals(this.classifier, rhs.classifier);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return "DVRCoordinate[groupID=" + this.groupID + "; artifactID=" + this.artifactID + "; version=" + this.version
                + (StringHelper.isNotEmpty(this.classifier) ? "; classifier=" + this.classifier : "") + "]";
    }

    /**
     * Factory method without classifier. All parameters must match the constraints from
     * {@link DVRValidityHelper#isValidCoordinateGroupID(String)},
     * {@link DVRValidityHelper#isValidCoordinateArtifactID(String)} and
     * {@link DVRValidityHelper#isValidCoordinateVersion(String)}.
     *
     * @param groupID the group ID. May neither be <code>null</code> nor empty.
     * @param artifactID the artifact ID. May neither be <code>null</code> nor empty.
     * @param version the version String. May neither be <code>null</code> nor empty.
     * @return the created {@link DVRCoordinate} and never <code>null</code>.
     * @throws DVRVersionException if the provided version is invalid.
     */
    public static @NonNull DVRCoordinate create(@NonNull @Nonempty final String groupID, @NonNull @Nonempty final String artifactID,
            @NonNull @Nonempty final String version) throws DVRVersionException {
        return create(groupID, artifactID, version, null);
    }

    /**
     * Factory method for DVR coordinates. All parameters must match the constraints from
     * {@link DVRValidityHelper#isValidCoordinateGroupID(String)},
     * {@link DVRValidityHelper#isValidCoordinateArtifactID(String)},
     * {@link DVRValidityHelper#isValidCoordinateVersion(String)} and
     * {@link DVRValidityHelper#isValidCoordinateClassifier(String)}.
     *
     * @param groupID the group ID. May neither be <code>null</code> nor empty.
     * @param artifactID the artifact ID. May neither be <code>null</code> nor empty.
     * @param version the version String. May neither be <code>null</code> nor empty.
     * @param classifier the classifier. May be <code>null</code>.
     * @return the created {@link DVRCoordinate} and never <code>null</code>.
     * @throws DVRVersionException if the provided version is invalid.
     */
    public static @NonNull DVRCoordinate create(@NonNull @Nonempty final String groupID, @NonNull @Nonempty final String artifactID,
            @NonNull @Nonempty final String version, final @Nullable String classifier) throws DVRVersionException {
        return new DVRCoordinate(groupID, artifactID, DVRVersion.parseOrThrow(version), classifier);
    }

    /**
     * Try to parse the provided coordinates String. This is the reverse operation to {@link #getAsSingleID()}.
     *
     * @param coords the coordinate String to parse. May be <code>null</code>.
     * @return never <code>null</code>.
     * @throws DVRCoordinateException in case the layout is incorrect.
     * @throws DVRVersionException in case the version is incorrect.
     */
    public static @NonNull DVRCoordinate parseOrThrow(final @Nullable String coords) throws DVRCoordinateException, DVRVersionException {
        final List<String> parts = StringHelper.getExploded(PART_SEPARATOR, coords);
        final int size = parts.size();
        if (size >= 3 && size <= 4) {
            return create(parts.get(0), parts.get(1), parts.get(2), size >= 4 ? parts.get(3) : null);
        }

        throw new DVRCoordinateException("Invalid DVR Coordinates '" + coords + "' provided!");
    }

    public static @Nullable DVRCoordinate parseOrNull(final @Nullable String coords) {
        try {
            return parseOrThrow(coords);
        } catch (final DVRException | RuntimeException ex) {
            LOGGER.warn(ex.getMessage());
            return null;
        }
    }
}
