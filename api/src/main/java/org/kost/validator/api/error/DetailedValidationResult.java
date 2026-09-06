package org.kost.validator.api.error;

import org.conformatron.api.model.detection.CTDetectionList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record DetailedValidationResult(@NonNull CTDetectionList parseDetections, @Nullable CTDetectionList schemaDetections,
        @Nullable CTDetectionList schematronDetections) {

}
