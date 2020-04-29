package de.kosit.validationtool.daemon;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.ObjectFactory;

/**
 * Wir benötigen einen Handler, der zur Verarbeitung von HTTP-Anforderungen aufgerufen wird , und hier für Verarbeitung
 * das GET Request um Health-Endpunkt zu erstellen. Die Klasse HealthHandler implementiert diese Schnittstelle
 */
@Slf4j
class HealthHandler implements HttpHandler {

    /**
     * Klasse zur Erzeugung Health Xml , die optiamle Status.
     *
     * @author Roula Antoun
     */
    @Slf4j
    static class Health {

        private final long freeMemory;

        private final long maxMemory;

        private final long totalMemory;

        private final Configuration config;

        Health(final Configuration config) {

            final Runtime runtime = Runtime.getRuntime();
            this.freeMemory = runtime.freeMemory();
            this.maxMemory = runtime.maxMemory();
            this.totalMemory = runtime.totalMemory();
            this.config = config;
        }

        /**
         * Methode, die schreibt das Health Xml für optimale Status
         *
         */
        Document writeHealthXml() {
            final DocumentBuilder dBuilder = ObjectFactory.createDocumentBuilder(false);
            final Document doc = dBuilder.newDocument();
            final Element rootElement = doc.createElementNS("https://localhost:8080/Health", "Health");
            doc.appendChild(rootElement);
            rootElement.appendChild(getMemory(doc, this.freeMemory, this.maxMemory, this.totalMemory));
            rootElement.appendChild(getState(doc));
            rootElement.appendChild(getScenario(doc, this.config));
            return doc;
        }

        /**
         * Methode, die schreibt das System Status Node im Xml File
         *
         * @param doc Vom Typ Dokument.
         *
         */
        private static Node getState(final Document doc) {
            final Element state = doc.createElement("state");
            state.setAttribute("indicator", "OK");
            final Element stateNode = doc.createElement("message");
            stateNode.appendChild(doc.createTextNode("System is up and running normally"));
            state.appendChild(stateNode);
            return state;
        }

        /**
         * Methode, die schreibt das Scnarios Information Node im Xml File
         *
         * @param doc Vom Typ Dokument .
         * @param config Vom Typ {@link Configuration} das verwendete scenario.
         *
         */
        private static Node getScenario(final Document doc, final Configuration config) {
            final Element scenario = doc.createElement("scenario");
            final Element scenarioNameNode = doc.createElement("name");
            scenarioNameNode.appendChild(doc.createTextNode(config.getName()));
            scenario.appendChild(scenarioNameNode);
            return scenario;
        }

        /**
         * Methode, die schreibt das Scnarios Information Node im Xml File
         *
         * @param doc Vom Typ Dokument .
         * @param freeMemory Vom Typ long , der freier Speicher.
         * @param maxMemory Vom Typ long , der maximaler Speicher
         * @param totalMemory Vom Typ long , der Gesamte speicher.
         *
         */
        private static Node getMemory(final Document doc, final long freeMemory, final long maxMemory, final long totalMemory) {
            final Element memory = doc.createElement("memoryState");
            final String freeM = Long.toString(freeMemory);
            final Element freeMNode = doc.createElement("freeMemory");
            freeMNode.appendChild(doc.createTextNode(freeM));
            memory.appendChild(freeMNode);
            final String maxM = Long.toString(maxMemory);
            final Element maxMNode = doc.createElement("maxMemory");
            maxMNode.appendChild(doc.createTextNode(maxM));
            memory.appendChild(maxMNode);
            final String totalM = Long.toString(totalMemory);
            final Element totalMNode = doc.createElement("totalMemory");
            totalMNode.appendChild(doc.createTextNode(totalM));
            memory.appendChild(totalMNode);
            return memory;
        }
    }

    private final Configuration scenarios;

    HealthHandler(final Configuration config) {
        this.scenarios = config;
    }

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        final Health health = new Health(this.scenarios);
        final Document doc = health.writeHealthXml();
        try {
            Daemon.writeOutputstreamArray(httpExchange, doc);
        } catch (final TransformerException e) {
            Daemon.writeError(httpExchange, 500, e.getMessage());
            log.error("Fehler beim Erzeugen der Status-Information", e);
        }
    }
}
