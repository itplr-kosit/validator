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

package de.kosit.validationtool.daemon;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Simple base implemenation for http handlers. Doing I/O stuff.
 * 
 * @author Andreas Penski
 */
abstract class BaseHandler implements HttpHandler {

    protected static final String APPLICATION_XML = "application/xml";

    protected static void write(final HttpExchange exchange, final byte[] content, final String contentType) throws IOException {
        write(exchange, content, contentType, HttpStatus.SC_OK);
    }

    protected static void write(final HttpExchange exchange, final byte[] content, final String contentType, final int statusCode)
            throws IOException {
        write(exchange, contentType, os -> os.write(content), statusCode);
    }

    protected static void write(final HttpExchange exchange, final String contentType, final Write write, final int statusCode)
            throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, 0);
        final OutputStream os = exchange.getResponseBody();
        write.write(os);
        os.close();
    }

    protected static void error(final HttpExchange exchange, final int statusCode, final String message) throws IOException {
        final byte[] bytes = message.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "text/plain");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        final OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    @FunctionalInterface
    protected interface Write {

        void write(OutputStream out) throws IOException;
    }
}
