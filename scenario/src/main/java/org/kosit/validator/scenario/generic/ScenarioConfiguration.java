package org.kosit.validator.scenario.generic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.string.StringHelper;

/**
 * The version independent representation of a complete scenario configuration - the root object of the generic scenario
 * data model. It covers the union of the requirements of the scenario configuration versions 2 and 3, so that a single
 * instance can be serialized into both of them.
 * <p>
 * The properties that only exist in one of the two versions are:
 * <ul>
 * <li>{@link #getFrameworkVersion()} is required by version 2 and optional in version 3.</li>
 * <li>{@link #getValidFromDate()} only exists in version 3.</li>
 * <li>{@link Scenario#getCoordinate()} and {@link ScenarioResource#getCoordinate()} are required by version 3 and do
 * not exist in version 2.</li>
 * <li>{@link EScenarioKind#PDF} scenarios only exist in version 3.</li>
 * </ul>
 * The single {@code date} of version 2 corresponds to {@link #getLastModificationDate()}.
 *
 * @author Philip Helger
 */
public class ScenarioConfiguration {

    private String name;

    private @Nullable String author;

    private @Nullable LocalDate lastModificationDate;

    private @Nullable LocalDate validFromDate;

    private @Nullable String frameworkVersion;

    private @Nullable ScenarioDescription description;

    private final List<Scenario> scenarios = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param name the human readable name of the configuration. May neither be <code>null</code> nor empty.
     */
    public ScenarioConfiguration(@NonNull @Nonempty final String name) {
        setName(name);
    }

    /**
     * @return the human readable name of this configuration. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getName() {
        return this.name;
    }

    /**
     * Set the human readable name of this configuration.
     *
     * @param name the name to use. May neither be <code>null</code> nor empty.
     * @return this for chaining
     */
    public @NonNull ScenarioConfiguration setName(@NonNull @Nonempty final String name) {
        ObjectHelper.requireNonNull(name, "Name");
        if (StringHelper.isEmpty(name)) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        this.name = name;
        return this;
    }

    /**
     * @return the author of this configuration. May be <code>null</code>.
     */
    public @Nullable String getAuthor() {
        return this.author;
    }

    /**
     * Set the author of this configuration.
     *
     * @param author the author to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioConfiguration setAuthor(final @Nullable String author) {
        this.author = StringHelper.emptyToNull(author);
        return this;
    }

    /**
     * @return the date of the last modification of this configuration. This is the {@code date} of scenario
     *         configuration version 2. May be <code>null</code>, but both versions require it.
     */
    public @Nullable LocalDate getLastModificationDate() {
        return this.lastModificationDate;
    }

    /**
     * Set the date of the last modification of this configuration.
     *
     * @param lastModificationDate the date to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioConfiguration setLastModificationDate(final @Nullable LocalDate lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
        return this;
    }

    /**
     * @return the date from which on this configuration is to be applied. May be <code>null</code>. Only supported by
     *         scenario configuration version 3.
     */
    public @Nullable LocalDate getValidFromDate() {
        return this.validFromDate;
    }

    /**
     * Set the date from which on this configuration is to be applied.
     *
     * @param validFromDate the date to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioConfiguration setValidFromDate(final @Nullable LocalDate validFromDate) {
        this.validFromDate = validFromDate;
        return this;
    }

    /**
     * @return the version of the validator framework this configuration was written for. May be <code>null</code>, but
     *         scenario configuration version 2 requires it.
     */
    public @Nullable String getFrameworkVersion() {
        return this.frameworkVersion;
    }

    /**
     * @return <code>true</code> if a framework version is present, <code>false</code> if not.
     */
    public boolean hasFrameworkVersion() {
        return StringHelper.isNotEmpty(this.frameworkVersion);
    }

    /**
     * Set the version of the validator framework this configuration was written for.
     *
     * @param frameworkVersion the version to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioConfiguration setFrameworkVersion(final @Nullable String frameworkVersion) {
        this.frameworkVersion = StringHelper.emptyToNull(frameworkVersion);
        return this;
    }

    /**
     * @return the description of this configuration. May be <code>null</code>, but scenario configuration version 2
     *         requires it.
     */
    public @Nullable ScenarioDescription getDescription() {
        return this.description;
    }

    /**
     * Set the description of this configuration.
     *
     * @param description the description to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioConfiguration setDescription(final @Nullable ScenarioDescription description) {
        this.description = description;
        return this;
    }

    /**
     * @return the modifiable list of all contained scenarios, in document order. Never <code>null</code>.
     */
    public @NonNull List<Scenario> getScenarios() {
        return this.scenarios;
    }

    /**
     * Add a scenario.
     *
     * @param scenario the scenario to add. May not be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioConfiguration addScenario(@NonNull final Scenario scenario) {
        ObjectHelper.requireNonNull(scenario, "Scenario");
        this.scenarios.add(scenario);
        return this;
    }

    /**
     * @param kind the kind to filter for. May not be <code>null</code>.
     * @return a new list with all scenarios of the provided kind, in document order. Never <code>null</code>.
     */
    public @NonNull List<Scenario> getAllScenariosOfKind(@NonNull final EScenarioKind kind) {
        ObjectHelper.requireNonNull(kind, "Kind");
        return this.scenarios.stream().filter(x -> x.getKind() == kind).toList();
    }

    @Override
    public String toString() {
        return "ScenarioConfiguration[name=" + this.name + "; author=" + this.author + "; lastModificationDate=" + this.lastModificationDate
                + "; validFromDate=" + this.validFromDate + "; frameworkVersion=" + this.frameworkVersion + "; description="
                + this.description + "; scenarios=" + this.scenarios + "]";
    }
}
