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

package de.kosit.validationtool.impl;

import java.text.MessageFormat;
import java.util.Locale;

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
        System.out.println(new MessageFormat(message, Locale.ENGLISH).format(params));
    }

    /**
     * Writes to standard error channel.
     *
     * @param message the message with placeholders
     * @param params the params.
     */
    public static void writeErr(final String message, final Object... params) {
        System.err.println(new MessageFormat(message, Locale.ENGLISH).format(params));
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
