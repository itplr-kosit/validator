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
import org.kosit.schematron.resolve.RelativeUriResolver;
import org.kosit.schematron.resolve.ResolvingConfigurationStrategy;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentRepository.class);

    private static final record CacheKey(String compilerId, URI uri) {
    }

    private final Processor processor;

    private final URI repository;

    private final ResourceResolver resolver;

    private final UnparsedTextURIResolver unparsedTextURIResolver;

    private final SchemaFactory schemaFactory;

    private final ResolvingConfigurationStrategy resolvingConfigurationStrategy;

    private final Map<CacheKey, Source> schematronXsltCache = new ConcurrentHashMap<>();

    private final SchematronCompilerRegistry compilerRegistry;

    private static SchematronCompilerRegistry defaultSchematronCompilerRegistry(final Processor processor) {
        return new SchematronCompilerRegistry(List.of(new SchXsltCompiler(), new SchXslt2Compiler(), new IsoSchematronCompiler(processor)));
    }

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
                strategy.createSchemaFactory(), strategy, defaultSchematronCompilerRegistry(processor));
    }

    protected ContentRepository(final Processor processor, final URI repository, final ResourceResolver resolver,
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

    public final Processor getProcessor() {
        return this.processor;
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

    public Schema createSchema(final @NonNull Source @NonNull [] schemaSources) {
        Objects.requireNonNull(schemaSources);

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
        LOGGER.info("Loading or compiling Schematron {} using compiler {}", schUri, compilerId);

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

    public Schema createSchema(final @NonNull URI uri) {
        final var resolved = resolveInRepository(uri);
        if (resolved == null)
            throw new IllegalStateException("Failed to resolve URI " + uri);

        return createSchema(new Source[] { resolved });
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
