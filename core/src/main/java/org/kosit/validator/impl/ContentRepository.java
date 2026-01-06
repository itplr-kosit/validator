/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
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

package org.kosit.validator.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import lombok.Data;
import net.sf.saxon.s9api.*;
import org.apache.commons.lang3.StringUtils;
import org.kosit.validator.api.SchematronCompiler;
import org.xml.sax.SAXException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.kosit.validator.api.ResolvingConfigurationStrategy;
import org.kosit.validator.impl.Scenario.Transformation;
import org.kosit.validator.impl.xml.RelativeUriResolver;
import org.kosit.validator.impl.xml.StringTrimAdapter;
import org.kosit.validator.model.scenarios.NamespaceType;
import org.kosit.validator.model.scenarios.ResourceType;
import org.kosit.validator.model.scenarios.ScenarioType;
import org.kosit.validator.model.scenarios.ValidateWithSchematron;

import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.lib.ResourceResolverWrappingURIResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;

/**
 * Repository für verschiedene XML Artefakte zur Vearbeitung der Prüfszenarien.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class ContentRepository {

    @Data
    private static final class CacheKey {

        private final String compilerId;

        private final URI uri;
    }

    @Getter
    private final Processor processor;

    private final URI repository;

    private final ResourceResolver resolver;

    private final UnparsedTextURIResolver unparsedTextURIResolver;

    private final SchemaFactory schemaFactory;

    @Getter
    private final ResolvingConfigurationStrategy resolvingConfigurationStrategy;

    private final Map<CacheKey, Source> schematronXsltCache = new ConcurrentHashMap<>();

    private final SchematronCompilerRegistry compilerRegistry;

    /**
     * Creates a new {@link ContentRepository} based on configured security and resolving strategy and the specified
     * repository location.
     * 
     * @param strategy the security and resolving strategy
     * @param repository the repository.
     */
    public ContentRepository(final Processor processor, final ResolvingConfigurationStrategy strategy, final URI repository) {
        this.repository = repository;
        this.resolvingConfigurationStrategy = strategy;
        this.processor = processor;
        this.resolver = getResolver(strategy, repository);
        this.unparsedTextURIResolver = this.resolvingConfigurationStrategy.createUnparsedTextURIResolver(repository);
        this.schemaFactory = this.resolvingConfigurationStrategy.createSchemaFactory();
        this.compilerRegistry = defaultSchematronCompilerRegistry(processor);
    }

    private static SchematronCompilerRegistry defaultSchematronCompilerRegistry(Processor processor) {
        return new SchematronCompilerRegistry(List.of(new SchXsltCompiler(processor)) // erstmal nur SchXslt
        );
    }

    @SuppressWarnings("squid:S2095")
    private static Source resolve(final URL resource) {
        try {
            return new StreamSource(resource.openStream(), resource.toURI().getRawPath());
        } catch (final IOException | URISyntaxException e) {
            throw new IllegalStateException("Can not load schema for resource " + resource.getPath(), e);
        }
    }

    private static ResourceResolver getResolver(final ResolvingConfigurationStrategy strategy, final URI repository) {
        final URIResolver uriResolver = strategy.createResolver(repository);
        final ResourceResolver resourceResolver = strategy.createResourceResolver(repository);
        if (uriResolver != null && resourceResolver != null) {
            throw new IllegalStateException("Must not provide both URIResolver and ResourceResolver");
        }
        if (uriResolver != null) {
            return new ResourceResolverWrappingURIResolver(uriResolver);
        }
        return resourceResolver;
    }

    private Schema createSchema(final Source[] schemaSources) {
        try {
            this.schemaFactory.setResourceResolver(null);
            return this.schemaFactory.newSchema(schemaSources);
        } catch (final SAXException e) {
            throw new IllegalArgumentException("Can not load schema from sources " + schemaSources[0].getSystemId(), e);
        }
    }

    /**
     * Lädt ein XSL von der angegebenen URI
     *
     * @param uri die URI der XSL Definition
     * @return ein XSLT Executable
     */
    public XsltExecutable loadXsltScript(final URI uri) {
        log.info("  Loading XSLT script from  {}", uri);
        final XsltCompiler xsltCompiler = getProcessor().newXsltCompiler();
        final CollectingErrorEventHandler listener = new CollectingErrorEventHandler();
        try {
            xsltCompiler.setErrorListener(listener);
            if (getResolver() != null) {
                // otherwise use default resolver
                xsltCompiler.setResourceResolver(getResolver());
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

    public XsltExecutable loadSchematronXslt(final URI schUri, final String compilerId) {
        log.info("Loading or compiling Schematron {} using compiler {}", schUri, compilerId);

        SchematronCompiler compiler = compilerRegistry.get(compilerId);

        CacheKey key = new CacheKey(compilerId, schUri);
        Source xsltSource = schematronXsltCache.computeIfAbsent(key, k -> compiler.compileToXslt(schUri, this::resolveInRepository));

        final XsltCompiler xsltCompiler = getProcessor().newXsltCompiler();
        try {
            return xsltCompiler.compile(xsltSource);
        } catch (final SaxonApiException e) {
            throw new IllegalStateException("Can not compile xslt executable for uri " + schUri, e);
        }
    }

    /**
     * Erzeugt ein Schema-Objekt auf Basis der übergebenen URL.
     *
     * @param url die url
     * @return das erzeugte Schema
     */
    public Schema createSchema(final URL url) {
        return createSchema(new Source[] { resolve(url) });
    }

    public Schema createSchema(final URI uri) {
        return createSchema(new Source[] { resolveInRepository(uri) });
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
            final ResourceRequest r = new ResourceRequest();
            r.baseUri = this.repository.toString();
            r.relativeUri = source.toString();
            // return this.resolver.resolve(source.toString(), this.repository.toString());
            return this.resolver.resolve(r);
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
    public ResourceResolver getResolver() {
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
    public List<Transformation> createReportTransformations(final ScenarioType t) {
        log.info("Create Report Transformations:");
        return t.getCreateReport().stream().map(createReportType -> createTransformation(createReportType.getResource()))
                .collect(Collectors.toList());
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
        log.info("Create Schematron Transformation:");

        final ResourceType resource = validateWithSchematron.getResource();
        final URI uri = URI.create(resource.getLocation());
        final String path = uri.getPath();

        final String compilerId = StringUtils.defaultIfBlank(validateWithSchematron.getCompiler(), SchXsltCompiler.COMPILER_ID);

        if (path != null && path.endsWith(".sch")) {
            final XsltExecutable executable = loadSchematronXslt(uri, compilerId);
            return new Transformation(executable, resource);
        }

        return createTransformation(validateWithSchematron.getResource());
    }

    public Transformation createIdentityTransformation() {
        final URL url = ContentRepository.class.getClassLoader().getResource("transform/identity.xsl");
        try ( final InputStream input = url.openStream() ) {
            final XsltCompiler xsltCompiler = getProcessor().newXsltCompiler();
            final XsltExecutable executable = xsltCompiler.compile(new StreamSource(input));
            final ResourceType resource = new ResourceType();
            resource.setName("identity");
            resource.setLocation(url.toString());
            return new Transformation(executable, resource);
        } catch (final IOException | SaxonApiException e) {
            throw new IllegalStateException("Error creating identity transformation", e);
        }
    }
}
