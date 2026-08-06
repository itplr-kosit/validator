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

package de.kosit.validationtool.impl.xml;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.Locale;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.StandardUnparsedTextResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.trans.XPathException;

/**
 * {@link URIResolver} that allows resolving local artifacts only. Any uri scheme designating a remote location (e.g.
 * http(s)) is rejected.
 *
 * @author Stefan Grönke
 * @since 1.6.3
 */
public class LocalUriResolver implements URIResolver, UnparsedTextURIResolver {

    private static final Set<String> LOCAL_SCHEMES = Set.of("file", "jar");

    @Override
    public Source resolve(final String href, final String base) throws TransformerException {
        final URI resolved = RelativeUriResolver.resolve(URI.create(href), URI.create(base));
        if (isLocal(resolved)) {
            try {
                return new StreamSource(resolved.toURL().openStream(), resolved.toASCIIString());
            } catch (final IOException e) {
                throw new TransformerException(String.format("Can not resolve required  %s", href), e);
            }
        }
        throw new TransformerException(String.format("Only local artifacts can be resolved. %s resolves to %s", href, resolved));
    }

    @Override
    public Reader resolve(final URI absoluteURI, final String encoding, final Configuration config) throws XPathException {
        if (isLocal(absoluteURI)) {
            return new StandardUnparsedTextResolver().resolve(absoluteURI, encoding, config);
        }
        throw new XPathException(String.format("Only local artifacts can be resolved. Can not resolve %s", absoluteURI));
    }

    private static boolean isLocal(final URI uri) {
        return uri.getScheme() != null && LOCAL_SCHEMES.contains(uri.getScheme().toLowerCase(Locale.ROOT));
    }

}
