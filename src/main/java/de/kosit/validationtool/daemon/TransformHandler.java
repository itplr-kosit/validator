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
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
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
        try ( final ByteArrayOutputStream out = new ByteArrayOutputStream();
              final ByteArrayOutputStream sourceCopy = new ByteArrayOutputStream() ) {
            Processor processor = new Processor(false);
            XsltCompiler xsltCompiler = processor.newXsltCompiler();

            // Copy source content if it's a StreamSource
            if (source instanceof StreamSource) {
                try ( InputStream sourceInputStream = ((StreamSource) source).getInputStream() ) {
                    sourceInputStream.transferTo(sourceCopy);
                }
            }

            // 0. Check Namespace of the XML Source
            Source namespaceCheckSource = (source instanceof StreamSource)
                    ? new StreamSource(new ByteArrayInputStream(sourceCopy.toByteArray()))
                    : source;
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMResult domResult = new DOMResult();
            transformer.transform(namespaceCheckSource, domResult);
            Document doc = (Document) domResult.getNode();
            String namespace = doc.getDocumentElement().getNamespaceURI();
            String xsltStyle = "visualization/xsl/";
            switch (namespace) {
                case "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2":
                    xsltStyle += "ubl-invoice-xr.xsl";
                    break;
                case "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2":
                    xsltStyle += "ubl-creditnote-xr.xsl";
                    break;
                case "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100":
                    xsltStyle += "cii-xr.xsl";
                    break;
                default:
                    throw new TransformerException("Unknown namespace:" + namespace);
            }

            // 1. XSLT -> XR
            Source firstTransformSource = (source instanceof StreamSource)
                    ? new StreamSource(new ByteArrayInputStream(sourceCopy.toByteArray()))
                    : source;
            XsltExecutable stylesheet = xsltCompiler.compile(new StreamSource(new File(xsltStyle)));
            Xslt30Transformer xslt30Transformer = stylesheet.load30();
            xslt30Transformer.transform(firstTransformSource, processor.newSerializer(out));

            // 2. XR -> HTML
            String htmlStylePath = "visualization/xsl/xrechnung-html.xsl";
            XsltExecutable htmlStylesheet = xsltCompiler.compile(new StreamSource(new File(htmlStylePath)));
            Xslt30Transformer htmlTransformer = htmlStylesheet.load30();

            try ( final ByteArrayOutputStream htmlOut = new ByteArrayOutputStream() ) {
                // Create a new ByteArrayInputStream from out's content without closing out
                ByteArrayInputStream xrInputStream = new ByteArrayInputStream(out.toByteArray());
                htmlTransformer.transform(new StreamSource(xrInputStream), processor.newSerializer(htmlOut));
                return htmlOut.toByteArray();
            }
        } catch (final IOException e) {
            Printer.writeOut("Error serializeXR2HTML IOException result", e);
            throw new IllegalStateException("Can not serialize result", e);
        } catch (final SaxonApiException e) {
            Printer.writeOut("Error serializeXR2HTML SaxonApiException result", e);
            throw new IllegalStateException("Can not serialize result", e);
        } catch (TransformerException e) {
            Printer.writeOut("Error serializeXR2HTML TransformerException result", e);
            throw new IllegalStateException("Can not serialize result", e);
        }
    }
}
