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

import org.junit.Test;

import net.sf.saxon.trans.XPathException;

import de.kosit.validationtool.impl.xml.LocalUriResolver;

/**
 * Tests {@link LocalUriResolver}.
 */
public class LocalUriResolverTest {

    private static final URI BASE;

    static {
        try {
            BASE = LocalUriResolver.class.getResource("/examples/assertions/").toURI();
        } catch (final URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private final LocalUriResolver resolver = new LocalUriResolver();

    @Test
    public void testResolveLocalRelative() throws TransformerException {
        final Source resource = this.resolver.resolve("ubl-0001.xml", BASE.toASCIIString());
        assertThat(resource).isNotNull();
    }

    @Test
    public void testResolveLocalAbsolute() throws TransformerException {
        final Source resource = this.resolver.resolve(BASE.toASCIIString() + "ubl-0001.xml", BASE.toASCIIString());
        assertThat(resource).isNotNull();
    }

    @Test
    public void testRemoteHrefRejected() {
        assertThatThrownBy(() -> this.resolver.resolve("http://example.org/evil.xsl", BASE.toASCIIString()))
                .isInstanceOf(TransformerException.class).hasMessageContaining("Only local artifacts");
    }

    @Test
    public void testRemoteUnparsedTextRejected() throws URISyntaxException {
        assertThatThrownBy(() -> this.resolver.resolve(new URI("http://example.org/evil.txt"), "UTF-8", null))
                .isInstanceOf(XPathException.class).hasMessageContaining("Only local artifacts");
    }

    @Test
    public void testClasspathJAR() throws URISyntaxException, TransformerException {
        final URL main = LocalUriResolverTest.class.getClassLoader().getResource("packaged/main.xsd");
        final Source resolved = this.resolver.resolve("./resources/reference.xsd", main.toURI().toASCIIString());
        assertThat(resolved).isNotNull();
    }
}
