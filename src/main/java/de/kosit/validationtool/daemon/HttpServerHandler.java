package de.kosit.validationtool.daemon;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.input.SourceInput;

/**
 * Wir benötigen einen Handler, der zur Verarbeitung von HTTP-Anforderungen aufgerufen wird um hier die Verarbeitung des
 * POST Request zu realisieren.
 */
@Slf4j
class HttpServerHandler implements HttpHandler {

    private static final AtomicLong counter = new AtomicLong(0);

    private final Check implemenation;

    HttpServerHandler(final Check check) {
        this.implemenation = check;
    }

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
                    final SourceInput serverInput = (SourceInput) InputFactory.read(inputStream, "Prüfling" + counter.incrementAndGet());
                    Daemon.writeOutputstreamArray(httpExchange, this.implemenation.check(serverInput));
                } else {
                    Daemon.writeError(httpExchange, 400, "XML-Inhalt erforderlich!");
                }

            } else {
                Daemon.writeError(httpExchange, 405, "Es ist nur die POST-Methode erlaubt!");
            }
        } catch (final Exception e) {
            Daemon.writeError(httpExchange, 500, "Interner Fehler bei der Verarbeitung des Requests: " + e.getMessage());
            log.error("Es ist ein Fehler aufgetreten. Das Dokument kann nicht geprüft werden", e);
        }
    }

}
