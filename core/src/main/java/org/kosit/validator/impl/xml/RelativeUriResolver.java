package org.kosit.validator.impl.xml;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.lib.StandardUnparsedTextResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.trans.XPathException;

/**
 * {@link URIResolver} that resolves artifacts relative to a given base uri. The resolved URI must be resolving as child
 * e.g. the baseUri must be a parent of the resolved artifact.
 *
 * @author Andreas Penski
 */
public class RelativeUriResolver implements URIResolver, UnparsedTextURIResolver, ResourceResolver {

    /**
     * the base uri
     */
    private final URI baseUri;

    /**
     * Resolves a relative uri including uris within a jar file.
     *
     * @param href the uri to resolve
     * @param base the base uri
     * @return the resolved uri
     */
    public static URI resolve(final URI href, final URI base) {
        final boolean jarURI = isJarURI(base);
        final URI tmpBase = jarURI ? URI.create(base.toASCIIString().substring(4)) : base;
        final URI result = tmpBase.resolve(href);
        return jarURI ? URI.create("jar:" + result.toString()) : result;
    }

    static boolean isJarURI(final URI uri) {
        return uri.isOpaque() && uri.getScheme().equals("jar");
    }

    private static boolean isUnderBaseUri(final URI resolved, final URI baseUri) {
        if (resolved == null || baseUri == null) {
            return false;
        }
        final String base = baseUri.toASCIIString().replaceAll("file:/+", "");
        final String r = resolved.toASCIIString().replaceAll("file:/+", "");
        return r.startsWith(base);
    }

    /**
     * Creates a new {@code RelativeUriResolver} instance.
     *
     * @param baseUri the base uri
     */
    public RelativeUriResolver(final URI baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public Source resolve(final String href, final String base) throws TransformerException {
        final URI resolved = resolve(URI.create(href), URI.create(base));
        if (isUnderBaseUri(resolved, this.baseUri)) {
            try {
                return new StreamSource(resolved.toURL().openStream(), resolved.toASCIIString());
            } catch (final IOException e) {
                throw new TransformerException("Can not resolve required '" + href + "'", e);
            }
        }
        throw new TransformerException(
                "The resolved transformation artifact " + resolved + " is not within the configured repository " + this.baseUri);
    }

    // from UnparsedTextURIResolver
    @Override
    public Reader resolve(final URI absoluteURI, final String encoding, final Configuration config) throws XPathException {
        if (isUnderBaseUri(absoluteURI, this.baseUri)) {
            return new StandardUnparsedTextResolver().resolve(absoluteURI, encoding, config);
        }
        throw new XPathException(
                "The resolved transformation artifact " + absoluteURI + " is not within the configured repository " + this.baseUri);
    }

    @Override
    public Source resolve(final ResourceRequest request) throws XPathException {
        try {
            return resolve(request.relativeUri, request.baseUri);
        } catch (final TransformerException e) {
            throw new XPathException(e);
        }
    }
}
