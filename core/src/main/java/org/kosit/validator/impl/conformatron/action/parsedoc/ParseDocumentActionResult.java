package org.kosit.validator.impl.conformatron.action.parsedoc;

import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.detection.ICTDetectionList;
import org.conformatron.api.model.source.ICTParsedValidationSource;
import org.jspecify.annotations.Nullable;

public interface ParseDocumentActionResult<T extends ICTParsedValidationSource> {

    ECTStepResult getResult();

    default boolean isSuccess() {
        return getResult().isSuccess();
    }

    default boolean isFailure() {
        return !getResult().isSuccess();
    }

    ICTDetectionList getDetectionList();

    @Nullable
    T getParsedSource();
}
