package org.kosit.validator.scenario.generic;

import java.util.List;
import java.util.Objects;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.coord.DVRCoordinate;
import org.kosit.base.coord.DVRCoordinateException;
import org.kosit.base.coord.DVRException;
import org.kosit.base.string.StringHelper;

/**
 * The coordinate of a scenario or of a single resource, as introduced by scenario configuration version 3. This class
 * is immutable.
 * <p>
 * It deliberately keeps the group ID, artifact ID, version and classifier exactly as they were read from - or are meant
 * to be written to - the XML, and additionally offers the parsed {@link DVRCoordinate}. Keeping both is necessary,
 * because {@link DVRCoordinate} is stricter and more normalizing than the XML representation:
 * <ul>
 * <li>It rejects versions with superfluous leading zeroes, so the Factur-X version <code>1.09.2</code> has no
 * {@link DVRCoordinate} representation at all.</li>
 * <li>It normalizes the version, so <code>1.0.0</code> would be written back as <code>1</code>.</li>
 * </ul>
 * Serialization therefore always uses the raw parts, whereas comparison and repository lookups should use
 * {@link #getCoordinate()}.
 *
 * @author Philip Helger
 * @see DVRCoordinate
 */
public final class ScenarioCoordinate {

    private final String groupID;

    private final String artifactID;

    private final String version;

    private final @Nullable String classifier;

    // status vars
    private final @Nullable DVRCoordinate coordinate;

    private final @Nullable String coordinateError;

    private ScenarioCoordinate(@NonNull @Nonempty final String groupID, @NonNull @Nonempty final String artifactID,
            @NonNull @Nonempty final String version, final @Nullable String classifier) {
        if (StringHelper.isEmpty(groupID)) {
            throw new IllegalArgumentException("GroupID must not be empty");
        }
        if (StringHelper.isEmpty(artifactID)) {
            throw new IllegalArgumentException("ArtifactID must not be empty");
        }
        if (StringHelper.isEmpty(version)) {
            throw new IllegalArgumentException("Version must not be empty");
        }
        this.groupID = groupID;
        this.artifactID = artifactID;
        this.version = version;
        // Unify "" and null
        this.classifier = StringHelper.isNotEmpty(classifier) ? classifier : null;

        // Try to parse eagerly, but never fail on it
        DVRCoordinate parsed = null;
        String error = null;
        try {
            parsed = DVRCoordinate.create(groupID, artifactID, version, this.classifier);
        } catch (final DVRException | RuntimeException ex) {
            error = ex.getMessage();
        }
        this.coordinate = parsed;
        this.coordinateError = error;
    }

    /**
     * @return the group ID exactly as provided. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getGroupID() {
        return this.groupID;
    }

    /**
     * @return the artifact ID exactly as provided. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getArtifactID() {
        return this.artifactID;
    }

    /**
     * @return the version exactly as provided. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getVersion() {
        return this.version;
    }

    /**
     * @return <code>true</code> if a classifier is present, <code>false</code> if not.
     */
    public boolean hasClassifier() {
        return this.classifier != null;
    }

    /**
     * @return the optional classifier exactly as provided. May be <code>null</code> but never empty.
     */
    public @Nullable String getClassifier() {
        return this.classifier;
    }

    /**
     * @return <code>true</code> if the provided parts form a valid {@link DVRCoordinate}, <code>false</code> if not.
     * @see #getCoordinate()
     * @see #getCoordinateError()
     */
    public boolean hasCoordinate() {
        return this.coordinate != null;
    }

    /**
     * @return the parsed DVR coordinate. <code>null</code> if the provided parts do not form a valid
     *         {@link DVRCoordinate} - see {@link #getCoordinateError()} for the reason in that case.
     */
    public @Nullable DVRCoordinate getCoordinate() {
        return this.coordinate;
    }

    /**
     * @return the parsed DVR coordinate. Never <code>null</code>.
     * @throws DVRCoordinateException if the provided parts do not form a valid {@link DVRCoordinate}.
     */
    public @NonNull DVRCoordinate getCoordinateOrThrow() throws DVRCoordinateException {
        if (this.coordinate == null) {
            throw new DVRCoordinateException("'" + getAsSingleID() + "' is no valid DVR Coordinate: " + this.coordinateError);
        }
        return this.coordinate;
    }

    /**
     * @return the reason why the provided parts do not form a valid {@link DVRCoordinate}. <code>null</code> if
     *         {@link #hasCoordinate()} is <code>true</code>.
     */
    public @Nullable String getCoordinateError() {
        return this.coordinateError;
    }

    /**
     * @return the joint String representation of the raw parts, separated by a colon (:) character. Contrary to
     *         {@link DVRCoordinate#getAsSingleID()} the version is not normalized. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getAsSingleID() {
        return DVRCoordinate.getAsSingleID(this.groupID, this.artifactID, this.version, this.classifier);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ScenarioCoordinate rhs = (ScenarioCoordinate) o;
        return this.groupID.equals(rhs.groupID) && this.artifactID.equals(rhs.artifactID) && this.version.equals(rhs.version)
                && Objects.equals(this.classifier, rhs.classifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.groupID, this.artifactID, this.version, this.classifier);
    }

    @Override
    public String toString() {
        return "ScenarioCoordinate[" + getAsSingleID() + "]";
    }

    /**
     * Factory method without classifier.
     *
     * @param groupID the group ID. May neither be <code>null</code> nor empty.
     * @param artifactID the artifact ID. May neither be <code>null</code> nor empty.
     * @param version the version. May neither be <code>null</code> nor empty.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioCoordinate of(@NonNull @Nonempty final String groupID, @NonNull @Nonempty final String artifactID,
            @NonNull @Nonempty final String version) {
        return of(groupID, artifactID, version, null);
    }

    /**
     * Factory method.
     *
     * @param groupID the group ID. May neither be <code>null</code> nor empty.
     * @param artifactID the artifact ID. May neither be <code>null</code> nor empty.
     * @param version the version. May neither be <code>null</code> nor empty.
     * @param classifier the classifier. May be <code>null</code>.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioCoordinate of(@NonNull @Nonempty final String groupID, @NonNull @Nonempty final String artifactID,
            @NonNull @Nonempty final String version, final @Nullable String classifier) {
        return new ScenarioCoordinate(groupID, artifactID, version, classifier);
    }

    /**
     * Factory method taking over the parts of an existing DVR coordinate. Note that the version of the created object
     * is the normalized one of {@link DVRCoordinate#getVersionString()}.
     *
     * @param coordinate the coordinate to take the parts from. May not be <code>null</code>.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioCoordinate of(@NonNull final DVRCoordinate coordinate) {
        ObjectHelper.requireNonNull(coordinate, "Coordinate");
        return new ScenarioCoordinate(coordinate.getGroupID(), coordinate.getArtifactID(), coordinate.getVersionString(),
                coordinate.getClassifier());
    }

    /**
     * Try to parse the provided coordinate String. This is the reverse operation to {@link #getAsSingleID()}.
     *
     * @param singleID the coordinate String to parse. May be <code>null</code>.
     * @return never <code>null</code>.
     * @throws DVRCoordinateException in case the layout is incorrect.
     */
    public static @NonNull ScenarioCoordinate parseOrThrow(final @Nullable String singleID) throws DVRCoordinateException {
        final List<String> parts = StringHelper.getExploded(DVRCoordinate.PART_SEPARATOR, singleID);
        final int size = parts.size();
        if (size < 3 || size > 4) {
            throw new DVRCoordinateException("Invalid DVR Coordinates '" + singleID + "' provided!");
        }
        return of(parts.get(0), parts.get(1), parts.get(2), size >= 4 ? parts.get(3) : null);
    }

    /**
     * Try to parse the provided coordinate String. This is the reverse operation to {@link #getAsSingleID()}.
     *
     * @param singleID the coordinate String to parse. May be <code>null</code>.
     * @return <code>null</code> if the layout is incorrect.
     */
    public static @Nullable ScenarioCoordinate parseOrNull(final @Nullable String singleID) {
        try {
            return parseOrThrow(singleID);
        } catch (final DVRCoordinateException | RuntimeException ex) {
            return null;
        }
    }
}
