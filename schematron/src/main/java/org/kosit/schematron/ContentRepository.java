package org.kosit.schematron;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.kosit.base.string.StringHelper;
import org.kosit.base.xml.SchemaResolver;
import org.kosit.schematron.resolve.ResolvingConfigurationStrategy;
import org.kosit.schematron.resolve.RelativeUriResolver;
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
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof final ContentRepository.CacheKey other))
                return false;
            final String this$compilerId = this.getCompilerId();
            final String other$compilerId = other.getCompilerId();
            if (this$compilerId == null ? other$compilerId != null : !this$compilerId.equals(other$compilerId))
                return false;
            final URI this$uri = this.getUri();
            final URI other$uri = other.getUri();
            if (this$uri == null ? other$uri != null : !this$uri.equals(other$uri))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final String $compilerId = this.getCompilerId();
            result = result * PRIME + ($compilerId == null ? 43 : $compilerId.hashCode());
            final URI $uri = this.getUri();
            result = result * PRIME + ($uri == null ? 43 : $uri.hashCode());
            return result;
        }

        @Override
        public String toString() {
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
        this.resolver = strategy.createResourceResolver(repository);
        this.unparsedTextURIResolver = this.resolvingConfigurationStrategy.createUnparsedTextURIResolver(repository);
        this.schemaFactory = this.resolvingConfigurationStrategy.createSchemaFactory();
        this.compilerRegistry = defaultSchematronCompilerRegistry(processor);
    }

    private static SchematronCompilerRegistry defaultSchematronCompilerRegistry(final Processor processor) {
        return new SchematronCompilerRegistry(List.of(new SchXsltCompiler(), new SchXslt2Compiler(), new IsoSchematronCompiler(processor)));
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
        final SchematronCompiler compiler = compilerRegistry.get(compilerId);
        final CacheKey key = new CacheKey(compilerId, schUri);
        final Source xsltSource = schematronXsltCache.computeIfAbsent(key, k -> compiler.compileToXslt(schUri, this::resolveInRepository));
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
        return createSchema(new Source[] { SchemaResolver.resolve(url) });
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
                    + (StringHelper.isNotBlank(expression) ? expression : "EMPTY EXPRESSION") + "'", e);
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
