package org.conformatron.api.model.scenario;

import java.util.List;

import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.model.conformance.CTConformanceStatement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A conformance target declared by the selected scenario.
 * <p>
 * Step 8 ({@code COMPUTE_CONFORMANCE}) evaluates one {@link CTConformanceStatement} per target. Each target groups one
 * or more rule set references and optionally carries an XPath {@code acceptSelector} that can override the default
 * detection-based evaluation.
 * </p>
 */
public interface CTConformanceTarget {

    /**
     * @return Unique identifier for this conformance target (e.g. {@code "en-16931"}, {@code "xrechnung-3.0"}).
     */
    @NonNull
    @Nonempty
    String getTargetID();

    /**
     * @return Human-readable display name (e.g. {@code "EN 16931"}, {@code "XRechnung 3.0"}).
     */
    @NonNull
    @Nonempty
    String getTargetName();

    /**
     * @return References to the rule sets (artifact references / hrefs) that this target is evaluated against. Order is
     *         significant.
     */
    @NonNull
    List<String> getRuleSetReferences();

    /**
     * @return An optional XPath expression evaluated against the validation results to determine conformance. If
     *         non-null, this takes precedence over the default detection-based evaluation (reference impl:
     *         {@code acceptSelector}). May be {@code null}.
     */
    @Nullable
    String getAcceptSelector();

    /**
     * @return {@code true} if a custom accept selector is configured.
     */
    default boolean hasAcceptSelector() {
        return getAcceptSelector() != null;
    }
}
