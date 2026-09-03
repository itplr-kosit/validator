package org.conformatron.api.model.conformance;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The result of evaluating a single conformance target in step 8 ({@code COMPUTE_CONFORMANCE}).
 * <p>
 * {@link #INCONCLUSIVE} maps to the "please inspect" severity proposed in issue 04a and requires the
 * {@code DECISION_RECOMMENDATION} step to produce {@link CTDecision#EVALUATE_FURTHER}.
 * </p>
 */
public enum CTConformanceResult {

    /** All rule sets for this target passed; no error-band detections. */
    CONFORMANT("conformant"),

    /**
     * At least one rule set for this target produced an error-band detection, or the {@code ICTApplyRulesResult} was
     * empty (cancelled pipeline).
     */
    NON_CONFORMANT("non-conformant"),

    /**
     * One or more detections require human inspection and cannot be resolved automatically. Requires a manual review
     * before a final decision can be made. Blocked on issue 04a (severity model gap).
     */
    INCONCLUSIVE("inconclusive");

    private final String id;

    CTConformanceResult(final @NonNull @Nonempty String id) {
        this.id = id;
    }

    @NonNull
    @Nonempty
    public String getID() {
        return id;
    }

    public boolean isConformant() {
        return this == CONFORMANT;
    }

    public static @Nullable CTConformanceResult getFromIDOrNull(final @Nullable String id) {
        if (id != null)
            for (final var x : values())
                if (id.equals(x.id))
                    return x;
        return null;
    }
}
