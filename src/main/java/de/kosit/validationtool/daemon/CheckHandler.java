package de.kosit.validationtool.daemon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.net.httpserver.HttpExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.impl.input.SourceInput;

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
                final InputStream inputStream = httpExchange.getRequestBody();
                if (inputStream.available() > 0) {
                    final SourceInput serverInput = (SourceInput) InputFactory.read(inputStream,
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
