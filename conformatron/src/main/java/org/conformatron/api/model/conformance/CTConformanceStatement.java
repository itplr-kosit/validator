package org.conformatron.api.model.conformance;

import org.conformatron.api.model.scenario.CTConformanceTarget;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The conformance verdict for a single {@link CTConformanceTarget}, produced by step 8 ({@code COMPUTE_CONFORMANCE}).
 * <p>
 * Conformance is distinct from plain validity: a document may be invalid against EN 16931 yet conformant with XRechnung
 * 3.0 if the profile overrides the relevant rule via a custom conformance level or {@code acceptSelector}.
 * </p>
 */
public interface CTConformanceStatement {

    /**
     * @return The conformance target this statement evaluates.
     */
    @NonNull
    CTConformanceTarget getTarget();

    /**
     * @return The conformance result for this target.
     */
    @NonNull
    CTConformanceResult getResult();

    /**
     * @return Optional human-readable rationale for the result (e.g. which rule caused NON_CONFORMANT, or that an
     *         acceptSelector was applied). May be {@code null}.
     */
    @Nullable
    String getRationale();

    /**
     * @return {@code true} if a custom {@code acceptSelector} was applied to determine this statement (rather than the
     *         default detection-based evaluation).
     */
    boolean isAcceptSelectorApplied();
}
