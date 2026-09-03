package org.conformatron.api.model.report;

import java.time.OffsetDateTime;
import java.util.List;

import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.annotation.Nonnegative;
import org.conformatron.api.model.detection.CTSeverity;
import org.jspecify.annotations.NonNull;

/**
 * Root container for all validation reports of a single validation run.
 * <p>
 * Maps to the XVRL {@code <reports>} root element. A single validation run validates one source document and produces
 * multiple {@link CTValidationReport} instances — one per validation action (e.g. parse, scenario detection, XSD
 * validation, Schematron validation, decision).
 * </p>
 * <h3>Design Rationale</h3>
 * <ul>
 * <li>This is the top-level entry point into the validation result domain model.</li>
 * <li>It corresponds 1:1 to a CSVR (Conformance Structured Validation Result) XML document.</li>
 * <li>Both detail reports and compact reports share this same root structure. The difference lies in how much detail
 * each contained {@link CTValidationReport} carries.</li>
 * </ul>
 *
 * @author Andreas Schmitz TODO Refactor name to ICTValidationRootReport?
 */
public interface CTValidationRootReport {

    /**
     * @return UUID
     */
    @NonNull
    String getID();

    // -- Metadata about the overall validation run --

    /**
     * @return The timestamp (UTC) when the validation run was initiated. Maps to XVRL
     *         {@code <reports>/<metadata>/<timestamp>}.
     */
    @NonNull
    OffsetDateTime getTimestampUTC();

    /**
     * @return A reference (URI or path) to the source document that was validated. Maps to XVRL
     *         {@code <reports>/<metadata>/<document @href>}.
     */
    @NonNull
    @Nonempty
    String getDocumentReference();

    /**
     * @return The name of the validator software that produced this result. Maps to XVRL
     *         {@code <reports>/<metadata>/<validator @name>}. Example: "KoSIT XML Validator"
     */
    @NonNull
    @Nonempty
    String getValidatorName();

    /**
     * @return The version of the validator software. Maps to XVRL {@code <reports>/<metadata>/<validator @version>}.
     *         Example: "2.0.0"
     */
    @NonNull
    @Nonempty
    String getValidatorVersion();

    /**
     * @return The primary language of the reports (BCP-47 language tag). Maps to XVRL {@code <reports @xml:lang>}.
     *         Example: "de", "en"
     */
    @NonNull
    @Nonempty
    String getLanguage();

    // -- Report mode --

    /**
     * @return {@code true} if this is a compact report (one summary report per validated document, without per-action
     *         breakdown), {@code false} if this is a detail report (one report per pipeline action with full detection
     *         details). Both modes use the same interfaces — the difference is in data granularity.
     */
    boolean isCompact();

    // -- Contained reports --

    /**
     * @return An ordered, non-empty list of validation reports — one per executed action. The order reflects the order
     *         of execution in the validation pipeline. Maps to the sequence of {@code <report>} elements inside
     *         {@code <reports>}.
     */
    @NonNull
    @Nonempty
    List<CTValidationReport> getReports();

    /**
     * @return The number of contained reports.
     */
    @Nonnegative
    default int getReportCount() {
        return getReports().size();
    }

    /**
     * @return The worst (highest) severity found across all contained reports. Useful for a quick "is everything ok?"
     *         check.
     */
    @NonNull
    CTSeverity getOverallWorstSeverity();

    /**
     * @return {@code true} if all contained reports have a valid digest (i.e. no action reported a validation failure).
     */
    boolean isOverallValid();
}
