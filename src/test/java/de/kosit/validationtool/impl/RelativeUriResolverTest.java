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

package de.kosit.validationtool.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.junit.Test;

import net.sf.saxon.trans.XPathException;

import de.kosit.validationtool.impl.xml.RelativeUriResolver;

/**
 * Testet den Uri-Resolver der relative auflösen soll
 * 
 * @author Andreas Penski
 */
public class RelativeUriResolverTest {

    private static final URI BASE;

    static {
        try {
            BASE = RelativeUriResolver.class.getResource("/examples/assertions/").toURI();
        } catch (final URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private URIResolver resolver = new RelativeUriResolver(BASE);

    @Test
    public void testSuccess() throws TransformerException {
        final Source resource = this.resolver.resolve("ubl-0001.xml", BASE.toASCIIString());
        assertThat(resource).isNotNull();
    }

    @Test(expected = TransformerException.class)
    public void testNotExisting() throws TransformerException {
        this.resolver.resolve("ubl-0001", BASE.toASCIIString());
    }

    @Test(expected = TransformerException.class)
    public void testOutOfPath() throws TransformerException {
        this.resolver.resolve("../results/report.xml", BASE.toASCIIString());
    }

    @Test
    public void testClasspathLocal() throws URISyntaxException, TransformerException {
        this.resolver = new RelativeUriResolver(RelativeUriResolver.class.getClassLoader().getResource("loading").toURI());
        final URL moz = RelativeUriResolverTest.class.getClassLoader().getResource("loading/main.xsd");
        final Source resolved = this.resolver.resolve("./resources/reference.xsd", moz.toURI().toASCIIString());
        assertThat(resolved).isNotNull();
    }

    @Test
    public void testClasspathJAR() throws URISyntaxException, TransformerException {
        this.resolver = new RelativeUriResolver(RelativeUriResolver.class.getClassLoader().getResource("packaged").toURI());
        final URL moz = RelativeUriResolverTest.class.getClassLoader().getResource("packaged/main.xsd");
        final Source resolved = this.resolver.resolve("./resources/reference.xsd", moz.toURI().toASCIIString());
        assertThat(resolved).isNotNull();
    }

    @Test
    public void testPercentEncodedTraversalRejected() {
        // %2e%2e survives URI.resolve() undecoded but is decoded to '..' when the file is opened.
        assertThatThrownBy(() -> this.resolver.resolve("x/%2e%2e/%2e%2e/simple/scenarios.xml", BASE.toASCIIString()))
                .isInstanceOf(TransformerException.class).hasMessageContaining("not within the configured repository");
    }

    @Test
    public void testPercentEncodedTraversalRejectedForUnparsedText() throws URISyntaxException {
        final RelativeUriResolver r = new RelativeUriResolver(BASE);
        final URI escaped = new URI(BASE.toASCIIString() + "x/%2e%2e/%2e%2e/simple/scenarios.xml");
        assertThatThrownBy(() -> r.resolve(escaped, "UTF-8", null)).isInstanceOf(XPathException.class)
                .hasMessageContaining("not within the configured repository");
    }

    @Test
    public void testSiblingDirectoryWithCommonPrefixRejected() throws URISyntaxException {
        // a base uri without trailing slash must not match sibling directories sharing a name prefix
        final URI baseWithoutSlash = new URI(BASE.toASCIIString().replaceFirst("/$", ""));
        final URIResolver r = new RelativeUriResolver(baseWithoutSlash);
        assertThatThrownBy(() -> r.resolve("../assertions-sibling/file.xml", baseWithoutSlash.toASCIIString()))
                .isInstanceOf(TransformerException.class).hasMessageContaining("not within the configured repository");
    }

    @Test
    public void testDifferentSchemeRejected() {
        assertThatThrownBy(() -> this.resolver.resolve("http://example.org/evil.xsl", BASE.toASCIIString()))
                .isInstanceOf(TransformerException.class).hasMessageContaining("not within the configured repository");
    }
}
