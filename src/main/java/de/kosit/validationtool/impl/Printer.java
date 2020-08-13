package de.kosit.validationtool.impl;

import java.text.MessageFormat;

/**
 * Wrapper for {@link System Systems} printing capability.
 * 
 * @author Andreas Penski
 */
@SuppressWarnings("squid:S106")
public class Printer {

    private Printer() {
        // hide
    }

    /**
     * Writes to standard output channel.
     * 
     * @param message the message with placeholders
     * @param params the params.
     */
    public static void writeOut(final String message, final Object... params) {
        System.out.println(MessageFormat.format(message, params));
    }

    /**
     * Writes to standard error channel.
     *
     * @param message the message with placeholders
     * @param params the params.
     */
    public static void writeErr(final String message, final Object... params) {
        System.err.println(MessageFormat.format(message, params));
    }
}
