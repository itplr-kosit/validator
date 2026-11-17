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
 * <li>Scenario configuration version 2 carries the {@link #getCoordinate() coordinate} in its optional {@code id}
 * attribute, as one string, and requires the {@link #getLocation() location}.</li>
 * <li>Scenario configuration version 3 requires the coordinate. The location is optional there - if it is absent, the
 * repository is asked for the coordinate instead - and the resource names the {@link #getRepository() repository} it is
 * resolved in.</li>
 * <li>An {@link #getAbsoluteLocation() absolute location} stands outside every repository and is the one way past the
 * confinement resolution otherwise enforces. Only version 3 can write it, and never together with a repository or a
 * location.</li>
 * </ul>
 *
 * @author Philip Helger
 */
public class ScenarioResource {

    private @Nullable ScenarioCoordinate coordinate;

    private String name;

    private @Nullable String location;

    private @Nullable String absoluteLocation;

    private @Nullable String repository;

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

    /**
     * @return the absolute location of this resource, outside every repository. May be <code>null</code>, and is always
     *         <code>null</code> when a location or a repository is set.
     */
    public @Nullable String getAbsoluteLocation() {
        return this.absoluteLocation;
    }

    /**
     * @return <code>true</code> if an absolute location is present, <code>false</code> if not.
     */
    public boolean hasAbsoluteLocation() {
        return StringHelper.isNotEmpty(this.absoluteLocation);
    }

    /**
     * Set the absolute location of this resource, which takes it out of every repository.
     *
     * @param absoluteLocation the absolute location to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioResource setAbsoluteLocation(final @Nullable String absoluteLocation) {
        this.absoluteLocation = StringHelper.emptyToNull(absoluteLocation);
        return this;
    }

    /**
     * @return the name of the repository this resource is resolved in, as the configuration declares it. May be
     *         <code>null</code>: then the configuration has exactly one repository, or the resource stands outside
     *         every repository.
     * @see ScenarioConfiguration#getRepositories()
     */
    public @Nullable String getRepository() {
        return this.repository;
    }

    /**
     * @return <code>true</code> if a repository is named, <code>false</code> if not.
     */
    public boolean hasRepository() {
        return StringHelper.isNotEmpty(this.repository);
    }

    /**
     * Set the repository this resource is resolved in.
     *
     * @param repository the name of a repository the configuration declares. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioResource setRepository(final @Nullable String repository) {
        this.repository = StringHelper.emptyToNull(repository);
        return this;
    }

    @Override
    public String toString() {
        return "ScenarioResource[" + (this.coordinate != null ? "coordinate=" + this.coordinate.getAsSingleID() + "; " : "") + "name="
                + this.name + "; location=" + this.location
                + (this.absoluteLocation != null ? "; absoluteLocation=" + this.absoluteLocation : "")
                + (this.repository != null ? "; repository=" + this.repository : "") + "]";
    }
}
