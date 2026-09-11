package org.kosit.schematron;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.string.StringHelper;
import org.kosit.schematron.compiler.SchematronCompiler;
import org.kosit.schematron.resolve.RelativeUriResolver;
import org.kosit.schematron.resolve.ResolvingConfigurationStrategy;
import org.kost.validator.api.xml.CollectingErrorEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.lib.ResourceResolver;
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

    private static final record CacheKey(String compilerId, URI uri) {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentRepository.class);

    private final Processor processor;

    private final URI repository;

    private final ResourceResolver resolver;

    private final UnparsedTextURIResolver unparsedTextURIResolver;

    private final ResolvingConfigurationStrategy resolvingConfigurationStrategy;

    private final Map<CacheKey, Source> schematronXsltCache = new ConcurrentHashMap<>();

    /**
     * Compiled XML Schemas of this repository, by the artifact URIs they were built from.
     * <p>
     * A {@link Schema} is immutable and thread-safe by the JAXP contract, so one compilation per URI set serves every
     * document and every thread. Without it each run recompiled the schema from disk, which for a set like UBL or CII
     * is the most expensive thing the pipeline does.
     * </p>
     */
    private final Map<List<String>, Schema> schemaCache = new ConcurrentHashMap<>();

    private final SchematronCompilerRegistry compilerRegistry;

    /**
     * Creates a new {@link ContentRepository} based on configured security and resolving strategy and the specified
     * repository location.
     *
     * @param processor Saxon processor to use
     * @param strategy the security and resolving strategy
     * @param repository the repository.
     */
    public ContentRepository(final Processor processor, final ResolvingConfigurationStrategy strategy, final URI repository) {
        this(processor, repository, strategy.createResourceResolver(repository), strategy.createUnparsedTextURIResolver(repository),
                strategy, SchematronCompilerRegistry.defaultSchematronCompilerRegistry(processor));
    }

    protected ContentRepository(final Processor processor, final URI repository, final ResourceResolver resolver,
            final UnparsedTextURIResolver unparsedTextURIResolver, final ResolvingConfigurationStrategy resolvingConfigurationStrategy,
            final SchematronCompilerRegistry compilerRegistry) {
        this.processor = processor;
        this.repository = repository;
        this.resolver = resolver;
        this.unparsedTextURIResolver = unparsedTextURIResolver;
        this.resolvingConfigurationStrategy = resolvingConfigurationStrategy;
        this.compilerRegistry = compilerRegistry;
    }

    public final Processor getProcessor() {
        return this.processor;
    }

    /**
     * @return the base URI this repository resolves artifacts against. Never {@code null}.
     */
    public final URI getRepository() {
        return this.repository;
    }

    /**
     * Returns the {@link URIResolver} to use for resolving xml artifacts.
     *
     * @return the resolver
     */
    public final ResourceResolver getResolver() {
        return this.resolver;
    }

    public final UnparsedTextURIResolver getUnparsedTextURIResolver() {
        return this.unparsedTextURIResolver;
    }

    public final ResolvingConfigurationStrategy getResolvingConfigurationStrategy() {
        return this.resolvingConfigurationStrategy;
    }

    /**
     * Compiles the given sources into a schema. Not cached - the sources may be streams, which only read once.
     *
     * @param schemaSources the schema documents
     * @return the compiled schema
     */
    public Schema createSchema(final @NonNull Source @NonNull [] schemaSources) {
        Objects.requireNonNull(schemaSources);

        // a SchemaFactory is not thread-safe by its own contract, and one repository serves every request thread of a
        // server, so each compilation gets its own. The strategy hands out a fresh, hardened factory per call
        final SchemaFactory factory = this.resolvingConfigurationStrategy.createSchemaFactory();
        try {
            return factory.newSchema(schemaSources);
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
        final XsltCompiler xsltCompiler = processor.newXsltCompiler();
        final CollectingErrorEventHandler listener = new CollectingErrorEventHandler();
        try {
            xsltCompiler.setErrorListener(listener);
            if (resolver != null) {
                // otherwise use default resolver
                xsltCompiler.setResourceResolver(resolver);
            }
            return xsltCompiler.compile(resolveInRepository(uri));
        } catch (final SaxonApiException e) {
            listener.getErrors().forEach(event -> event.log(LOGGER));
            throw new IllegalStateException("Can not compile xslt executable for uri " + uri, e);
        } finally {
            if (!listener.hasErrors() && listener.hasEvents()) {
                LOGGER.warn("Received warnings or errors while loading a xslt script " + uri);
                listener.getErrors().forEach(e -> e.log(LOGGER));
            }
        }
    }

    public XsltExecutable loadSchematronXslt(final String compilerId, final URI schUri) {
        LOGGER.info("Loading or compiling Schematron " + schUri + " using compiler " + compilerId);

        final SchematronCompiler compiler = compilerRegistry.get(compilerId);
        if (compiler == null)
            throw new IllegalStateException("Failed to resolve Schematron compiler with ID '" + compilerId + "'");

        final CacheKey key = new CacheKey(compilerId, schUri);
        final Source xsltSource = schematronXsltCache.computeIfAbsent(key, _ -> compiler.compileToXslt(schUri, this::resolveInRepository));
        final XsltCompiler xsltCompiler = processor.newXsltCompiler();
        try {
            return xsltCompiler.compile(xsltSource);
        } catch (final SaxonApiException e) {
            throw new IllegalStateException("Can not compile xslt executable for uri " + schUri, e);
        }
    }

    /**
     * Creates the schema of the given artifact, resolved in this repository. Compiled once and then served from the
     * cache.
     *
     * @param uri the URI of the schema artifact
     * @return the compiled schema
     */
    public Schema createSchema(final @NonNull URI uri) {
        return cachedSchema(List.of(uri.toString()));
    }

    /**
     * Creates a schema based on the given URIs. Compiled once per URI set and then served from the cache.
     *
     * @param uris the uris in string representation
     * @return the schema
     */
    public Schema createSchema(final Collection<String> uris) {
        return cachedSchema(List.copyOf(uris));
    }

    private Schema cachedSchema(final List<String> uris) {
        // a failed compilation throws out of the mapping function and is not remembered, so a repaired artifact is
        // picked up on the next run
        return this.schemaCache.computeIfAbsent(uris, key -> createSchema(key.stream().map(this::resolveRequired).toArray(Source[]::new)));
    }

    private Source resolveRequired(final String uri) {
        final Source resolved = resolveInRepository(URI.create(uri));
        if (resolved == null) {
            throw new IllegalStateException("Failed to resolve URI " + uri);
        }
        return resolved;
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
            return this.resolver.resolve(r);
        } catch (final TransformerException e) {
            LOGGER.error("Error resolving source " + source, e);
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
    public XPathExecutable createXPath(final String expression, @Nullable final Map<String, String> namespaces) {
        try {
            final XPathCompiler compiler = processor.newXPathCompiler();
            if (namespaces != null)
                for (final var e : namespaces.entrySet())
                    compiler.declareNamespace(e.getKey(), e.getValue());

            return compiler.compile(expression);
        } catch (final SaxonApiException e) {
            throw new IllegalStateException("Can not compile xpath match expression '"
                    + (StringHelper.isNotBlank(expression) ? expression : "EMPTY EXPRESSION") + "'", e);
        }
    }
}
