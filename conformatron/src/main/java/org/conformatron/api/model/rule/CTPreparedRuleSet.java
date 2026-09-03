package org.conformatron.api.model.rule;

import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.model.validation.CTCompiledValidationArtifact;
import org.conformatron.api.model.validation.CTValidationArtifactReference;
import org.conformatron.api.model.validation.CTValidationType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A compiled / prepared rule set, ready for execution by step 7 ({@code APPLY_RULES}).
 * <p>
 * Produced by step 6 ({@code PREPARE_RULES}) and consumed by steps 7 and 8. The {@link #getCompiledArtifactHandle()} is
 * intentionally opaque ({@code Object}) so implementations can store a Saxon {@code XsltExecutable}, a JAXP
 * {@code Schema}, or any other engine artifact.
 * </p>
 *
 * <p>
 * <b>Open discussion:</b> which fields can realistically be populated per engine type (XSD, Schematron/SchXslt,
 * EDIFACT)? Each field is individually annotated.
 * </p>
 */
public interface CTPreparedRuleSet {

    /**
     * @return The validation engine/type this rule set was compiled for. Always available.
     */
    @NonNull
    CTValidationType getEngineType();

    /**
     * @return The version of the engine used for compilation (e.g. {@code "SchXslt2-2.0"}). May be {@code null} if
     *         unknown or not applicable.
     */
    @Nullable
    String getEngineVersion();

    /**
     * @return The name of the output format produced by applying this rule set (e.g. {@code "SVRL"}). May be
     *         {@code null} for non-reporting validators (e.g. XSD).
     */
    @Nullable
    String getOutputFormatName();

    /**
     * @return The version of the output format (e.g. SVRL spec version). May be {@code null}. To be confirmed per
     *         engine — see ADR: SVRL strategy.
     */
    @Nullable
    String getOutputFormatVersion();

    /**
     * @return The Schematron phase used during compilation (e.g. {@code "#ALL"}). {@code null} for non-Schematron rule
     *         sets.
     */
    @Nullable
    String getPhase();

    /**
     * @return The original artifact reference (URI / path) this rule set was compiled from. Maps to XVRL
     *         {@code <schema @href>}. Always available.
     */
    @NonNull
    @Nonempty
    CTValidationArtifactReference getArtifactReference();

    /**
     * @return An opaque handle to the compiled, engine-ready artifact. For Saxon/Schematron:
     *         {@code net.sf.saxon.s9api.XsltExecutable}. For JAXP/XSD: {@code javax.xml.validation.Schema}. Callers
     *         must cast based on {@link #getEngineType()}. Never {@code null} after successful preparation.
     */
    @NonNull
    CTCompiledValidationArtifact<?> getCompiledArtifact();
}
