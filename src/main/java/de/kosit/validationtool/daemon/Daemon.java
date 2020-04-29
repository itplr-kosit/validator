package de.kosit.validationtool.daemon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.DefaultCheck;
import de.kosit.validationtool.impl.ObjectFactory;

/**
 * HTTP-Daemon für die Bereitstellung der Prüf-Funktionalität via http.
 *
 * @author Roula Antoun
 */
@RequiredArgsConstructor
@Setter
@Getter
@Slf4j
public class Daemon {

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
    static void writeError(final HttpExchange httpExchange, final int rCode, final String response) throws IOException {
        httpExchange.sendResponseHeaders(rCode, response.length());
        final OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * Methode, die die Antwort als String-Text schreibt
     *
     * @param httpExchange um den Antwort Body zu erhalten
     * @param doc der Report
     */
    static void writeOutputstreamArray(final HttpExchange httpExchange, final Document doc)
            throws IOException, TransformerException {
        final byte[] bytes = serialize(doc);
        final OutputStream os = httpExchange.getResponseBody();
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
    static byte[] serialize(final Document report) throws TransformerException {

        try ( final ByteArrayOutputStream bArrayOS = new ByteArrayOutputStream() ) {
            final DOMSource source = new DOMSource(report);
            final StreamResult streamResult = new StreamResult(bArrayOS);
            final Transformer transformer = ObjectFactory.createTransformer(true);
            transformer.transform(source, streamResult);
            return bArrayOS.toByteArray();
        } catch (final IOException e) {
            log.error("Report {}", e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Methode zum Starten des Servers
     * 
     * @param config the configuration to use
     */
    public void startServer(final Configuration config) {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(this.hostName, this.port), 0);
            final DefaultCheck check = new DefaultCheck(config);
            server.createContext("/", new HttpServerHandler(check));
            server.createContext("/health", new HealthHandler(config));
            server.setExecutor(Executors.newFixedThreadPool(this.threadCount));
            server.start();
            log.info("Server unter Port {} ist erfolgreich gestartet", this.port);
        } catch (final IOException e) {
            log.error("Fehler beim HttpServer erstellen: {}", e.getMessage(), e);
        }
    }
}
