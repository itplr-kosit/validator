package org.conformatron.api.model.detection;

import java.time.OffsetDateTime;
import java.util.Locale;

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
    OffsetDateTime getDateTimeUtc();

    default boolean hasDateTimeUtc() {
        return getDateTimeUtc() != null;
    }

    /**
     * @return The severity associated with this detection. May not be <code>null</code>.
     */
    @NonNull
    CTSeverity getSeverity();

    /**
     * @return The severity declared by the rule, when the effective {@link #getSeverity()} is a scenario
     *         {@code customLevel} override; {@code null} when no override was applied.
     */
    @Nullable
    CTSeverity getOriginalSeverity();

    default boolean hasOriginalSeverity() {
        return getOriginalSeverity() != null;
    }

    /**
     * @return The unique identifier of the detection. May be <code>null</code>.
     */
    @Nullable
    String getId();

    default boolean hasId() {
        final var s = getId();
        return s != null && !s.isEmpty();
    }

    /**
     * @return The detection code classifying this finding. Maps to XVRL {@code <detection @code>}. Examples:
     *         "scenario-matched", "BR-DE-13", "compilation". This is distinct from {@link #getId()} which is a unique
     *         identifier, while {@code code} is a classification/category. May be <code>null</code>.
     */
    @Nullable
    String getCode();

    default boolean hasCode() {
        final var s = getCode();
        return s != null && !s.isEmpty();
    }

    /**
     * @return The field or path expression this detection refers to. May be <code>null</code>.
     */
    @Nullable
    String getField();

    default boolean hasField() {
        final var s = getField();
        return s != null && !s.isEmpty();
    }

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

    default boolean hasText() {
        return getText() != null;
    }

    /**
     * @return An optional summary text for this detection. Maps to XVRL {@code <detection>/<summary>}. Used e.g. in the
     *         decision-recommender report for the overall assessment text. Distinct from {@link #getText()} which maps
     *         to {@code <message>}. May be <code>null</code>.
     */
    @Nullable
    CTDetectionText getSummary();

    default boolean hasSummary() {
        return getSummary() != null;
    }

    /**
     * @return An optional Java exception that provides further technical details on the detection. May be
     *         <code>null</code>.
     */
    @Nullable
    Throwable getLinkedException();

    default boolean hasLinkedException() {
        return getLinkedException() != null;
    }

    @NonNull
    default String getAsString(final Locale locale) {
        final StringBuilder sb = new StringBuilder();
        if (hasDateTimeUtc())
            sb.append(getDateTimeUtc().toString()).append(' ');
        if (getSeverity() instanceof final CTStandardSeverity std) {
            if (hasOriginalSeverity()) {
                final var orig = getOriginalSeverity();
                if (orig instanceof final CTStandardSeverity origStg) {
                    sb.append('[').append(std.getLogText()).append(" <- ").append(origStg.getLogText()).append("] ");
                } else {
                    sb.append('[').append(std.getLogText()).append(" <- ").append(orig.getNumericLevel()).append("] ");
                }
            } else {
                sb.append('[').append(std.getLogText()).append("] ");
            }
        }
        if (hasId())
            sb.append('[').append(getId()).append("] ");
        if (hasField())
            sb.append("in [").append(getField()).append("] ");
        if (getLocation().hasAnyInformation())
            sb.append("@ ").append(getLocation().getAsString()).append(' ');
        if (hasText())
            sb.append(getText().getDisplayTextOrFallback(locale)).append(' ');
        if (hasLinkedException()) {
            final var ex = getLinkedException();
            sb.append('(').append(ex.getClass().getName()).append(':').append(ex.getMessage()).append(") ");
        }
        // Delete last char (blank)
        if (sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
