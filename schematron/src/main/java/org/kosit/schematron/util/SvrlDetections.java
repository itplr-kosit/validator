package org.kosit.schematron.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.kosit.base.string.StringHelper;
import org.kosit.conformatron.detection.Detection;
import org.kosit.conformatron.detection.DetectionList;
import org.kosit.conformatron.detection.DetectionLocation;
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
 * asserts), {@code <svrl:text>} becomes the text, and the SVRL {@code @location} XPath becomes the detection's location
 * ({@link DetectionLocation#ofXPath}) — a report consumer can then jump to the node instead of parsing the message
 * text.
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
                        .add(Detection.builder().severity(severityOf(failedAssert.getRole(), failedAssert.getFlag()))
                                .code(StringHelper.blankToDefault(failedAssert.getId(), CODE_FAILED_ASSERT))
                                .location(DetectionLocation.builder().resourceId(documentName).xpath(failedAssert.getLocation()).build())
                                .text(textOf(failedAssert.getText())).build());
                case final SuccessfulReport report -> detections.add(Detection.builder()
                        .severity(severityOf(report.getRole(), report.getFlag()))
                        .code(StringHelper.blankToDefault(report.getId(), CODE_SUCCESSFUL_REPORT))
                        .location(DetectionLocation.builder().resourceId(documentName).xpath(report.getLocation()).build()).text(textOf(report.getText())).build());
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
        final String level = StringHelper.isNotBlank(role) ? role : flag != null && !flag.isEmpty() ? flag.get(0) : null;
        if (StringHelper.isBlank(level)) {
            return CTStandardSeverity.ERROR;
        }

        return switch (level.toLowerCase(Locale.ROOT)) {
            case "information", "info" -> CTStandardSeverity.NONE;
            case "warning", "warn" -> CTStandardSeverity.WARNING;
            default -> CTStandardSeverity.ERROR;
        };
    }

    @NonNull
    private static String textOf(final Text text) {
        if (text == null) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();
        for (final var o : text.getContent())
            builder.append(String.valueOf(o).trim());
        return builder.toString();
    }

}
