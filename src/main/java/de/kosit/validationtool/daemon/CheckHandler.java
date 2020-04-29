package de.kosit.validationtool.daemon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
     * @param httpExchange kapselt eine empfangene HTTP-Anforderung und eine Antwort, die in einem Exchange generiert werden
     *            soll.
     */
    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        try {
            log.debug("Incoming request");
            final String requestMethod = httpExchange.getRequestMethod();
            if (requestMethod.equals("POST")) {
                final InputStream inputStream = httpExchange.getRequestBody();
                if (inputStream.available() > 0) {
                    final SourceInput serverInput = (SourceInput) InputFactory.read(inputStream,
                            "supplied_instance_" + counter.incrementAndGet());
                    final Result result = this.implemenation.checkInput(serverInput);
                    write(httpExchange, serialize(result), APPLICATION_XML);
                } else {
                    error(httpExchange, 400, "No content supplied");
                }

            } else {
                error(httpExchange, 405, "Method not supported");
            }
        } catch (final Exception e) {
            error(httpExchange, 500, "Internal error: " + e.getMessage());
        }
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
