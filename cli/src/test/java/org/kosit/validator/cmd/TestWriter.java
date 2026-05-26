package org.kosit.validator.cmd;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.output.WriterOutputStream;

/**
 * Helper that captures picocli, {@link Printer} and slf4j-simple log output into in-memory writers for assertions.
 *
 * <p>
 * slf4j-simple writes to {@link System#err} by default and looks the stream up dynamically on each log call
 * ({@code org.slf4j.simpleLogger.cacheOutputStream=false} is its default). {@link #installAsSystemStreams()} therefore
 * suffices to capture all log output into {@link #getErrorOutput()}.
 * </p>
 */
public class TestWriter {

    private final StringWriter outWriter = new StringWriter();

    private final StringWriter errWriter = new StringWriter();

    private PrintStream originalErr;

    public StringWriter getOutWriter() {
        return outWriter;
    }

    public StringWriter getErrWriter() {
        return errWriter;
    }

    public String getOutput() {
        return outWriter.toString();
    }

    public String getErrorOutput() {
        return errWriter.toString();
    }

    public List<String> getOutputLines() {
        return Arrays.stream(getOutput().split("\\R")).toList();
    }

    public List<String> getErrorOutputLines() {
        return Arrays.stream(getErrorOutput().split("\\R")).toList();
    }

    /**
     * Redirects {@link System#err} into this writer's err buffer so that slf4j-simple log output (which defaults to
     * {@code System.err}) and {@code e.printStackTrace()} calls are captured into {@link #getErrorOutput()}. Must be
     * paired with {@link #restoreSystemStreams()}.
     * <p>
     * {@link System#out} is intentionally not redirected: picocli's own {@code setOut(PrintWriter)} writer is what
     * tests rely on for command output, and intercepting {@link System#out} via a {@link WriterOutputStream} has been
     * observed to interfere with picocli's help rendering.
     * </p>
     */
    public void installAsSystemStreams() {
        this.originalErr = System.err;
        System.setErr(new PrintStream(asOutputStream(errWriter), true, StandardCharsets.UTF_8));
    }

    private static OutputStream asOutputStream(final Writer w) {
        try {
            return WriterOutputStream.builder().setWriter(w).setCharset(StandardCharsets.UTF_8).get();
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to create WriterOutputStream", e);
        }
    }

    public void restoreSystemStreams() {
        if (originalErr != null) {
            System.setErr(originalErr);
            originalErr = null;
        }
    }
}
