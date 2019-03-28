package de.kosit.validationtool.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.CheckConfiguration;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.DefaultCheck;
import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.model.scenarios.Scenarios;

/**
 * HTTP-Daemon für die Bereitstellung der Prüf-Funktionalität via http.
 *
 * @author Roula Antoun
 */
@RequiredArgsConstructor
@Setter
@Getter
@Slf4j
class Daemon {

    /**
     * Wir benötigen einen Handler, der zur Verarbeitung von HTTP-Anforderungen aufgerufen wird um hier die Verarbeitung des
     * POST Request zu realisieren.
     */
    @Slf4j
    private static class HttpServerHandler implements HttpHandler {

        private static final AtomicLong counter = new AtomicLong(0);

        private final Check implemenation;

        HttpServerHandler(Check check) {
            this.implemenation = check;
        }

        /**
         * Methode, die eine gegebene Anforderung verarbeitet und eine entsprechende Antwort generiert
         *
         * @param httpExchange kapselt eine empfangene HTTP-Anforderung und eine Antwort, die in einem Exchange generiert werden
         *            soll.
         */
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                log.debug("Incoming request");
                String requestMethod = httpExchange.getRequestMethod();
                if (requestMethod.equals("POST")) {
                    InputStream inputStream = httpExchange.getRequestBody();
                    Input serverInput = InputFactory.read(inputStream, "Prüfling" + counter.incrementAndGet());

                    int contentLength = serverInput.getContent().length;
                    if (contentLength != 0) {
                        writeOutputstreamArray(httpExchange, implemenation.check(serverInput));
                    } else {
                        writeError(httpExchange, 400, "XML-Inhalt erforderlich!");
                    }
                } else {
                    writeError(httpExchange, 405, "Es ist nur die POST-Methode erlaubt!");
                }
            } catch (Exception e) {
                writeError(httpExchange, 500, "Interner Fehler bei der Verarbeitung des Requests: " + e.getMessage());
                log.error("Es ist ein Fehler aufgetreten. Das Dokument kann nicht geprüft werden", e);
            }
        }

    }

    /**
     * Wir benötigen einen Handler, der zur Verarbeitung von HTTP-Anforderungen aufgerufen wird , und hier für Verarbeitung
     * das GET Request um Health-Endpunkt zu erstellen. Die Klasse HealthHandler implementiert diese Schnittstelle
     */
    @Slf4j
    static class HealthHandler implements HttpHandler {

        private final Scenarios scenarios;

        HealthHandler(Scenarios scenarios) {
            this.scenarios = scenarios;
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            Health health = new Health(scenarios);
            Document doc = health.writeHealthXml();
            try {
                writeOutputstreamArray(httpExchange, doc);
            } catch (TransformerException e) {
                writeError(httpExchange, 500, e.getMessage());
                log.error("Fehler beim Erzeugen der Status-Information", e);
            }
        }
    }

    private final URI scenarioDefinition;

    private final URI repository;

    private final String hostName;

    private final int port;

    private final int threadCount;

    /**
     * Methode, die die Antwort als String-Text schreibt
     *
     * @param httpExchange um den Antwort Body zu erhalten
     * @param rCode der Code-Status
     * @param response die String antwort, die ich anzeigen möchte
     */
    private static void writeError(HttpExchange httpExchange, int rCode, String response) throws IOException {
        httpExchange.sendResponseHeaders(rCode, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * Methode, die die Antwort als String-Text schreibt
     *
     * @param httpExchange um den Antwort Body zu erhalten
     * @param doc der Report
     */
    private static void writeOutputstreamArray(HttpExchange httpExchange, Document doc) throws IOException, TransformerException {
        final byte[] bytes = serialize(doc);
        OutputStream os = httpExchange.getResponseBody();
        httpExchange.getResponseHeaders().add("Content-Type", "application/xml");
        httpExchange.sendResponseHeaders(200, bytes.length);
        os.write(bytes);
        os.close();
        log.debug("Xml File erzeugen ist Fertig  ");
    }

    /**
     * Methode zum Serialisieren des Dokuments.
     *
     * @param report Vom Typ Dokument, aka Report .
     */
    private static byte[] serialize(Document report) throws TransformerException {

        try ( ByteArrayOutputStream bArrayOS = new ByteArrayOutputStream() ) {
            DOMSource source = new DOMSource(report);
            StreamResult streamResult = new StreamResult(bArrayOS);
            Transformer transformer = ObjectFactory.createTransformer(true);
            transformer.transform(source, streamResult);
            return bArrayOS.toByteArray();
        } catch (IOException e) {
            log.error("Report {}", e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Methode zum Starten des Servers
     */
    void startServer() {
        CheckConfiguration config = new CheckConfiguration(scenarioDefinition);
        config.setScenarioRepository(repository);
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(hostName, port), 0);
            DefaultCheck check = new DefaultCheck(config);
            server.createContext("/", new HttpServerHandler(check));
            server.createContext("/health", new HealthHandler(check.getRepository().getScenarios()));
            server.setExecutor(Executors.newFixedThreadPool(threadCount));
            server.start();
            log.info("Server unter Port {} ist erfolgreich gestartet", port);
        } catch (IOException e) {
            log.error("Fehler beim HttpServer erstellen!", e.getMessage(), e);
        }
    }
}
