package org.kosit.validator.scenario.generic;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.string.StringHelper;

/**
 * A single resource referenced by a {@link Scenario} - an XML Schema, a Schematron (or its precompiled XSLT) or a
 * report transformation.
 * <p>
 * Version dependent constraints:
 * <ul>
 * <li>Scenario configuration version 2 has no {@link #getCoordinate() coordinate} and requires the
 * {@link #getLocation() location}.</li>
 * <li>Scenario configuration version 3 requires the coordinate. The location is optional there - if it is absent, the
 * resource is retrieved from the global repository based on its coordinate.</li>
 * </ul>
 *
 * @author Philip Helger
 */
public class ScenarioResource {

    private @Nullable ScenarioCoordinate coordinate;

    private String name;

    private @Nullable String location;

    /**
     * Constructor.
     *
     * @param name the human readable name of the resource. May neither be <code>null</code> nor empty.
     */
    public ScenarioResource(@NonNull @Nonempty final String name) {
        setName(name);
    }

    /**
     * @return the DVR coordinate of this resource. May be <code>null</code>, e.g. if the resource was read from a
     *         version 2 configuration.
     */
    public @Nullable ScenarioCoordinate getCoordinate() {
        return this.coordinate;
    }

    /**
     * @return <code>true</code> if a coordinate is present, <code>false</code> if not.
     */
    public boolean hasCoordinate() {
        return this.coordinate != null;
    }

    /**
     * Set the DVR coordinate of this resource.
     *
     * @param coordinate the coordinate to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioResource setCoordinate(final @Nullable ScenarioCoordinate coordinate) {
        this.coordinate = coordinate;
        return this;
    }

    /**
     * @return the human readable name of this resource. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getName() {
        return this.name;
    }

    /**
     * Set the human readable name of this resource.
     *
     * @param name the name to use. May neither be <code>null</code> nor empty.
     * @return this for chaining
     */
    public @NonNull ScenarioResource setName(@NonNull @Nonempty final String name) {
        ObjectHelper.requireNonNull(name, "Name");
        if (StringHelper.isEmpty(name)) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        this.name = name;
        return this;
    }

    /**
     * @return the location of this resource, relative to the configuration repository. May be <code>null</code> in a
     *         version 3 configuration.
     */
    public @Nullable String getLocation() {
        return this.location;
    }

    /**
     * @return <code>true</code> if a location is present, <code>false</code> if not.
     */
    public boolean hasLocation() {
        return StringHelper.isNotEmpty(this.location);
    }

    /**
     * Set the location of this resource.
     *
     * @param location the location to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioResource setLocation(final @Nullable String location) {
        this.location = StringHelper.emptyToNull(location);
        return this;
    }

    @Override
    public String toString() {
        return "ScenarioResource[" + (this.coordinate != null ? "coordinate=" + this.coordinate.getAsSingleID() + "; " : "") + "name="
                + this.name + "; location=" + this.location + "]";
    }
}
