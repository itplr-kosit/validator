package de.kosit.validationtool.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import net.sf.saxon.s9api.SaxonApiException;
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
public class HtmlExtraction {

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
}
