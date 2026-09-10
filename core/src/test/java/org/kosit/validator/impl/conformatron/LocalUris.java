package org.kosit.validator.impl.conformatron;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Reduces every URL a generated report picked up from the machine it ran on to its local part, right before the report
 * is written to disk.
 * <p>
 * A report locates what it validated against — the scenario declaration, the retrieved artifacts, the parsed document —
 * and the test data resolves to wherever it happens to sit: inside the packaged test data archive after {@code mvn
 * verify}, in {@code target/classes} when the IDE hands out the plain output directory, and below the checkout for the
 * repository local {@code e2e/} material. All three carry an absolute path, so every generated artifact differs between
 * two machines and a diff of them says nothing but "generated somewhere else" — the same problem
 * {@link FixedTimestamps} solves for the serialization time.
 * </p>
 * <p>
 * What is kept is the part that identifies the artifact: the path inside the archive, below the build output directory,
 * or relative to the checkout. Like {@link FixedTimestamps} this is a text substitution on the way out and not a
 * behaviour of the writer: a real run still reports where it really read from, only what the generators commit is
 * local.
 * </p>
 */
public final class LocalUris {

    /** The archive part of a {@code jar:} URL, up to and including the separator of the entry within it. */
    private static final Pattern IN_ARCHIVE = Pattern.compile("jar:file:[^\"'<>\\s]*?!/");

    /** The part of a URL above a build output directory, up to and including that directory. */
    private static final Pattern IN_CLASSES = Pattern.compile("file:/[^\"'<>\\s]*?/target/(?:test-)?classes/");

    /** The checkout the generator runs in, so that everything below it becomes checkout relative. */
    private static final Pattern IN_CHECKOUT;

    static {
        // same anchor as the generators use to find their output folder: the module directory, or its parent when the
        // tests run in the core module
        final Path moduleDir = Paths.get("").toAbsolutePath();
        final Path root = moduleDir.endsWith("core") ? moduleDir.getParent() : moduleDir;
        // the raw path of a directory URI starts and ends with a slash — "file:", "file://" and "file:///" all occur
        IN_CHECKOUT = Pattern.compile("file:/{0,2}" + Pattern.quote(root.toUri().getRawPath()));
    }

    /**
     * @param report the serialized report
     * @return the same report with every absolute URL of this machine reduced to its local part
     */
    public static byte[] apply(final byte[] report) {
        String xml = new String(report, StandardCharsets.UTF_8);
        xml = IN_ARCHIVE.matcher(xml).replaceAll("");
        xml = IN_CLASSES.matcher(xml).replaceAll("");
        xml = IN_CHECKOUT.matcher(xml).replaceAll("");
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    private LocalUris() {
        // static utility
    }
}
