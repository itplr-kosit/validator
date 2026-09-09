package org.kosit.validator.impl.conformatron.model;

import java.net.URI;
import java.util.Objects;

import org.conformatron.api.model.validation.CTCompiledValidationArtifact;
import org.conformatron.api.model.validation.CTValidationArtifactReference;
import org.jspecify.annotations.Nullable;
import org.kosit.base.string.StringHelper;

/**
 * A rule set reference as the scenario declares it: the artifact, plus what the scenario knows about it beyond the
 * location.
 * <ul>
 * <li>The Schematron processor the scenario names for it ({@code validateWithSchematron/@compiler}). For a {@code .sch}
 * that is the processor step 6 compiles it with; for a precompiled {@code .xsl} it documents the processor that
 * produced the executable, so the report can state how the rule set was built. A scenario that names none yields
 * {@code null}.</li>
 * <li>The compilation, if the scenario was assembled in code and the caller handed the artifact over compiled. Step 5
 * passes such an artifact through instead of resolving it.</li>
 * </ul>
 *
 * @param reference the artifact reference; must not be {@code null}
 * @param compilerId the declared Schematron processor, {@code null} when the scenario declares none
 * @param compiled the artifact handed over compiled, {@code null} when it is to be resolved from the repository
 * @author Andreas Schmitz
 */
public record ScenarioRuleSetReference(URI reference, @Nullable String compilerId,
        @Nullable CTCompiledValidationArtifact<?> compiled) implements CTValidationArtifactReference {

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
        return of(reference, compilerId, null);
    }

    /**
     * @param reference the artifact reference as declared in the scenario configuration
     * @param compilerId the declared Schematron processor; blank counts as none
     * @param compiled the artifact handed over compiled, or {@code null}
     * @return the carrier for this reference
     */
    public static ScenarioRuleSetReference of(final String reference, final @Nullable String compilerId,
            final @Nullable CTCompiledValidationArtifact<?> compiled) {
        if (StringHelper.isBlank(reference)) {
            throw new IllegalArgumentException("reference may not be null or blank");
        }
        return new ScenarioRuleSetReference(URI.create(reference), StringHelper.isBlank(compilerId) ? null : compilerId, compiled);
    }

    /**
     * @param reference any artifact reference of the pipeline
     * @return the Schematron processor the scenario declared for it, {@code null} for references without one
     */
    public static @Nullable String declaredCompiler(final CTValidationArtifactReference reference) {
        return reference instanceof final ScenarioRuleSetReference declared ? declared.compilerId() : null;
    }

    /**
     * @param reference any artifact reference of the pipeline
     * @return the compilation the scenario handed over for it, {@code null} for references to be resolved
     */
    public static @Nullable CTCompiledValidationArtifact<?> handedOverCompiled(final CTValidationArtifactReference reference) {
        return reference instanceof final ScenarioRuleSetReference declared ? declared.compiled() : null;
    }
}
