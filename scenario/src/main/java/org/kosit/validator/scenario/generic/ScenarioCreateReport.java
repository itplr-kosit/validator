package org.kosit.validator.scenario.generic;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.string.StringHelper;

/**
 * A single report to be created for a {@link Scenario}.
 *
 * @author Philip Helger
 */
public class ScenarioCreateReport {

    private @Nullable String id;

    private ScenarioResource resource;

    private final List<ScenarioCustomErrorLevel> customLevels = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param resource the report transformation resource to use. May not be <code>null</code>.
     */
    public ScenarioCreateReport(@NonNull final ScenarioResource resource) {
        setResource(resource);
    }

    /**
     * @return the ID of this report. May be <code>null</code>, but both scenario configuration versions require it.
     */
    public @Nullable String getID() {
        return this.id;
    }

    /**
     * @return <code>true</code> if an ID is present, <code>false</code> if not.
     */
    public boolean hasID() {
        return StringHelper.isNotEmpty(this.id);
    }

    /**
     * Set the ID of this report.
     *
     * @param id the ID to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioCreateReport setID(final @Nullable String id) {
        this.id = StringHelper.emptyToNull(id);
        return this;
    }

    /**
     * @return the report transformation resource. Never <code>null</code>.
     */
    public @NonNull ScenarioResource getResource() {
        return this.resource;
    }

    /**
     * Set the report transformation resource.
     *
     * @param resource the resource to use. May not be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioCreateReport setResource(@NonNull final ScenarioResource resource) {
        this.resource = ObjectHelper.requireNonNull(resource, "Resource");
        return this;
    }

    /**
     * @return the modifiable list of all custom error levels of this report. Never <code>null</code>.
     */
    public @NonNull List<ScenarioCustomErrorLevel> getCustomLevels() {
        return this.customLevels;
    }

    /**
     * Add a custom error level.
     *
     * @param customLevel the custom error level to add. May not be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioCreateReport addCustomLevel(@NonNull final ScenarioCustomErrorLevel customLevel) {
        ObjectHelper.requireNonNull(customLevel, "CustomLevel");
        this.customLevels.add(customLevel);
        return this;
    }

    @Override
    public String toString() {
        return "ScenarioCreateReport[id=" + this.id + "; resource=" + this.resource + "; customLevels=" + this.customLevels + "]";
    }
}
