package org.kosit.cvr.report;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.source.CTParsedValidationSource;

/**
 * Result of an ad-hoc schematron validation run.
 *
 * @param status success or failure of the run itself (failure = parse, preparation or processing error)
 * @param parsedSource the parsed document; may be {@code null} if the source could not be read
 * @param detections all findings and errors of the run; never {@code null}
 */
public record AdHocValidationResult(CTStepResult status, CTParsedValidationSource parsedSource, CTDetectionList detections) {

    public boolean isSuccess() {
        return this.status == CTStepResult.SUCCESS;
    }

    /**
     * @return {@code true} if the run succeeded and the document satisfies the rules (no ERROR or FATAL detections)
     */
    public boolean isConformant() {
        return isSuccess() && !this.detections.containsAtLeastOneError();
    }
}