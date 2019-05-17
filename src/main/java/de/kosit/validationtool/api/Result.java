package de.kosit.validationtool.api;

import org.w3c.dom.Document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.XdmNode;

/**
 * API Rückgabe Objekt des Ergebnisses des Validierungsprozesses.
 * 
 * @author Andreas Penski
 */
@Getter
@RequiredArgsConstructor
public class Result {

    /** Der generierte Report. */
    private final XdmNode report;

    /** Das evaluierte Ergebnis. */
    private final AcceptRecommendation acceptRecommendation;

    /**
     * Gibt den Report als W3C-{@link Document} zurück.
     * 
     * @return der Report
     */
    public Document getReportDocument() {
        return (Document) NodeOverNodeInfo.wrap(getReport().getUnderlyingNode());
    }

    /**
     * Schnellzugriff auf die Empfehlung zur Weiterverarbeitung des Dokuments.
     * 
     * @return true wenn {@link AcceptRecommendation#ACCEPTABLE}
     */
    public boolean isAcceptable() {
        return AcceptRecommendation.ACCEPTABLE.equals(acceptRecommendation);
    }

}
