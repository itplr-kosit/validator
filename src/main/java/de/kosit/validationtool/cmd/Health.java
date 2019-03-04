package de.kosit.validationtool.cmd;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.model.scenarios.Scenarios;

/**
 * Klasse zur Erzeugung Health Xml , die optiamle Status.
 *
 * @author Roula Antoun
 */
@Slf4j
class Health {

    private final long freeMemory;

    private final long maxMemory;

    private final long totalMemory;

    private final Scenarios scenarios;

    Health(Scenarios scenarios) {

        Runtime runtime = Runtime.getRuntime();
        freeMemory = runtime.freeMemory();
        maxMemory = runtime.maxMemory();
        totalMemory = runtime.totalMemory();
        this.scenarios = scenarios;
    }

    /**
     * Methode, die schreibt das Health Xml für optimale Status
     *
     */
    Document writeHealthXml() {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        Document doc = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.newDocument();
            Element rootElement = doc.createElementNS("https://localhost:8080/Health", "Health");
            doc.appendChild(rootElement);
            rootElement.appendChild(getMemory(doc, freeMemory, maxMemory, totalMemory));
            rootElement.appendChild(getState(doc));
            rootElement.appendChild(getScenario(doc, scenarios));
        } catch (ParserConfigurationException e) {
            log.error("Fehler beim Schreiben der Status-Informationen", e);
        }
        return doc;
    }

    /**
     * Methode, die schreibt das System Status Node im Xml File
     * 
     * @param doc Vom Typ Dokument.
     *
     */
    private Node getState(Document doc) {
        Element state = doc.createElement("state");
        state.setAttribute("indicator", "OK");
        Element stateNode = doc.createElement("message");
        stateNode.appendChild(doc.createTextNode("System is up and running normally"));
        state.appendChild(stateNode);
        return state;
    }

    /**
     * Methode, die schreibt das Scnarios Information Node im Xml File
     * 
     * @param doc Vom Typ Dokument .
     * @param scenarios Vom Typ {@link Scenarios} das verwendete scenario.
     *
     */
    private Node getScenario(Document doc, Scenarios scenarios) {
        Element scenario = doc.createElement("scenario");
        Element scenarioNameNode = doc.createElement("name");
        scenarioNameNode.appendChild(doc.createTextNode(scenarios.getName()));
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
    private static Node getMemory(Document doc, long freeMemory, long maxMemory, long totalMemory) {
        Element memory = doc.createElement("memoryState");
        String freeM = Long.toString(freeMemory);
        Element freeMNode = doc.createElement("freeMemory");
        freeMNode.appendChild(doc.createTextNode(freeM));
        memory.appendChild(freeMNode);
        String maxM = Long.toString(maxMemory);
        Element maxMNode = doc.createElement("maxMemory");
        maxMNode.appendChild(doc.createTextNode(maxM));
        memory.appendChild(maxMNode);
        String totalM = Long.toString(totalMemory);
        Element totalMNode = doc.createElement("totalMemory");
        totalMNode.appendChild(doc.createTextNode(totalM));
        memory.appendChild(totalMNode);
        return memory;
    }
}
