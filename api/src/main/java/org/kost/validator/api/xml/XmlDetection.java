package org.kost.validator.api.xml;

import java.io.IOException;
import java.util.HexFormat;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.source.CTValidationSource;
import org.jspecify.annotations.NonNull;
import org.kosit.base.string.StringHelper;
import org.kosit.conformatron.detection.Detection;
import org.kosit.conformatron.detection.DetectionLocation;
import org.xml.sax.SAXParseException;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * Helper to emit XML {@link Detection} instances.
 *
 * @author Philip Helger
 *
 */
public final class XmlDetection {

    /** Detection code for well-formedness errors. */
    public static final String CODE_NOT_WELLFORMED = "not-wellformed";

    /** Detection code on success. */
    public static final String CODE_DOCUMENT_PARSED = "document-parsed";

    /** Detection code for IO failures while reading the source. */
    public static final String CODE_SOURCE_READ_ERROR = "source-read-error";

    @NonNull
    public static Detection success(final @NonNull CTValidationSource source) {
        return Detection.builderNone().code(CODE_DOCUMENT_PARSED).location(source.getName()).text(
                source.getReadResource().getHashAlgorithmName() + "=" + HexFormat.of().formatHex(source.getReadResource().getHashBytes()))
                .build();
    }

    @NonNull
    public static Detection errorNotWellformed(final @NonNull String resourceId, final @NonNull Exception e) {
        return Detection.builderError().code(CODE_NOT_WELLFORMED).location(resourceId).text(e.getMessage()).linkedException(e).build();
    }

    @NonNull
    public static Detection errorNotWellformed(final @NonNull String resourceId, final @NonNull SAXParseException e) {
        return Detection.builderError().code(CODE_NOT_WELLFORMED).location(DetectionLocation.builder().resourceId(resourceId).location(e))
                .text(e.getMessage()).linkedException(e).build();
    }

    @NonNull
    public static Detection ioError(final @NonNull String resourceId, final @NonNull IOException e) {
        return Detection.builderError().code(CODE_SOURCE_READ_ERROR).location(resourceId).text(e.getMessage()).linkedException(e).build();
    }

    public static CTDetection saxonApiError(@NonNull final String resourceId, final SaxonApiException e) {
        final String code = e.getErrorCode() != null ? e.getErrorCode().toString() : null;
        return Detection
                .builderError().code(code).location(DetectionLocation.builder()
                        .resourceId(StringHelper.emptyToDefault(e.getSystemId(), resourceId)).lineNumber(e.getLineNumber()))
                .text(e.getMessage()).linkedException(e).build();
    }

    private XmlDetection() {
    }
}
