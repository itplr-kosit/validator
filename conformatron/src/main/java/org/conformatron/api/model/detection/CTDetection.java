package org.conformatron.api.model.detection;

import java.time.OffsetDateTime;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Define a single detection
 *
 * @author Philip Helger
 */
public interface CTDetection {

    /**
     * @return The date and time in UTC/GMT when the detection was created. May be <code>null</code>.
     */
    @Nullable
    OffsetDateTime getDateTimeUTC();

    /**
     * @return The severity associated with this detection. May not be <code>null</code>.
     */
    @NonNull
    CTSeverity getSeverity();

    /**
     * @return The unique identifier of the detection. May be <code>null</code>.
     */
    @Nullable
    String getID();

    /**
     * @return The detection code classifying this finding. Maps to XVRL {@code <detection @code>}. Examples:
     *         "scenario-matched", "BR-DE-13", "compilation". This is distinct from {@link #getID()} which is a unique
     *         identifier, while {@code code} is a classification/category. May be <code>null</code>.
     */
    @Nullable
    String getCode();

    /**
     * @return The field or path expression this detection refers to. May be <code>null</code>.
     */
    @Nullable
    String getField();

    /**
     * @return The location where the detection occurred. May not be <code>null</code>.
     */
    @NonNull
    CTDetectionLocation getLocation();

    /**
     * @return The textual description of the detection. May be <code>null</code> - e.g. if an ID is present instead.
     */
    @Nullable
    CTDetectionText getText();

    /**
     * @return An optional summary text for this detection. Maps to XVRL {@code <detection>/<summary>}. Used e.g. in the
     *         decision-recommender report for the overall assessment text. Distinct from {@link #getText()} which maps
     *         to {@code <message>}. May be <code>null</code>.
     */
    @Nullable
    CTDetectionText getSummary();

    /**
     * @return An optional Java exception that provides further technical details on the detection. May be
     *         <code>null</code>.
     */
    @Nullable
    Exception getLinkedException();
}
