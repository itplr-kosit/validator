package org.kosit.validator.impl.conformatron;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Drops the line numbers from the Java stack traces a generated report carries, right before it is written to disk.
 * <p>
 * A cancelled run records the exception that cancelled it, diagnostic information included, and a stack trace names the
 * line every frame stands on. Those numbers move whenever anything above them in the same file moves - an added import
 * is enough - so a committed report differs after a change that has nothing to do with it. Four of the unhappy examples
 * churn that way, and a diff of them then says "someone edited a test", which is exactly what a reference example must
 * not say.
 * </p>
 * <p>
 * What is kept is the frame: class, method and file. That is what a reader of the example needs; the line number is
 * only meaningful together with the revision it was taken from, and the example carries no revision. The same text
 * substitution on the way out as {@link FixedTimestamps} and {@link LocalUris}: a real run still reports the real line,
 * only what the generators commit is stable.
 * </p>
 */
public final class StableStackTraces {

    /** The line number in a stack trace frame, e.g. {@code (RetrieveArtifactsAction.java:201)}. */
    private static final Pattern FRAME_LINE_NUMBER = Pattern.compile("(\\.java):\\d+\\)");

    /**
     * @param report the serialized report
     * @return the same report with every stack trace frame reduced to class, method and file
     */
    public static byte[] apply(final byte[] report) {
        final String xml = new String(report, StandardCharsets.UTF_8);
        return FRAME_LINE_NUMBER.matcher(xml).replaceAll("$1)").getBytes(StandardCharsets.UTF_8);
    }

    private StableStackTraces() {
        // static utility
    }
}
