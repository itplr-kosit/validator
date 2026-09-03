package org.kosit.validator.xml.resolve;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.kosit.base.uri.UriHelper;

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
     * whether artifacts within an archive base uri may be resolved
     */
    private final boolean resolveInArchive;

    /**
     * Resolves a relative uri, but not within an archive base uri - see {@link #resolve(URI, URI, boolean)}.
     *
     * @param href the uri to resolve
     * @param base the base uri
     * @return the resolved uri
     */
    public static URI resolve(final URI href, final URI base) {
        return resolve(href, base, false);
    }

    /**
     * Resolves a relative uri, optionally including uris within a jar file.
     *
     * @param href the uri to resolve
     * @param base the base uri
     * @param resolveInArchive <code>true</code> to resolve within an archive base uri like
     *            <code>jar:file:/some.jar!/repository/</code>, e.g. because the scenario configuration is shipped as a
     *            jar. Off by default, see {@link UriHelper#resolve(URI, URI, boolean)}
     * @return the resolved uri
     */
    public static URI resolve(final URI href, final URI base, final boolean resolveInArchive) {
        return UriHelper.resolve(base, href, resolveInArchive);
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
     * Creates a new {@code RelativeUriResolver} instance that does not resolve within an archive base uri.
     *
     * @param baseUri the base uri
     */
    public RelativeUriResolver(final URI baseUri) {
        this(baseUri, false);
    }

    /**
     * Creates a new {@code RelativeUriResolver} instance.
     *
     * @param baseUri the base uri
     * @param resolveInArchive <code>true</code> to resolve within an archive base uri, see
     *            {@link #resolve(URI, URI, boolean)}
     */
    public RelativeUriResolver(final URI baseUri, final boolean resolveInArchive) {
        this.baseUri = baseUri;
        this.resolveInArchive = resolveInArchive;
    }

    @Override
    public Source resolve(final String href, final String base) throws TransformerException {
        final URI resolved = resolve(URI.create(href), URI.create(base), this.resolveInArchive);
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
