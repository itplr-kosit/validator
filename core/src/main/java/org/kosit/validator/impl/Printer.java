/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kosit.validator.impl;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * Wrapper for {@link System Systems} printing capability.
 * 
 * @author Andreas Penski
 */
@SuppressWarnings("squid:S106")
public class Printer {

    private static PrintWriter OUT = new PrintWriter(System.out, true);

    private static PrintWriter ERR = new PrintWriter(System.err, true);

    private Printer() {
        // hide
    }

    /**
     * Overrides output writers e.g. for tests
     */
    public static void configure(PrintWriter out, PrintWriter err) {
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
    @SuppressWarnings("squid:S1148")
    public static void writeErr(final Exception ex, final String message, final Object... params) {
        writeErr(message, params);
        if (ex != null) {
            ex.printStackTrace();
        }
    }

}
