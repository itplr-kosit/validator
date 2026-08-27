package org.kosit.validator.impl.conformatron.action.parsedoc.xml;

import java.util.List;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.jspecify.annotations.Nullable;
import org.kosit.validator.impl.conformatron.action.parsedoc.ParseDocumentActionResult;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.source.DomValidationSource;

/**
 * Single XML parsing result
 */
public class ParseXmlResult implements ParseDocumentActionResult<DomValidationSource> {

    private CTStepResult result;

    private CTDetectionList detections;

    private DomValidationSource parsedSource;

    public static ParseXmlResult failure(List<CTDetection> detections) {
        return failure(new DetectionList(detections));
    }

    public static ParseXmlResult failure(CTDetectionList detections) {
        return new ParseXmlResult(CTStepResult.FAILURE, detections, null);
    }

    /**
     * @param result success or failure (failure cancels the process)
     * @param detections this execution's contribution to the report; never {@code null}
     * @param parsedSource the parsed source. On a well-formedness failure it still carries source metadata, bytes and
     *            SHA-512 hash for document identity in the partial CVRL — only without parsed content
     *            ({@code isParsed() == false}). {@code null} only when the source could not be read at all.
     */
    public ParseXmlResult(CTStepResult result, CTDetectionList detections, @Nullable DomValidationSource parsedSource) {
        this.result = result;
        this.detections = detections;
        this.parsedSource = parsedSource;
    }

    public CTStepResult getResult() {
        return this.result;
    }

    public CTDetectionList getDetectionList() {
        return this.detections;
    }

    public @Nullable DomValidationSource getParsedSource() {
        return parsedSource;
    }
}