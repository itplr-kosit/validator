package org.conformatron.api.model.action;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;

/**
 * Represents a single execution of a single action with a start and end date and time as well as a list of detections.
 *
 * @author Philip Helger
 */
public interface CTActionExecution {

    /**
     * @return The action that was executed.
     */
    @NonNull
    CTAction getAction();

    /**
     * @return The date and time when action was started. Must be using UTC time zone.
     */
    @NonNull
    OffsetDateTime getStartDateTimeUTC();

    /**
     * @return The date and time when action ended. Must be using UTC time zone.
     */
    @NonNull
    OffsetDateTime getEndDateTimeUTC();

    /**
     * @return A non-<code>null</code> but may empty list of relevant parameters that were passed to this action
     *         execution. Each parameter consists of a name and a value. Each parameter name MUST be unique. Parameter
     *         values MUST NOT be <code>null</code> but maybe an empty string. No other constraints are imposed on the
     *         general interface level.
     */
    @NonNull
    Map<String, Object> getInputParameterDescriptions();

    /**
     * @return A non-<code>null</code> but may empty list of relevant outputs that were created by this action
     *         execution. Each output element consists of a name and a value. In case an action creates more than one
     *         output, the order of results must be maintained. Each output element name MUST be unique. Each output
     *         element values MUST NOT be <code>null</code> but maybe an empty string. No other constraints are imposed
     *         on the general interface level.
     */
    @NonNull
    LinkedHashMap<String, String> getOutputDescriptions();

    /**
     * @return The generic report date for this particular action execution.
     */
    @NonNull
    CTActionReport createActionReport();
}
