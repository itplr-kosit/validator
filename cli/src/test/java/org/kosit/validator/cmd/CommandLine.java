package org.kosit.validator.cmd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;

/**
 * Helper for testing command line output.
 *
 * @author Andreas Penski
 */
public class CommandLine {

    private static final ReplaceableOutputStream<ByteArrayOutputStream> out = new ReplaceableOutputStream<>();

    private static final ReplaceableOutputStream<ByteArrayOutputStream> error = new ReplaceableOutputStream<>();

    static {
        // initialization must happen before SLF4J's SimpleLogger, otherwise logs are not captured.
        // therefore this class must not have a log
        System.setOut(new PrintStream(new TeeOutputStream(System.out, out)));
        System.setErr(new PrintStream(new TeeOutputStream(System.err, error)));
        setStandardInput(nullInputStream());
    }

    public static void setStandardInput(final InputStream in) {
        System.setIn(in);
    }

    public static InputStream nullInputStream() {
        return new InputStream() {

            @Override
            public int read() throws IOException {
                return 0;
            }
        };
    }

    public static String getOutput() {
        return out.getOut().toString();
    }

    public static String getErrorOutput() {
        return error.getOut().toString();
    }

    public static List<String> getOutputLines() {
        return readLines(out.getOut().toByteArray());
    }

    public static List<String> getErrorLines() {
        return readLines(error.getOut().toByteArray());
    }

    private static List<String> readLines(final byte[] bytes) {
        try ( ByteArrayInputStream in = new ByteArrayInputStream(bytes);
              Reader r = new InputStreamReader(in) ) {
            return IOUtils.readLines(r);
        } catch (final IOException e) {
            throw new IllegalStateException("Can not read input");
        }
    }

    public static void activate() {
        out.setOut(new ByteArrayOutputStream());
        error.setOut(new ByteArrayOutputStream());
        // Re-bind Printer's cached writers to the current (tee'd) System.out/System.err so output from
        // Printer.writeOut/writeErr is captured. Without this, Printer may still hold a PrintWriter wrapping the
        // original System.out from a previous test class (no longer isolated by Quarkus' classloader).
        Printer.reset();
    }

    public static void deactivate() {
        out.setOut(null);
        error.setOut(null);
        setStandardInput(nullInputStream());
    }

    public static void clear() {
        deactivate();
        activate();
    }

    /**
     * Simple proxy for {@link OutputStream} whose target can be swapped.
     *
     * @param <O> type of the underlying {@link OutputStream}
     */
    private static class ReplaceableOutputStream<O extends OutputStream> extends OutputStream {

        private O out;

        @Override
        public void write(final int idx) throws IOException {
            if (this.out != null) {
                this.out.write(idx);
            }
        }

        @Override
        public void write(final byte[] bts) throws IOException {
            if (this.out != null) {
                this.out.write(bts);
            }
        }

        @Override
        public void write(final byte[] bts, final int st, final int end) throws IOException {
            if (this.out != null) {
                this.out.write(bts, st, end);
            }
        }

        @Override
        public void flush() throws IOException {
            if (this.out != null) {
                this.out.flush();
            }
        }

        @Override
        public void close() throws IOException {
            if (this.out != null) {
                this.out.close();
            }
        }

        public O getOut() {
            return this.out;
        }

        public void setOut(final O out) {
            this.out = out;
        }
    }
}
