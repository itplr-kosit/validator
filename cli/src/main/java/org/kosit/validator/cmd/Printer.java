package org.kosit.validator.cmd;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * Wrapper for {@link System Systems} printing capability.
 * 
 * @author Andreas Penski
 */
public class Printer {

    private static PrintWriter OUT = new PrintWriter(System.out, true);

    private static PrintWriter ERR = new PrintWriter(System.err, true);

    private Printer() {
        // hide
    }

    /**
     * Overrides output writers e.g. for tests
     */
    public static void configure(final PrintWriter out, final PrintWriter err) {
        OUT = out;
        ERR = err;
    }

    /**
     * System.out/System.err (for runtime)
     */
    public static void reset() {
        OUT = new PrintWriter(System.out, true);
        ERR = new PrintWriter(System.err, true);
    }

    /**
     * Writes content verbatim to standard output — no placeholder substitution. For anything that is content rather
     * than a message template: a report may legitimately contain braces, which {@link MessageFormat} would choke on.
     *
     * @param content the content to write
     */
    public static void writeRaw(final String content) {
        OUT.println(content);
    }

    /**
     * Writes content to the standard error channel as it is - for text that is not a message template, like a usage
     * message with its quotes and placeholders.
     *
     * @param content the content to write
     */
    public static void writeErrRaw(final String content) {
        ERR.println(content);
    }

    /**
     * Writes to standard output channel.
     * 
     * @param message the message with placeholders
     * @param params the params.
     */
    public static void writeOut(final String message, final Object... params) {
        try {
            OUT.println(new MessageFormat(message, Locale.ENGLISH).format(params));
        } catch (final RuntimeException ex) {
            ERR.println("[Format error!] <" + message + "> with params <" + params + ">");
        }
    }

    /**
     * Writes to standard error channel.
     *
     * @param message the message with placeholders
     * @param params the params.
     */
    public static void writeErr(final String message, final Object... params) {
        try {
            ERR.println(new MessageFormat(message, Locale.ENGLISH).format(params));
        } catch (final RuntimeException ex) {
            ERR.println("[Format error!] <" + message + "> with params <" + params + ">");
        }
    }

    /**
     * Writes to standard error channel and prints a stacktrace.
     * 
     * @param ex the exception
     * @param message the message with placeholders
     * @param params the params
     */
    public static void writeErr(final Exception ex, final String message, final Object... params) {
        writeErr(message, params);
        if (ex != null) {
            ex.printStackTrace();
        }
    }

}
