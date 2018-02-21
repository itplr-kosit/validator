/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.sf.saxon.s9api.*;

/**
 * Repository für verschiedene XML Artefakte zur Vearbeitung der Prüfszenarien.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class ContentRepository {

    @Getter
    private final Processor processor;

    private final URI repository;

    private Schema reportInputSchema;

    private static Source resolve(URL resource) {
        try {
            return new StreamSource(resource.openStream(), resource.toURI().getRawPath());
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Can not load schema for resource " + resource.getPath(), e);
        }
    }

    private static Schema createSchema(Source[] schemaSources, LSResourceResolver resourceResolver) {
        try {
            SchemaFactory sf = ObjectFactory.createSchemaFactory();
            sf.setResourceResolver(resourceResolver);
            return sf.newSchema(schemaSources);
        } catch (SAXException e) {
            throw new IllegalArgumentException("Can not load schema from sources " + schemaSources[0].getSystemId(), e);
        }
    }

    private static Schema createSchema(Source[] schemaSources) {
        return createSchema(schemaSources, null);
    }

    /**
     * Lädt ein XSL von der angegebenen URI
     *
     * @param uri die URI der XSL Definition
     * @return ein XSLT Executable
     */
    public XsltExecutable loadXsltScript(URI uri) {
        log.info("Loading XSLT script from  {}", uri);
        final XsltCompiler xsltCompiler = getProcessor().newXsltCompiler();
        final CollectingErrorEventHandler listener = new CollectingErrorEventHandler();
        try {
            xsltCompiler.setErrorListener(listener);
            xsltCompiler.setURIResolver(new RelativeUriResolver(repository));

            return xsltCompiler.compile(resolve(uri));
        } catch (SaxonApiException e) {
            listener.getErrors().forEach(event -> event.log(log));
            throw new IllegalStateException("Can not compile xslt executable for uri " + uri, e);
        } finally {
            if (!listener.hasErrors() && listener.hasEvents()) {
                log.warn("Received warnings while loading a xslt script {}", uri);
                listener.getErrors().forEach(e -> e.log(log));
            }
        }
    }

    /**
     * Erzeugt ein Schema-Objekt auf Basis der übergebenen URL.
     *
     * @param url die url
     * @return das erzeugte Schema
     */
    public Schema createSchema(URL url) {
        log.info("Load schema from source {}", url.getPath());
        return createSchema(new Source[] { resolve(url) });
    }

    /**
     * Liefert das definiert Schema für die Szenario-Konfiguration
     *
     * @return Scenario-Schema
     */
    public Schema getScenarioSchema() {
        return createSchema(ContentRepository.class.getResource("/xsd/scenarios.xsd"));
    }

    /**
     * Liefert das definierte Schema für die Validierung des [@link CreateReportInput}
     *
     * @return ReportInput-Schema
     */
    public Schema getReportInputSchema() {
        if (reportInputSchema == null) {
            final Source source = resolve(ContentRepository.class.getResource("/xsd/createReportInput.xsd"));
            reportInputSchema = createSchema(new Source[] { source }, new ClassPathResourceResolver("/xsd"));
        }
        return reportInputSchema;
    }

    /**
     * Erzeugt ein Schema auf Basis der übegebenen URIs
     * 
     * @param uris die uris in String-Repräsentation
     * @return das Schema
     */
    public Schema createSchema(Collection<String> uris) {
        return createSchema(uris.stream().map(s -> resolve(URI.create(s))).toArray(Source[]::new));
    }

    private Source resolve(URI source) {
        return new StreamSource(repository.resolve(source).toASCIIString());
    }

    /**
     * Erzeugt einen [@link XPathExecutable} auf Basis der angegebenen Informationen.
     * 
     * @param expression der XPATH-Ausdruck
     * @param namespaces optionale Namespace-Mappings
     * @return ein kompiliertes Executable
     */
    public XPathExecutable createXPath(String expression, Map<String, String> namespaces) {
        try {
            final XPathCompiler compiler = getProcessor().newXPathCompiler();
            if (namespaces != null) {
                namespaces.entrySet().forEach(n -> compiler.declareNamespace(n.getKey(), n.getValue()));
            }
            return compiler.compile(expression);
        } catch (SaxonApiException e) {
            throw new IllegalStateException(String.format("Can not compile xpath match expression '%s'",
                    StringUtils.isNotBlank(expression) ? expression : "EMPTY EXPRESSION"), e);
        }
    }

}
