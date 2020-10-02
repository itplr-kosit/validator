/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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
