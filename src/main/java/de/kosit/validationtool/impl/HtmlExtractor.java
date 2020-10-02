/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kosit.validationtool.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import lombok.RequiredArgsConstructor;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
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

    private final Processor processor;

    private XPathExecutable executable;

    public List<XdmNode> extract(final XdmNode xdmSource) {
        try {
            final XPathSelector selector = getSelector();
            selector.setContextItem(xdmSource);
            return selector.stream().map(HtmlExtractor::castToNode).collect(Collectors.toList());

        } catch (final SaxonApiException e) {
            throw new IllegalStateException("Can not extract html content", e);
        }
    }

    private static XdmNode castToNode(final XdmItem xdmItem) {
        return (XdmNode) xdmItem;
    }

    private XPathSelector getSelector() {
        if (this.executable == null) {
            final Map<String, String> ns = new HashMap<>();
            ns.put("html", "http://www.w3.org/1999/xhtml");
            this.executable = createXPath("//html:html", ns);
        }
        return this.executable.load();
    }

    private XPathExecutable createXPath(final String expression, final Map<String, String> namespaces) {
        try {
            final XPathCompiler compiler = this.processor.newXPathCompiler();
            if (namespaces != null) {
                namespaces.forEach(compiler::declareNamespace);
            }
            return compiler.compile(expression);
        } catch (final SaxonApiException e) {
            throw new IllegalStateException(String.format("Can not compile xpath match expression '%s'",
                    StringUtils.isNotBlank(expression) ? expression : "EMPTY EXPRESSION"), e);
        }
    }

    private String convertToString(final XdmNode element) {
        try {
            final StringWriter writer = new StringWriter();
            final Serializer serializer = this.processor.newSerializer(writer);
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
        return extract(node).stream().map(this::convertToString).collect(Collectors.toList());
    }

    public List<Element> extractAsElement(final XdmNode node) {
        return extract(node).stream().map(HtmlExtractor::convertToElement).collect(Collectors.toList());
    }

    private static Element convertToElement(final XdmNode xdmItem) {
        return (Element) NodeOverNodeInfo.wrap(xdmItem.getUnderlyingNode());
    }
}
