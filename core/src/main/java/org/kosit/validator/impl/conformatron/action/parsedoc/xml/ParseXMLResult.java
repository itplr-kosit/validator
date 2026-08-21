package org.kosit.validator.impl.conformatron.action.parsedoc.xml;

import java.util.List;

import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.detection.ICTDetection;
import org.conformatron.api.model.detection.ICTDetectionList;
import org.jspecify.annotations.Nullable;
import org.kosit.validator.impl.conformatron.action.parsedoc.ParseDocumentActionResult;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.DomValidationSource;

/**
 * Single XML parsing result
 */
public class ParseXMLResult implements ParseDocumentActionResult<DomValidationSource> {

    private ECTStepResult result;

    private ICTDetectionList detections;

    private DomValidationSource parsedSource;

    public static ParseXMLResult failure(List<ICTDetection> detections) {
        return failure (new DetectionList(detections));
    }

    public static ParseXMLResult failure(ICTDetectionList detections) {
        return new ParseXMLResult(ECTStepResult.FAILURE, detections, null);
    }

    /**
     * @param result success or failure (failure cancels the process)
     * @param detections this execution's contribution to the report; never {@code null}
     * @param parsedSource the parsed source. On a well-formedness failure it still carries source metadata, bytes and
     *            SHA-512 hash for document identity in the partial CVRL — only without parsed content
     *            ({@code isParsed() == false}). {@code null} only when the source could not be read at all.
     */
    public ParseXMLResult(ECTStepResult result, ICTDetectionList detections, @Nullable DomValidationSource parsedSource) {
        this.result = result;
        this.detections = detections;
        this.parsedSource = parsedSource;
    }

    public ECTStepResult getResult() {
        return this.result;
    }

    public ICTDetectionList getDetectionList() {
        return this.detections;
    }

    public @Nullable DomValidationSource getParsedSource() {
        return parsedSource;
    }
}