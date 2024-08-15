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

package de.kosit.validationtool.daemon;

import com.sun.net.httpserver.HttpExchange;
import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.impl.DefaultCheck;
import de.kosit.validationtool.impl.Printer;
import de.kosit.validationtool.impl.input.SourceInput;
import de.kosit.validationtool.impl.input.StreamHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.s9api.*;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
public class TransformHandler extends BaseHandler {

    private static final AtomicLong counter = new AtomicLong(0);

    private final Check implemenation;

    private final Processor processor;

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        try {
            Printer.writeOut("\nTransformHandler");

            final String requestMethod = httpExchange.getRequestMethod();
            if (requestMethod.equals("POST")) {
                final BufferedInputStream buffered = StreamHelper.wrapPeekable(httpExchange.getRequestBody());
                final SourceInput serverInput = (SourceInput) InputFactory.read(buffered, resolveInputName(httpExchange.getRequestURI()));
                // final Result result = this.implemenation.checkInput(serverInput);

                write(httpExchange, serializeXR2HTML(serverInput.getSource()), APPLICATION_XML, HttpStatus.SC_OK);
            }
        } catch (final Exception e) {
            Printer.writeOut("Error TransformHandler", e);
            error(httpExchange, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal error: " + e.getMessage());
        }
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

    private byte[] serializeXR2HTML(final Source source) {
        try ( final ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
            Processor processor = new Processor(false);
            XsltCompiler xsltCompiler = processor.newXsltCompiler();

            // 1. XSLT -> XR
            XsltExecutable stylesheet = xsltCompiler.compile(new StreamSource(new File("visualization/xsl/ubl-invoice-xr.xsl")));
            Xslt30Transformer transformer = stylesheet.load30();
            transformer.transform(source, processor.newSerializer(out));

            // 2. XR -> HTML
            XsltExecutable htmlStylesheet = xsltCompiler.compile(new StreamSource(new File("visualization/xsl/xrechnung-html.xsl")));
            Xslt30Transformer htmlTransformer = htmlStylesheet.load30();

            try ( final ByteArrayOutputStream htmlOut = new ByteArrayOutputStream() ) {
                // Use the XR output as the input for the HTML transformation
                htmlTransformer.transform(new StreamSource(new ByteArrayInputStream(out.toByteArray())), processor.newSerializer(htmlOut));

                // Return the HTML output
                return htmlOut.toByteArray();
            }
        } catch (final IOException | SaxonApiException e) {
            Printer.writeOut("Error serializeXR2HTML IOException result", e);
            throw new IllegalStateException("Can not serialize result", e);
        }
    }
}
