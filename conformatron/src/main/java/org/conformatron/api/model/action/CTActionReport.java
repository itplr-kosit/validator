package org.conformatron.api.model.action;

import org.conformatron.api.model.detection.CTDetectionList;
import org.jspecify.annotations.NonNull;

/**
 * Report of a single action.
 * 
 * @author Philip Helger
 *
 */
public interface CTActionReport {

    /**
     * 
     * @return The action it refers to.
     */
    @NonNull
    CTAction getAction();

    /**
     * @return The detections that were found.
     */
    @NonNull
    CTDetectionList getDetections();
}
