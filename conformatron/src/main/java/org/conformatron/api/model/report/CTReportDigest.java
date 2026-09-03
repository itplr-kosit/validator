package org.conformatron.api.model.report;

import java.util.Set;

import org.conformatron.api.annotation.Nonnegative;
import org.jspecify.annotations.NonNull;

/**
 * A summary digest of the validation outcome for a single action/report.
 * <p>
 * Maps to the XVRL {@code <digest>} element. The digest provides a quick overview without having to inspect individual
 * detections.
 * </p>
 * <h3>XVRL Mapping</h3>
 *
 * <pre>
 * &lt;digest valid="false" error-count="1" error-codes="BR-DE-13" warning-count="1" /&gt;
 * </pre>
 *
 * <h3>Design Rationale</h3>
 * <p>
 * The digest is the key element for <b>compact reports</b>: in a compact report, only the digest (plus a flat detection
 * list) is present — no per-action breakdown. For <b>detail reports</b>, the digest gives a quick summary alongside the
 * full detections.
 * </p>
 *
 * @author Andreas Schmitz
 */
public interface CTReportDigest {

    /**
     * @return {@code true} if this action's validation passed (no errors), {@code false} if it failed. Maps to XVRL
     *         {@code <digest @valid>}.
     */
    boolean isValid();

    /**
     * @return The number of detections with severity WARNING. Maps to XVRL {@code <digest @warning-count>}.
     */
    @Nonnegative
    int getWarningCount();

    /**
     * @return The number of detections with severity ERROR. Maps to XVRL {@code <digest @error-count>}.
     */
    @Nonnegative
    int getErrorCount();

    /**
     * @return A space-separated string of error codes found during this action. Maps to XVRL
     *         {@code <digest @error-codes>}. Example: "BR-DE-13 BR-DE-17" May be {@code null} if no error codes are
     *         available.
     */
    @NonNull
    Set<String> getErrorCodes();
}
