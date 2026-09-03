package org.conformatron.api.model.report;

import java.util.List;

import org.conformatron.api.model.action.CTAction;
import org.conformatron.api.model.action.CTActionReport;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTSeverity;
import org.jspecify.annotations.NonNull;

/**
 * Represents the validation report for a single action in the validation pipeline.
 * <p>
 * Maps to a single XVRL {@code <report>} element inside {@code <reports>}. Each report captures what happened during
 * one specific validation action, e.g. "Parse Document", "Validate Structure (XSD)", "Validate Rules (Schematron)",
 * etc.
 * </p>
 * <h3>Key Concepts</h3>
 * <ul>
 * <li>Every report has {@link CTReportMetadata metadata} identifying the action and its tools.</li>
 * <li>Every report has a {@link CTReportDigest digest} summarizing the outcome (valid/invalid + counts).</li>
 * <li>Every report has a {@link CTDetectionList} of individual findings (errors, warnings).</li>
 * <li>Reports can be nested: e.g. a "Schematron Validation" report may contain sub-reports for "Schematron Compilation"
 * and "Schematron Execution". This maps to the XVRL pattern {@code <reports>/<report>.../<report>}.</li>
 * </ul>
 * <h3>Compact vs. Detail</h3>
 * <p>
 * In a <b>detail report</b>, each action has its own report with full detection details. In a <b>compact report</b>, a
 * single report per validated document carries only the digest and a flat list of detections without per-action
 * breakdown. The same interface serves both — the difference is in how much data is populated.
 * </p>
 *
 * @see CTValidationRootReport
 * @see CTReportMetadata
 * @see CTReportDigest
 * @see CTActionReport
 * @author Andreas Schmitz TODO ggf. Split in Root Report und Sub-Report
 */
public interface CTValidationReport {
    // -- Identification --

    /**
     * @return An optional unique identifier for this report, used for cross-referencing. Maps to XVRL
     *         {@code <report @xml:id>}. For example, a Schematron compilation report may have id "schematron-report-1",
     *         and the execution report can reference it via {@code <schema>#schematron-report-1</schema>}. May be
     *         {@code null} if no cross-referencing is needed. UUID? Mandatory!<br>
     *         TODO Optionen: 1. UUID + Fortlaufende Nummerierung für Sub-Reports <- Vorläufige Auswahl 2. Jeder Report
     *         hat eine globale UUID 3. Fortlaufende Nummerierung (Keine globale Uniqueness) Fortlaufende Nummerierung
     */
    @NonNull
    String getID();

    // -- Action reference (superset of ICTActionReport) --

    /**
     * @return The action that produced this report. This provides the same information as
     *         {@link CTActionReport#getAction()}, ensuring that {@code ICTValidationReport} is a standalone superset of
     *         {@link CTActionReport} — no functionality is lost.
     */
    @NonNull
    CTAction getAction();

    // -- Metadata, digest, detections --

    /**
     * @return Metadata identifying this report's action, tools, and schema references. Never {@code null}.
     */
    @NonNull
    CTReportMetadata getMetadata();

    /**
     * @return A summary digest of the validation outcome for this action. Contains at minimum a valid/invalid flag and
     *         error/warning counts. Maps to XVRL {@code <digest>}.
     */
    @NonNull
    CTReportDigest getDigest();

    /**
     * @return The list of individual detections (findings) produced by this action. May be empty if the action produced
     *         no findings (e.g. all valid). Maps to the sequence of XVRL {@code <detection>} elements.
     */
    @NonNull
    CTDetectionList getDetections();

    /**
     * @return Nested sub-reports, if this report groups multiple related actions. For example, a Schematron validation
     *         report may contain sub-reports for compilation and execution. Returns an empty list if there are no
     *         sub-reports. Maps to nested {@code <report>} elements within a {@code <reports>} group.<br>
     *         TODO Änderung: Zu Hashmap
     */
    @NonNull
    List<CTValidationReport> getSubReports();

    /**
     * @return {@code true} if this report contains nested sub-reports.
     */
    default boolean hasSubReports() {
        return !getSubReports().isEmpty();
    }

    /**
     * @return The worst severity across this report's own detections <em>and</em> all nested sub-report detections.
     */
    @NonNull
    default CTSeverity getWorstSeverity() {
        CTSeverity worst = getDetections().getWorstSeverity();
        for (final CTValidationReport sub : getSubReports()) {
            worst = CTSeverity.getWorst(worst, sub.getWorstSeverity());
        }
        return worst;
    }
}
