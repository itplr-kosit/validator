package org.kost.validator.api.xml;

import java.util.List;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.kosit.conformatron.detection.Detection;
import org.kosit.conformatron.detection.DetectionLocation;

import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.s9api.XmlProcessingError;

/**
 * Collects every well-formedness error as a FATAL detection (one detection per parser error, with line/column) instead
 * of aborting on the first one.
 *
 * @param resourceId Resource ID of the resource being read
 * @param errors The error list to be filled.
 */
public record CollectingSaxonErrorReporter(String resourceId, List<CTDetection> errors) implements ErrorReporter {

    public static @NonNull CTDetection toDetection(final @NonNull XmlProcessingError error) {
        final var builder = Detection.builder().severity(error.isWarning() ? CTStandardSeverity.WARNING : CTStandardSeverity.ERROR);
        if (error.getErrorCode() != null)
            builder.code(error.getErrorCode().toString());
        builder.text(error.getMessage());
        if (error.getLocation() != null)
            builder.location(DetectionLocation.builder().resourceId(error.getLocation().getSystemId())
                    .lineNumber(error.getLocation().getLineNumber()).columnNumber(error.getLocation().getColumnNumber()));
        builder.field(error.getPath()).linkedException(error.getCause());
        return builder.build();
    }

    public void report(final @NonNull XmlProcessingError error) {
        errors.add(toDetection(error));
    }
}