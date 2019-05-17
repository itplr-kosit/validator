package de.kosit.validationtool.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import de.kosit.validationtool.api.AcceptRecommendation;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.api.XmlError;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

/**
 * @author Andreas Penski
 */

public class DefaultResult implements Result {

    /** Der generierte Report. */
    @Getter
    private final XdmNode report;

    /** Das evaluierte Ergebnis. */
    @Getter
    private final AcceptRecommendation acceptRecommendation;

    private final HtmlExtraction htmlExtraction;

    @Setter(AccessLevel.PACKAGE)
    @Getter
    private List<XmlError> schemaViolations = new ArrayList<>();

    public DefaultResult(final XdmNode report, final AcceptRecommendation recommendation, final ContentRepository repository) {
        this.report = report;
        this.acceptRecommendation = recommendation;
        this.htmlExtraction = new HtmlExtraction(repository);
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
        return AcceptRecommendation.ACCEPTABLE.equals(this.acceptRecommendation);
    }

    public List<String> extractHtmlAsString() {
        return extractHtml().stream().map(this::convertToString).collect(Collectors.toList());
    }

    private String convertToString(final XdmNode element) {
        try {
            final StringWriter writer = new StringWriter();
            final Serializer serializer = ObjectFactory.createProcessor().newSerializer(writer);
            serializer.serializeNode(element);
            return writer.toString();
        } catch (SaxonApiException e) {
            throw new IllegalStateException("Can not convert to string", e);
        }
    }

    public List<Element> extractHtmlAsElement() {
        return extractHtml().stream().map(DefaultResult::convertToElement).collect(Collectors.toList());
    }

    private static Element convertToElement(final XdmNode xdmItem) {
        return (Element) NodeOverNodeInfo.wrap(xdmItem.getUnderlyingNode());
    }

    public List<XdmNode> extractHtml() {
        return this.htmlExtraction.extract(getReport());
    }

}
