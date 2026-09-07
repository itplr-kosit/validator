package org.kost.validator.api.error;

import java.util.ArrayList;
import java.util.List;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record DetailedValidationResult(@NonNull CTDetectionList parseDetections, @Nullable CTDetectionList schemaDetections,
        @Nullable CTDetectionList schematronDetections) {

    public boolean containsNoError() {
        return (parseDetections == null || parseDetections.containsNoError())
                && (schemaDetections == null || schemaDetections.containsNoError())
                && (schematronDetections == null || schematronDetections.containsNoError());
    }

    @NonNull
    public List<CTDetection> getMergedDetections() {
        final var ret = new ArrayList<CTDetection>();
        if (parseDetections != null)
            parseDetections.forEach(ret::add);
        if (schemaDetections != null)
            schemaDetections.forEach(ret::add);
        if (schematronDetections != null)
            schematronDetections.forEach(ret::add);
        return ret;
    }
}
