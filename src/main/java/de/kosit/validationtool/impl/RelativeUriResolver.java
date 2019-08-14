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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.trans.XPathException;

/**
 * {@link URIResolver} that resolves artifacts relative to a given base uri. The resolved URI must be resolving as child
 * e.g. the baseUri must be a parent of the resolved artifact.
 *
 * @author Andreas Penski
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RelativeUriResolver implements URIResolver, UnparsedTextURIResolver {

    /** the base uri */
    private final URI baseUri;

    @Override
    public Source resolve(final String href, final String base) {
        final URI resolved = resolve(URI.create(href), URI.create(base));
        if (isUnderBaseUri(resolved)) {
            try {
                return new StreamSource(resolved.toURL().openStream(), resolved.toASCIIString());
            } catch (final IOException e) {

                throw new IllegalStateException(String.format("Can not resolve required  %s", href), e);
            }
        } else {
            throw new IllegalStateException(String
                    .format("The resolved transformation artifact %s is not within the configured repository %s", resolved, this.baseUri));
        }
    }

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

    private boolean isUnderBaseUri(final URI resolved) {
        final String base = this.baseUri.toASCIIString().replaceAll("file:/+", "");
        final String r = resolved.toASCIIString().replaceAll("file:/+", "");
        return r.startsWith(base);
    }

    @Override
    public Reader resolve(final URI absoluteURI, final String encoding, final Configuration config) throws XPathException {
        if (isUnderBaseUri(absoluteURI)) {
            try {
                return new InputStreamReader(absoluteURI.toURL().openStream(), encoding);
            } catch (final IOException e) {
                throw new IllegalStateException(String.format("Can not resolve required  %s", absoluteURI), e);
            }
        } else {
            throw new IllegalStateException(String.format(
                    "The resolved transformation artifact %s is not within the configured repository %s", absoluteURI, this.baseUri));
        }
    }
}