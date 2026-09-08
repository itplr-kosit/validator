package org.kosit.validator.impl.conformatron.model;

import java.net.URI;
import java.util.Objects;

import org.conformatron.api.model.validation.CTValidationArtifactReference;
import org.jspecify.annotations.Nullable;
import org.kosit.base.string.StringHelper;

/**
 * A rule set reference as the scenario declares it: the artifact plus the Schematron processor the scenario names for
 * it ({@code validateWithSchematron/@compiler}). For a {@code .sch} that is the processor step 6 compiles it with; for
 * a precompiled {@code .xsl} it documents the processor that produced the executable, so the report can state how the
 * rule set was built. A scenario that names none yields {@code null}.
 *
 * @param reference the artifact reference; must not be {@code null}
 * @param compilerId the declared Schematron processor, {@code null} when the scenario declares none
 * @author Andreas Schmitz
 */
public record ScenarioRuleSetReference(URI reference, @Nullable String compilerId) implements CTValidationArtifactReference {

    public ScenarioRuleSetReference {
        Objects.requireNonNull(reference);
    }

    @Override
    public URI getValidationArtifactReference() {
        return this.reference;
    }

    /**
     * @param reference the artifact reference as declared in the scenario configuration (e.g. {@code "simple.sch"})
     * @param compilerId the declared Schematron processor; blank counts as none
     * @return the carrier for this reference
     */
    public static ScenarioRuleSetReference of(final String reference, final @Nullable String compilerId) {
        if (StringHelper.isBlank(reference)) {
            throw new IllegalArgumentException("reference may not be null or blank");
        }
        return new ScenarioRuleSetReference(URI.create(reference), StringHelper.isBlank(compilerId) ? null : compilerId);
    }

    /**
     * @param reference any artifact reference of the pipeline
     * @return the Schematron processor the scenario declared for it, {@code null} for references without one
     */
    public static @Nullable String declaredCompiler(final CTValidationArtifactReference reference) {
        return reference instanceof final ScenarioRuleSetReference declared ? declared.compilerId() : null;
    }
}
