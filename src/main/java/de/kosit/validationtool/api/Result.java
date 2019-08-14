package de.kosit.validationtool.api;

import java.util.List;

import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.w3c.dom.Document;

import net.sf.saxon.s9api.XdmNode;

/**
 * API Rückgabe Objekt des Ergebnisses des Validierungsprozesses.
 * 
 * @author Andreas Penski
 */
public interface Result {

    /**
     * Zeigt an, ob die Verarbeitung durch den Validator erfolgreich durchlaufen wurde. Diese Funktion macht ausdrücklich
     * keine Aussage über die zur Akzeptanz.
     * 
     * @return true, wenn die Verarbeitung komplett und erfolgreich durchlaufen wurde
     * @see #getAcceptRecommendation()
     */
    boolean isProcessingSuccessful();

    /**
     * Gibt eine Liste mit Verarbeitungsfehlermeldungen zurück.
     * 
     * @return Liste mit Fehlermeldungen
     */
    List<String> getProcessingErrors();

    /**
     * Der generierte Report.
     */
    XdmNode getReport();

    /**
     * Das evaluierte Ergebnis.
     */
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

    /**
     * Liefert die Ergebnisse der Schematron-Prüfungen, in der Reihenfolge der Szenario-Konfiguration.
     * 
     * @return Liste mit Schematron-Ergebnissen
     */
    List<SchematronOutput> getSchematronResult();

    /**
     * Liefert ein true, wenn keine Schema-Violations vorhanden sind.
     * 
     * @return true wenn Schema-valide
     */
    boolean isSchemaValid();

    /**
     * Liefert ein true, wenn der Prüfling eine well-formed XML-Datei ist.
     * 
     * @return true wenn well-formed
     */
    boolean isWellformed();
}
