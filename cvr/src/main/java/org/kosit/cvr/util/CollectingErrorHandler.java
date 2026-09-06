package org.kosit.cvr.util;

import java.util.List;

import org.conformatron.api.model.detection.CTDetection;
import org.jspecify.annotations.NonNull;
import org.kosit.conformatron.detection.Detection;
import org.kosit.conformatron.detection.DetectionLocation;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Collects every well-formedness error as a FATAL detection (one detection per parser error, with line/column) instead
 * of aborting on the first one.
 *
 * @param resourceId Resource ID of the resource being read
 * @param errors The error list to be filled.
 */
public record CollectingErrorHandler(String resourceId, List<CTDetection> errors) implements ErrorHandler {

    /** Detection code for well-formedness errors. */
    public static final String CODE_NOT_WELLFORMED = "not-wellformed";

    @NonNull
    public static Detection errorNotWellformed(final @NonNull String resourceId, final @NonNull SAXParseException e) {
        return Detection.builderError().code(CODE_NOT_WELLFORMED).location(DetectionLocation.builder().resourceId(resourceId).location(e))
                .text(e.getMessage()).linkedException(e).build();
    }

    @Override
    public void warning(final SAXParseException e) {
        // well-formedness only: parser warnings do not affect the outcome of this step
    }

    @Override
    public void error(final SAXParseException e) {
        this.errors.add(errorNotWellformed(this.resourceId, e));
    }

    @Override
    public void fatalError(final SAXParseException e) throws SAXException {
        this.errors.add(errorNotWellformed(this.resourceId, e));
        throw e;
    }
}