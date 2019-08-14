package de.kosit.validationtool.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import de.kosit.validationtool.api.AcceptRecommendation;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.api.XmlError;
import de.kosit.validationtool.model.reportInput.CreateReportInput;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.XdmNode;

/**
 * Das Default-{@link Result} für die Rückgabe in der API
 * 
 * @author Andreas Penski
 */
public class DefaultResult implements Result {

    /** Der generierte Report. */
    @Getter
    private final XdmNode report;

    /** Die vom Validator erstelle interne Berichts-'Vorstufe' */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private CreateReportInput reportInput;

    /** Das evaluierte Ergebnis. */
    @Getter
    private final AcceptRecommendation acceptRecommendation;

    private final HtmlExtractor htmlExtraction;

    @Setter(AccessLevel.PACKAGE)
    @Getter
    private List<XmlError> schemaViolations;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private List<SchematronOutput> schematronResult;

    @Getter
    @Setter
    private boolean processingSuccessful;

    @Getter
    @Setter
    private boolean wellformed;

    public DefaultResult(final XdmNode report, final AcceptRecommendation recommendation, final HtmlExtractor htmlExtractor) {
        this.report = report;
        this.acceptRecommendation = recommendation;
        this.htmlExtraction = htmlExtractor;
    }

    @Override
    public List<String> getProcessingErrors() {
        return getReportInput().getProcessingError() != null ? getReportInput().getProcessingError().getError() : Collections.emptyList();
    }

    /**
     * Gibt den Report als W3C-{@link Document} zurück.
     *
     * @return der Report
     */
    @Override
    public Document getReportDocument() {
        return (Document) NodeOverNodeInfo.wrap(getReport().getUnderlyingNode());
    }

    /**
     * Schnellzugriff auf die Empfehlung zur Weiterverarbeitung des Dokuments.
     *
     * @return true wenn {@link AcceptRecommendation#ACCEPTABLE}
     */
    @Override
    public boolean isAcceptable() {
        return isProcessingSuccessful() && AcceptRecommendation.ACCEPTABLE.equals(this.acceptRecommendation);
    }

    @Override
    public boolean isSchemaValid() {
        return getSchemaViolations() != null && getSchemaViolations().isEmpty();
    }

    /**
     * Extrahiert evtl. im Report vorhandene HTML-Fragmente als String.
     * 
     * @return Liste mit HTML Strings.
     */
    public List<String> extractHtmlAsString() {
        return this.htmlExtraction.extractAsString(getReport());
    }

    /**
     * Extrahiert evtl. im Report vorhandene HTML-Fragmente.
     *
     * @return Liste mit HTML Nodes.
     */
    public List<XdmNode> extractHtml() {
        return this.htmlExtraction.extract(getReport());
    }

    /**
     * Extrahiert evtl. im Report vorhandene HTML-Fragmente als {@link Element}.
     *
     * @return Liste mit HTML Elementen.
     */
    public List<Element> extractHtmlAsElement() {
        return this.htmlExtraction.extractAsElement(getReport());
    }

    /**
     * Gibt alle Schematron-Ergebnisse vom Typ {@link FailedAssert} zurück.
     * 
     * @return die {@link FailedAssert}
     */
    public List<FailedAssert> getFailedAsserts() {
        return filterSchematronResult(FailedAssert.class);
    }

    private <T> List<T> filterSchematronResult(final Class<T> type) {
        return getSchematronResult().stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }

}
