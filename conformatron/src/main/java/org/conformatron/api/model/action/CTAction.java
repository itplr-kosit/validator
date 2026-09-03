package org.conformatron.api.model.action;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;

/**
 * Contains a single action or activity for which conformatron detections can be collected. Each action can be executed
 * multiple times, and each execution is represented as an {@link CTActionExecution}.
 *
 * @author Philip Helger
 */
public interface CTAction {

    /**
     * @return Name of the action for human identification
     */
    @NonNull
    @Nonempty
    String getName();

    /**
     * @return The general action type
     */
    @NonNull
    CTActionType getType();
}
