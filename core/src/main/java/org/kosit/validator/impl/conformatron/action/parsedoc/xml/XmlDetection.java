package org.kosit.validator.impl.conformatron.action.parsedoc.xml;

import java.io.IOException;
import java.util.HexFormat;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.source.CTValidationSource;
import org.jspecify.annotations.NonNull;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
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
        return new Detection(CTStandardSeverity.NONE, CODE_DOCUMENT_PARSED, DetectionLocation.of(source.getName()),
                source.getReadResource().getHashAlgorithmName() + "=" + HexFormat.of().formatHex(source.getReadResource().getHashBytes()),
                null);
    }

    @NonNull
    public static Detection errorNotWellformed(final @NonNull String resourceId, final @NonNull Exception e) {
        return new Detection(CTStandardSeverity.ERROR, CODE_NOT_WELLFORMED, DetectionLocation.of(resourceId), e.getMessage(), e);
    }

    @NonNull
    public static Detection errorNotWellformed(final @NonNull String resourceId, final @NonNull SAXParseException e) {
        return new Detection(CTStandardSeverity.ERROR, CODE_NOT_WELLFORMED, DetectionLocation.of(resourceId, e), e.getMessage(), e);
    }

    @NonNull
    public static Detection ioError(final @NonNull String resourceId, final @NonNull IOException e) {
        return new Detection(CTStandardSeverity.ERROR, CODE_SOURCE_READ_ERROR, DetectionLocation.of(resourceId), e.getMessage(), e);
    }

    private XmlDetection() {
    }
}
