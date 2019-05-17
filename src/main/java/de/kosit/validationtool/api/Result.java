package de.kosit.validationtool.api;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sf.saxon.s9api.XdmNode;

/**
 * API Rückgabe Objekt des Ergebnisses des Validierungsprozesses.
 * 
 * @author Andreas Penski
 */

public interface Result {

    /** Der generierte Report. */
    XdmNode getReport();

    /** Das evaluierte Ergebnis. */
    AcceptRecommendation getAcceptRecommendation();

    /**
     * Gibt den Report als W3C-{@link Document} zurück.
     * 
     * @return der Report
     */
    Document getReportDocument();

    /**
     * Schnellzugriff auf die Empfehlung zur Weiterverarbeitung des Dokuments.
     * 
     * @return true wenn {@link AcceptRecommendation#ACCEPTABLE}
     */
    boolean isAcceptable();

    /**
     * Gibt eine Liste mit gefundenen Schema-Validation-Fehler zurück. Diese Liste ist leer, wenn keine Fehler gefunden
     * wurden.
     */
    List<XmlError> getSchemaViolations();
    

    // TODO scheitert momentan daran, das intern kein svlr o.ä. zur Verfügung steht
    // List<XmlError> getSchematronResult();
    
    
    /**
     * Extrahiert evtl. vorhandenes HTML aus dem Report und stellt diese als {@link XdmNode}-Objekt zur Verfügung.
     * 
     * @return HTML-Nodes
     */
    List<XdmNode> extractHtml();

    /**
     * Extrahiert evtl. vorhandenes HTML aus dem Report und stellt diese als {@link String} zur Verfügung.
     * 
     * @return HTML-Nodes
     */
    List<String> extractHtmlAsString();

    /**
     * Extrahiert evtl. vorhandenes HTML aus dem Report und stellt diese im als {@link Element}-Objekt zur Verfügung.
     * 
     * @return HTML-Nodes
     */
    List<Element> extractHtmlAsElement();

}
