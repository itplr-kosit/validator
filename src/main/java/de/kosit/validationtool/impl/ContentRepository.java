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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.ResolvingConfigurationStrategy;
import de.kosit.validationtool.impl.Scenario.Transformation;
import de.kosit.validationtool.impl.xml.RelativeUriResolver;
import de.kosit.validationtool.impl.xml.StringTrimAdapter;
import de.kosit.validationtool.model.scenarios.NamespaceType;
import de.kosit.validationtool.model.scenarios.ResourceType;
import de.kosit.validationtool.model.scenarios.ScenarioType;
import de.kosit.validationtool.model.scenarios.ValidateWithSchematron;

import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Repository für verschiedene XML Artefakte zur Vearbeitung der Prüfszenarien.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class ContentRepository {

    private Schema reportInputSchema;

    @Getter
    private final Processor processor;

    private final URI repository;

    private final URIResolver resolver;

    private final UnparsedTextURIResolver unparsedTextURIResolver;

    private final SchemaFactory schemaFactory;

    @Getter
    private final ResolvingConfigurationStrategy resolvingConfigurationStrategy;

    /**
     * Creates a new {@link ContentRepository} based on configured security and resolving strategy and the specified
     * repository location.
     * 
     * @param strategy the security and resolving strategy
     * @param repository the repository.
     */
    public ContentRepository(final ResolvingConfigurationStrategy strategy, final URI repository) {
        this.repository = repository;
        this.resolvingConfigurationStrategy = strategy;
        this.processor = this.resolvingConfigurationStrategy.getProcessor();
        this.resolver = this.resolvingConfigurationStrategy.createResolver(repository);
        this.unparsedTextURIResolver = this.resolvingConfigurationStrategy.createUnparsedTextURIResolver(repository);
        this.schemaFactory = this.resolvingConfigurationStrategy.createSchemaFactory();
    }

    @SuppressWarnings("squid:S2095")
    private static Source resolve(final URL resource) {
        try {
            return new StreamSource(resource.openStream(), resource.toURI().getRawPath());
        } catch (final IOException | URISyntaxException e) {
            throw new IllegalStateException("Can not load schema for resource " + resource.getPath(), e);
        }
    }

    private Schema createSchema(final Source[] schemaSources, final LSResourceResolver resourceResolver) {
        try {
            final SchemaFactory sf = this.schemaFactory;
            sf.setResourceResolver(resourceResolver);
            return sf.newSchema(schemaSources);
        } catch (final SAXException e) {
            throw new IllegalArgumentException("Can not load schema from sources " + schemaSources[0].getSystemId(), e);
        }
    }

    private Schema createSchema(final Source[] schemaSources) {
        return createSchema(schemaSources, null);
    }

    /**
     * Lädt ein XSL von der angegebenen URI
     *
     * @param uri die URI der XSL Definition
     * @return ein XSLT Executable
     */
    public XsltExecutable loadXsltScript(final URI uri) {
        log.info("Loading XSLT script from  {}", uri);
        final XsltCompiler xsltCompiler = getProcessor().newXsltCompiler();
        final CollectingErrorEventHandler listener = new CollectingErrorEventHandler();
        try {
            xsltCompiler.setErrorListener(listener);
            if (getResolver() != null) {
                // otherwise use default resolver
                xsltCompiler.setURIResolver(getResolver());
            }

            return xsltCompiler.compile(resolveInRepository(uri));
        } catch (final SaxonApiException e) {
            listener.getErrors().forEach(event -> event.log(log));
            throw new IllegalStateException("Can not compile xslt executable for uri " + uri, e);
        } finally {
            if (!listener.hasErrors() && listener.hasEvents()) {
                log.warn("Received warnings or errors while loading a xslt script {}", uri);
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
    public Schema createSchema(final URL url) {
        return createSchema(url, null);
    }

    public Schema createSchema(final URI uri) {
        return createSchema(new Source[] { resolveInRepository(uri) });
    }

    public Schema createSchema(final URL url, final LSResourceResolver resourceResolver) {
        log.info("Load schema from source {}", url.getPath());
        return createSchema(new Source[] { resolve(url) }, resourceResolver);
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
        if (this.reportInputSchema == null) {
            final Source source = resolve(ContentRepository.class.getResource("/xsd/createReportInput.xsd"));
            this.reportInputSchema = createSchema(new Source[] { source }, new ClassPathResourceResolver("/xsd"));
        }
        return this.reportInputSchema;
    }

    /**
     * Erzeugt ein Schema auf Basis der übegebenen URIs
     * 
     * @param uris die uris in String-Repräsentation
     * @return das Schema
     */
    public Schema createSchema(final Collection<String> uris) {
        return createSchema(uris.stream().map(s -> resolveInRepository(URI.create(s))).toArray(Source[]::new));
    }

    /**
     * Liefert das Schema zu diesem Szenario.
     *
     * @return das passende Schema
     */
    public Schema createSchema(final ScenarioType s) {
        Schema schema = null;
        if (s.getValidateWithXmlSchema() != null) {
            final List<String> schemaResources = s.getValidateWithXmlSchema().getResource().stream().map(ResourceType::getLocation)
                    .collect(Collectors.toList());
            schema = createSchema(schemaResources);
        }
        return schema;
    }

    private Source resolveInRepository(final URI source) {
        try {
            if (this.resolver == null) {
                // TODO wie wird ohne resolver das richtige Artefakt gefunden?
                // assume local
                final URI resolved = RelativeUriResolver.resolve(source, this.repository);
                return new StreamSource(resolved.toASCIIString());
            }
            return this.resolver.resolve(source.toString(), this.repository.toString());
        } catch (final TransformerException e) {
            log.error("Error resolving source {}", source, e);
            throw new IllegalStateException(String.format("Can not resolve %s in repository %s", source, this.repository), e);
        }
    }

    /**
     * Erzeugt einen [@link XPathExecutable} auf Basis der angegebenen Informationen.
     * 
     * @param expression der XPATH-Ausdruck
     * @param namespaces optionale Namespace-Mappings
     * @return ein kompiliertes Executable
     */
    public XPathExecutable createXPath(final String expression, final Map<String, String> namespaces) {
        try {
            final XPathCompiler compiler = getProcessor().newXPathCompiler();
            if (namespaces != null) {
                namespaces.forEach(compiler::declareNamespace);
            }
            return compiler.compile(expression);
        } catch (final SaxonApiException e) {
            throw new IllegalStateException(String.format("Can not compile xpath match expression '%s'",
                    StringUtils.isNotBlank(expression) ? expression : "EMPTY EXPRESSION"), e);
        }
    }

    /**
     * Returns the {@link URIResolver} to use for resolving xml artifacts.
     * 
     * @return the resolver
     */
    public URIResolver getResolver() {
        return this.resolver;
    }

    public UnparsedTextURIResolver getUnparsedTextURIResolver() {
        return this.unparsedTextURIResolver;
    }

    /**
     * Gibt eine Transformation zurück.
     *
     * @return initialisierte Transformation
     */
    public Transformation createReportTransformation(final ScenarioType t) {
        final ResourceType resource = t.getCreateReport().getResource();
        return createTransformation(resource);
    }

    public Transformation createTransformation(final ResourceType resource) {
        final XsltExecutable executable = loadXsltScript(URI.create(resource.getLocation()));
        return new Transformation(executable, resource);
    }

    public XPathExecutable createMatchExecutable(final ScenarioType s) {
        final Map<String, String> namespaces = s.getNamespace().stream()
                .collect(Collectors.toMap(NamespaceType::getPrefix, ns -> StringTrimAdapter.trim(ns.getValue())));
        return createXPath(s.getMatch(), namespaces);
    }

    public XPathExecutable createAccepptExecutable(final ScenarioType s) {
        final Map<String, String> namespaces = s.getNamespace().stream()
                .collect(Collectors.toMap(NamespaceType::getPrefix, ns -> StringTrimAdapter.trim(ns.getValue())));
        return createXPath(s.getAcceptMatch(), namespaces);
    }

    public List<Transformation> createSchematronTransformations(final ScenarioType s) {
        return s.getValidateWithSchematron().isEmpty() ? Collections.emptyList()
                : s.getValidateWithSchematron().stream().map(this::createSchematronTransformation).collect(Collectors.toList());
    }

    public Transformation createSchematronTransformation(final ValidateWithSchematron validateWithSchematron) {
        return createTransformation(validateWithSchematron.getResource());
    }
}
