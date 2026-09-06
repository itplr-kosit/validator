package org.kosit.cvr.action.parsedoc.xml;

import java.io.IOException;
import java.util.HexFormat;

import org.conformatron.api.model.source.CTValidationSource;
import org.jspecify.annotations.NonNull;
import org.kosit.conformatron.detection.Detection;
import org.kosit.conformatron.detection.DetectionLocation;
import org.xml.sax.SAXParseException;

/**
 * Helper to emit XML {@link Detection} instances.
 * 
 * @author Philip Helger
 *
 */
public final class XmlDetection {

    /** Detection code on success. */
    public static final String CODE_DOCUMENT_PARSED = "document-parsed";

    /** Detection code for well-formedness errors. */
    public static final String CODE_NOT_WELLFORMED = "not-wellformed";

    /** Detection code for IO failures while reading the source. */
    public static final String CODE_SOURCE_READ_ERROR = "source-read-error";

    @NonNull
    public static Detection success(final @NonNull CTValidationSource source) {
        // XXX is this really how we want it?
        return Detection.builderNone().code(CODE_DOCUMENT_PARSED).location(DetectionLocation.of(source.getName())).text(
                source.getReadResource().getHashAlgorithmName() + "=" + HexFormat.of().formatHex(source.getReadResource().getHashBytes()))
                .build();
    }

    @NonNull
    public static Detection errorNotWellformed(final @NonNull String resourceId, final @NonNull Exception e) {
        return Detection.builderError().code(CODE_NOT_WELLFORMED).location(DetectionLocation.of(resourceId)).text(e.getMessage())
                .linkedException(e).build();
    }

    @NonNull
    public static Detection errorNotWellformed(final @NonNull String resourceId, final @NonNull SAXParseException e) {
        return Detection.builderError().code(CODE_NOT_WELLFORMED).location(DetectionLocation.of(resourceId, e)).text(e.getMessage())
                .linkedException(e).build();
    }

    @NonNull
    public static Detection ioError(final @NonNull String resourceId, final @NonNull IOException e) {
        return Detection.builderError().code(CODE_SOURCE_READ_ERROR).location(DetectionLocation.of(resourceId)).text(e.getMessage())
                .linkedException(e).build();
    }

    private XmlDetection() {
    }
}
