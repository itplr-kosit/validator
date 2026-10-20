package org.kosit.validator.scenario.generic;

import java.util.Objects;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.kosit.base.string.StringHelper;

/**
 * An artifact repository of a configuration: the name its resources refer to, and the place it stands. This class is
 * immutable.
 * <p>
 * Resolving a resource is confined to the repository it names - a location that would reach outside it is rejected (see
 * the step {@code retrieve-artifacts}). The only way past that is an absolute location on the resource itself, see
 * {@link ScenarioResource#getAbsoluteLocation()}.
 * </p>
 * <p>
 * Version dependent: scenario configuration version 3 declares repositories and lets every resource name one. Version 2
 * writes none - it has exactly one repository per scenario, handed to the loader from outside the document, and every
 * resource inherits it. A configuration read from version 2 is therefore given a single repository under
 * {@link #DEFAULT_NAME}, by whoever loads it, not by the mapper.
 * </p>
 *
 * @see ScenarioConfiguration#getRepositories()
 */
public final class ScenarioRepository {

    /** The name a repository gets when the configuration it came from did not name one. */
    public static final String DEFAULT_NAME = "default";

    private final String name;

    private final String location;

    private ScenarioRepository(@NonNull @Nonempty final String name, @NonNull @Nonempty final String location) {
        if (StringHelper.isEmpty(name)) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        if (StringHelper.isEmpty(location)) {
            throw new IllegalArgumentException("Location must not be empty");
        }
        this.name = name;
        this.location = location;
    }

    /**
     * @return the name the resources of the configuration refer to. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getName() {
        return this.name;
    }

    /**
     * @return where the repository stands: a directory, or an archive reached with a {@code jar:} URI. A relative URI
     *         is resolved against the configuration file. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getLocation() {
        return this.location;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ScenarioRepository rhs = (ScenarioRepository) o;
        return this.name.equals(rhs.name) && this.location.equals(rhs.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.location);
    }

    @Override
    public String toString() {
        return "ScenarioRepository[" + this.name + " -> " + this.location + "]";
    }

    /**
     * @param name the name the resources refer to. May neither be <code>null</code> nor empty.
     * @param location where the repository stands. May neither be <code>null</code> nor empty.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioRepository of(@NonNull @Nonempty final String name, @NonNull @Nonempty final String location) {
        return new ScenarioRepository(name, location);
    }

    /**
     * The repository of a configuration that does not name one, as version 2 writes it.
     *
     * @param location where the repository stands. May neither be <code>null</code> nor empty.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioRepository ofDefault(@NonNull @Nonempty final String location) {
        return new ScenarioRepository(DEFAULT_NAME, location);
    }
}
