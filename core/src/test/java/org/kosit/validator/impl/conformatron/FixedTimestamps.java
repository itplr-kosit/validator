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
     * Matches an absolute {@code file:} or {@code jar:file:} URI in an {@code href}, so that a generated artifact does
     * not carry the checkout path of the machine that generated it.
     */
    private static final Pattern ABSOLUTE_HREF = Pattern.compile("href=\"(?:jar:)?file:[^\"]*/([^/\"!]+)\"");

    /**
     * @param report the serialized report
     * @return the same report with every timestamp set to {@link #GENERATED_AT}
     */
    public static byte[] apply(final byte[] report) {
        final String xml = new String(report, StandardCharsets.UTF_8);
        return TIMESTAMP.matcher(xml).replaceAll("<timestamp>" + GENERATED_AT + "</timestamp>").getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Shortens every absolute {@code href} of a generated report to the file name it ends in — the configuration file
     * of the applied scenario, and the document and artifact URIs of a run over packaged test data.
     * <p>
     * Those URIs hold the checkout path of the machine that generated the report and the layout of its build
     * ({@code test-data/target/…jar} for a reactor build, {@code .m2/…jar} for a single module), so a committed report
     * that keeps them differs per machine and per invocation without anything having changed. What a consumer needs is
     * the file, and the file name says it.
     * </p>
     * <p>
     * Applied by the generators of the report identity examples. The older generators under {@code e2e/} still commit
     * absolute hrefs; normalizing those rewrites every artifact they own and is a change of its own.
     * </p>
     *
     * @param report the serialized report
     * @return the same report with every absolute href reduced to a file name
     */
    public static byte[] withoutAbsoluteHrefs(final byte[] report) {
        final String xml = new String(report, StandardCharsets.UTF_8);
        return ABSOLUTE_HREF.matcher(xml).replaceAll("href=\"$1\"").getBytes(StandardCharsets.UTF_8);
    }

    private FixedTimestamps() {
        // static utility
    }
}
