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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

import com.sun.net.httpserver.HttpExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.impl.input.SourceInput;
import de.kosit.validationtool.impl.input.StreamHelper;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;

/**
 * Wir benötigen einen Handler, der zur Verarbeitung von HTTP-Anforderungen aufgerufen wird um hier die Verarbeitung des
 * POST Request zu realisieren.
 */
@Slf4j
@RequiredArgsConstructor
class CheckHandler extends BaseHandler {

    private static final AtomicLong counter = new AtomicLong(0);

    private final Check implemenation;

    private final Processor processor;

    /**
     * Methode, die eine gegebene Anforderung verarbeitet und eine entsprechende Antwort generiert
     *
     * @param httpExchange kapselt eine empfangene HTTP-Anforderung und eine Antwort, die in einem Exchange generiert
     *            werden soll.
     */
    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        try {
            log.debug("Incoming request");
            final String requestMethod = httpExchange.getRequestMethod();
            // check neccessary, since gui can be disabled
            if (requestMethod.equals("POST")) {
                final BufferedInputStream buffered = StreamHelper.wrapPeekable(httpExchange.getRequestBody());
                if (!isMultipartFormData(httpExchange) && isContentAvailable(httpExchange, buffered)) {
                    final SourceInput serverInput = (SourceInput) InputFactory.read(buffered,
                            resolveInputName(httpExchange.getRequestURI()));
                    final Result result = this.implemenation.checkInput(serverInput);
                    write(httpExchange, serialize(result), APPLICATION_XML, resolveStatus(result));
                } else {
                    error(httpExchange, HttpStatus.SC_BAD_REQUEST, "No content supplied");
                }

            } else {
                error(httpExchange, HttpStatus.SC_METHOD_NOT_ALLOWED, "Method not supported");
            }
        } catch (final Exception e) {
            log.error("Error checking entity", e);
            error(httpExchange, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal error: " + e.getMessage());
        }
    }

    private static boolean isContentAvailable(final com.sun.net.httpserver.HttpExchange httpExchange, final BufferedInputStream buffered)
            throws IOException {
        final String length = httpExchange.getRequestHeaders().getFirst("Content-length");
        if (StringUtils.isNumeric(length)) {
            return Integer.parseInt(length) > 0;
        }
        return streamContainsContent(buffered);
    }

    private static boolean isMultipartFormData(final HttpExchange httpExchange) {
        return httpExchange.getRequestHeaders().getFirst("Content-type").startsWith("multipart");
    }

    private static boolean streamContainsContent(final BufferedInputStream requestBody) throws IOException {
        return requestBody.available() > 0;

    }

    private static String resolveInputName(final URI requestURI) {
        final String path = requestURI.getPath();
        if (path.equalsIgnoreCase("/")) {
            return "supplied_instance_" + counter.incrementAndGet();
        }
        return path.substring((path.lastIndexOf('/') + 1));
    }

    private static int resolveStatus(final Result result) {
        if (result.isProcessingSuccessful()) {
            return result.isAcceptable() ? HttpStatus.SC_OK : HttpStatus.SC_NOT_ACCEPTABLE;
        }
        return HttpStatus.SC_UNPROCESSABLE_ENTITY;
    }

    private byte[] serialize(final Result result) {
        try ( final ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
            final Serializer serializer = this.processor.newSerializer(out);
            serializer.serializeNode(result.getReport());
            return out.toByteArray();
        } catch (final SaxonApiException | IOException e) {
            log.error("Error serializing result", e);
            throw new IllegalStateException("Can not serialize result", e);
        }
    }

}
