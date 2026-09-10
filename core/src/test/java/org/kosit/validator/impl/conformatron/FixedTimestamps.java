package org.kosit.validator.impl.conformatron;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Rewrites the timestamps of a generated report to a fixed value, right before it is written to disk.
 * <p>
 * A report timestamp is the serialization time (D10), so it differs between two runs over the same input — and it is
 * the only thing that does. Without this, every run rewrites all 160 generated artifacts under {@code e2e/} and a diff
 * of them says nothing but "generated again"; with it, what the diff shows is a real change.
 * </p>
 * <p>
 * This is deliberately a text substitution on the way out, and not a clock inside the writer: nothing about the
 * validator changes, only what the generators commit.
 * </p>
 */
public final class FixedTimestamps {

    /** The value every generated report carries instead of its serialization time. */
    public static final String GENERATED_AT = "2026-01-01T00:00:00Z";

    /**
     * Matches a {@code <timestamp>} holding an actual {@code xs:dateTime} — so a {@code <timestamp>} element inside an
     * embedded source document or scenario is left alone.
     */
    private static final Pattern TIMESTAMP = Pattern
            .compile("<timestamp>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:Z|[+-]\\d{2}:\\d{2})</timestamp>");

    /**
     * @param report the serialized report
     * @return the same report with every timestamp set to {@link #GENERATED_AT}
     */
    public static byte[] apply(final byte[] report) {
        final String xml = new String(report, StandardCharsets.UTF_8);
        return TIMESTAMP.matcher(xml).replaceAll("<timestamp>" + GENERATED_AT + "</timestamp>").getBytes(StandardCharsets.UTF_8);
    }

    private FixedTimestamps() {
        // static utility
    }
}
