package org.kosit.validator.impl.conformatron.action.parsedoc;

import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.jspecify.annotations.Nullable;

public interface ParseDocumentActionResult<T extends CTParsedValidationSource> {

    ECTStepResult getResult();

    default boolean isSuccess() {
        return getResult().isSuccess();
    }

    default boolean isFailure() {
        return !getResult().isSuccess();
    }

    CTDetectionList getDetectionList();

    @Nullable
    T getParsedSource();
}
