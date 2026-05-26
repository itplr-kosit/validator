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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang3.StringUtils;
import org.kosit.validator.api.ResolvingConfigurationStrategy;
import org.kosit.validator.api.SchematronCompiler;
import org.kosit.validator.impl.Scenario.Transformation;
import org.kosit.validator.impl.xml.RelativeUriResolver;
import org.kosit.validator.impl.xml.StringTrimAdapter;
import org.kosit.validator.model.scenarios.NamespaceType;
import org.kosit.validator.model.scenarios.ResourceType;
import org.kosit.validator.model.scenarios.ScenarioType;
import org.kosit.validator.model.scenarios.ValidateWithSchematron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.lib.ResourceResolverWrappingURIResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Repository for various XML artifacts used to process the validation scenarios.
 *
 * @author Andreas Penski
 */
public class ContentRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentRepository.class);

    private static final class CacheKey {

        private final String compilerId;

        private final URI uri;

        public CacheKey(final String compilerId, final URI uri) {
            this.compilerId = compilerId;
            this.uri = uri;
        }

        public String getCompilerId() {
            return this.compilerId;
        }

        public URI getUri() {
            return this.uri;
        }

        @Override
        public boolean equals(final java.lang.Object o) {
            if (o == this)
                return true;
            if (!(o instanceof ContentRepository.CacheKey))
                return false;
            final ContentRepository.CacheKey other = (ContentRepository.CacheKey) o;
            final java.lang.Object this$compilerId = this.getCompilerId();
            final java.lang.Object other$compilerId = other.getCompilerId();
            if (this$compilerId == null ? other$compilerId != null : !this$compilerId.equals(other$compilerId))
                return false;
            final java.lang.Object this$uri = this.getUri();
            final java.lang.Object other$uri = other.getUri();
            if (this$uri == null ? other$uri != null : !this$uri.equals(other$uri))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $compilerId = this.getCompilerId();
            result = result * PRIME + ($compilerId == null ? 43 : $compilerId.hashCode());
            final java.lang.Object $uri = this.getUri();
            result = result * PRIME + ($uri == null ? 43 : $uri.hashCode());
            return result;
        }

        @Override
        public java.lang.String toString() {
            return "ContentRepository.CacheKey(compilerId=" + this.getCompilerId() + ", uri=" + this.getUri() + ")";
        }
    }

    private final Processor processor;

    private final URI repository;

    private final ResourceResolver resolver;

    private final UnparsedTextURIResolver unparsedTextURIResolver;

    private final SchemaFactory schemaFactory;

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
        return new SchematronCompilerRegistry(List.of(new SchXsltCompiler(), new SchXslt2Compiler(), new IsoSchematronCompiler(processor)));
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
     * Loads an XSL from the given URI.
     *
     * @param uri the URI of the XSL definition
     * @return an XSLT executable
     */
    public XsltExecutable loadXsltScript(final URI uri) {
        LOGGER.info("  Loading XSLT script from  {}", uri);
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
            listener.getErrors().forEach(event -> event.log(LOGGER));
            throw new IllegalStateException("Can not compile xslt executable for uri " + uri, e);
        } finally {
            if (!listener.hasErrors() && listener.hasEvents()) {
                LOGGER.warn("Received warnings or errors while loading a xslt script {}", uri);
                listener.getErrors().forEach(e -> e.log(LOGGER));
            }
        }
    }

    public XsltExecutable loadSchematronXslt(final URI schUri, final String compilerId) {
        LOGGER.info("Loading or compiling Schematron {} using compiler {}", schUri, compilerId);
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
     * Creates a schema object based on the given URL.
     *
     * @param url the url
     * @return the created schema
     */
    public Schema createSchema(final URL url) {
        return createSchema(new Source[] { resolve(url) });
    }

    public Schema createSchema(final URI uri) {
        return createSchema(new Source[] { resolveInRepository(uri) });
    }

    /**
     * Creates a schema based on the given URIs.
     *
     * @param uris the uris in string representation
     * @return the schema
     */
    public Schema createSchema(final Collection<String> uris) {
        return createSchema(uris.stream().map(s -> resolveInRepository(URI.create(s))).toArray(Source[]::new));
    }

    /**
     * Returns the schema for this scenario.
     *
     * @return the matching schema
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
                // TODO how is the correct artifact found without a resolver?
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
            LOGGER.error("Error resolving source {}", source, e);
            throw new IllegalStateException("Can not resolve " + source + " in repository " + this.repository, e);
        }
    }

    /**
     * Creates an {@link XPathExecutable} based on the given information.
     *
     * @param expression the XPATH expression
     * @param namespaces optional namespace mappings
     * @return a compiled executable
     */
    public XPathExecutable createXPath(final String expression, final Map<String, String> namespaces) {
        try {
            final XPathCompiler compiler = getProcessor().newXPathCompiler();
            if (namespaces != null) {
                namespaces.forEach(compiler::declareNamespace);
            }
            return compiler.compile(expression);
        } catch (final SaxonApiException e) {
            throw new IllegalStateException("Can not compile xpath match expression '"
                    + (StringUtils.isNotBlank(expression) ? expression : "EMPTY EXPRESSION") + "'", e);
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
     * Returns a transformation.
     *
     * @return initialized transformation
     */
    public List<Transformation> createReportTransformations(final ScenarioType t) {
        LOGGER.info("Create Report Transformations:");
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
        LOGGER.info("Create Schematron Transformation:");
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
        try ( InputStream input = url.openStream() ) {
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

    public ContentRepository(final Processor processor, final URI repository, final ResourceResolver resolver,
            final UnparsedTextURIResolver unparsedTextURIResolver, final SchemaFactory schemaFactory,
            final ResolvingConfigurationStrategy resolvingConfigurationStrategy, final SchematronCompilerRegistry compilerRegistry) {
        this.processor = processor;
        this.repository = repository;
        this.resolver = resolver;
        this.unparsedTextURIResolver = unparsedTextURIResolver;
        this.schemaFactory = schemaFactory;
        this.resolvingConfigurationStrategy = resolvingConfigurationStrategy;
        this.compilerRegistry = compilerRegistry;
    }

    public Processor getProcessor() {
        return this.processor;
    }

    public ResolvingConfigurationStrategy getResolvingConfigurationStrategy() {
        return this.resolvingConfigurationStrategy;
    }
}
