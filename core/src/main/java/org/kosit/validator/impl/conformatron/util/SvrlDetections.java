package org.kosit.validator.impl.conformatron.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.kosit.base.string.StringHelper;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.oclc.purl.dsdl.svrl.SuccessfulReport;
import org.oclc.purl.dsdl.svrl.Text;

/**
 * Maps an SVRL {@link SchematronOutput} to an {@link CTDetectionList} (conformatron-api step 7, {@code APPLY_RULES}).
 * Shared by {@link ApplyRulesAction} and the ad-hoc engine.
 * <p>
 * Field mapping per step-07 spec: the assertion {@code @id} becomes the detection <b>code</b> (fallback:
 * {@link #CODE_FAILED_ASSERT} / {@link #CODE_SUCCESSFUL_REPORT}), {@code @role} maps to the severity (default ERROR for
 * asserts), {@code <svrl:text>} becomes the text, and the SVRL {@code @location} XPath is appended to the message until
 * {@code ICTDetectionLocation} carries XPath locations natively.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class SvrlDetections {

    /** Fallback detection code for a violated {@code sch:assert} without an id. */
    public static final String CODE_FAILED_ASSERT = "failed-assert";

    /** Fallback detection code for a triggered {@code sch:report} without an id. */
    public static final String CODE_SUCCESSFUL_REPORT = "successful-report";

    private SvrlDetections() {
        // static utility
    }

    /**
     * Maps all failed asserts and successful reports of the given SVRL output to detections.
     *
     * @param svrl the SVRL output of one rule set application
     * @param documentName the validated document, used as detection location resource
     * @return the detections, in SVRL order; empty if the document satisfied all rules
     */
    public static CTDetectionList toDetections(final SchematronOutputType svrl, final String documentName) {
        final List<CTDetection> detections = new ArrayList<>();
        for (final Object entry : svrl.getActivePatternOrActiveGroupAndFiredRule()) {
            switch (entry) {
                case final FailedAssert failedAssert -> detections
                        .add(Detection.of(severityOf(failedAssert.getRole(), failedAssert.getFlag()),
                                StringHelper.blankToDefault(failedAssert.getId(), CODE_FAILED_ASSERT), DetectionLocation.of(documentName),
                                message(failedAssert.getLocation(), textOf(failedAssert.getText()))));
                case final SuccessfulReport report -> detections.add(Detection.of(severityOf(report.getRole(), report.getFlag()),
                        StringHelper.blankToDefault(report.getId(), CODE_SUCCESSFUL_REPORT), DetectionLocation.of(documentName),
                        message(report.getLocation(), textOf(report.getText()))));
                default -> {
                    // Ignore
                }
            }
        }
        return new DetectionList(detections);
    }

    /**
     * Per step-07 spec both SVRL attributes map to the severity: {@code @role} takes precedence, {@code @flag} is the
     * fallback (SchXslt-compiled XRechnung rules carry the level in {@code @flag}). No attribute at all defaults to
     * ERROR — an unclassified failed assert must not disappear.
     */
    private static CTStandardSeverity severityOf(final String role, final List<String> flag) {
        final String level = role != null && !role.isBlank() ? role : flag != null && !flag.isEmpty() ? flag.get(0) : null;
        if (level == null || level.isBlank()) {
            return CTStandardSeverity.ERROR;
        }
        return switch (level.toLowerCase(Locale.ROOT)) {
            case "information", "info" -> CTStandardSeverity.NONE;
            case "warning", "warn" -> CTStandardSeverity.WARNING;
            default -> CTStandardSeverity.ERROR;
        };
    }

    private static String textOf(final Text text) {
        if (text == null) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        text.getContent().forEach(c -> builder.append(String.valueOf(c).trim()));
        return builder.toString();
    }

    private static String message(final String location, final String text) {
        return location == null ? text : text + " (at " + location + ")";
    }
}
