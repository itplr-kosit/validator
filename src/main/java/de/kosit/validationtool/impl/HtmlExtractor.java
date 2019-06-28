package de.kosit.validationtool.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import lombok.RequiredArgsConstructor;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

/**
 * Funktion zum Extrahieren von HTML-Artefakten / Knoten aus einem XML-Dokument.
 *
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public class HtmlExtractor {

    private final ContentRepository repository;

    private XPathExecutable executable;

    public List<XdmNode> extract(XdmNode xdmSource) {
        try {
            final XPathSelector selector = getSelector();
            selector.setContextItem(xdmSource);
            return selector.stream().map(this::castToNode).collect(Collectors.toList());

        } catch (SaxonApiException e) {
            throw new IllegalStateException("Can not extract html content", e);
        }
    }

    private XdmNode castToNode(final XdmItem xdmItem) {
        return (XdmNode) xdmItem;
    }

    private XPathSelector getSelector() {
        if (executable == null) {
            Map<String, String> ns = new HashMap<>();
            ns.put("html", "http://www.w3.org/1999/xhtml");
            executable = repository.createXPath("//html:html", ns);
        }
        return executable.load();
    }

    private static String convertToString(final XdmNode element) {
        try {
            final StringWriter writer = new StringWriter();
            final Serializer serializer = ObjectFactory.createProcessor().newSerializer(writer);
            serializer.serializeNode(element);
            return writer.toString();
        } catch (final SaxonApiException e) {
            throw new IllegalStateException("Can not convert to string", e);
        }
    }

    /**
     * Extrahiert evtl. vorhandene HTML-Knoten als String.
     * 
     * @param node der root knoten
     * @return HTML-Fragment als String
     */
    public List<String> extractAsString(final XdmNode node) {
        return extract(node).stream().map(HtmlExtractor::convertToString).collect(Collectors.toList());
    }

    public List<Element> extractAsElement(final XdmNode node) {
        return extract(node).stream().map(HtmlExtractor::convertToElement).collect(Collectors.toList());
    }

    private static Element convertToElement(final XdmNode xdmItem) {
        return (Element) NodeOverNodeInfo.wrap(xdmItem.getUnderlyingNode());
    }
}
