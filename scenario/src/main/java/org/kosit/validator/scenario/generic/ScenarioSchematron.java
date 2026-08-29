package org.kosit.validator.scenario.generic;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.string.StringHelper;

/**
 * A single Schematron validation step of a {@link Scenario}.
 *
 * @author Philip Helger
 */
public class ScenarioSchematron {

    /** By default the PSVI of the preceding XML Schema validation is not passed to the Schematron */
    public static final boolean DEFAULT_PSVI = false;

    private ScenarioResource resource;

    private boolean psvi = DEFAULT_PSVI;

    private @Nullable String compiler;

    /**
     * Constructor.
     *
     * @param resource the Schematron resource to use. May not be <code>null</code>.
     */
    public ScenarioSchematron(@NonNull final ScenarioResource resource) {
        setResource(resource);
    }

    /**
     * @return the Schematron resource. Never <code>null</code>.
     */
    public @NonNull ScenarioResource getResource() {
        return this.resource;
    }

    /**
     * Set the Schematron resource.
     *
     * @param resource the resource to use. May not be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioSchematron setResource(@NonNull final ScenarioResource resource) {
        this.resource = ObjectHelper.requireNonNull(resource, "Resource");
        return this;
    }

    /**
     * @return <code>true</code> if the post schema validation infoset of the preceding XML Schema validation is to be
     *         passed to the Schematron, <code>false</code> if not.
     */
    public boolean isPsvi() {
        return this.psvi;
    }

    /**
     * Set whether the post schema validation infoset is to be passed to the Schematron.
     *
     * @param psvi <code>true</code> to pass it, <code>false</code> to not pass it.
     * @return this for chaining
     */
    public @NonNull ScenarioSchematron setPsvi(final boolean psvi) {
        this.psvi = psvi;
        return this;
    }

    /**
     * @return the name of the Schematron compiler to use. May be <code>null</code> to use the default one.
     */
    public @Nullable String getCompiler() {
        return this.compiler;
    }

    /**
     * @return <code>true</code> if a specific compiler is set, <code>false</code> if not.
     */
    public boolean hasCompiler() {
        return StringHelper.isNotEmpty(this.compiler);
    }

    /**
     * Set the name of the Schematron compiler to use.
     *
     * @param compiler the compiler name. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioSchematron setCompiler(final @Nullable String compiler) {
        this.compiler = StringHelper.emptyToNull(compiler);
        return this;
    }

    @Override
    public String toString() {
        return "ScenarioSchematron[resource=" + this.resource + "; psvi=" + this.psvi + "; compiler=" + this.compiler + "]";
    }
}
