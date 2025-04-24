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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import de.kosit.validationtool.impl.xml.RelativeUriResolver;
import org.junit.jupiter.api.Test;

/**
 * Testet den Uri-Resolver der relative auflösen soll
 *
 * @author Andreas Penski
 */
class RelativeUriResolverTest {

    private static final URI BASE;

    static {
        try {
            BASE = Objects.requireNonNull(RelativeUriResolver.class.getResource("/examples/assertions/")).toURI();
        } catch (final URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private URIResolver resolver = new RelativeUriResolver(BASE);

    @Test
    void success() throws TransformerException {
        final Source resource = this.resolver.resolve("ubl-0001.xml", BASE.toASCIIString());
        assertThat(resource).isNotNull();
    }

    @Test
    void notExisting() {
        assertThrows(TransformerException.class, () -> this.resolver.resolve("ubl-0001", BASE.toASCIIString()));
    }

    @Test
    void outOfPath() {
        assertThrows(TransformerException.class, () -> this.resolver.resolve("../results/report.xml", BASE.toASCIIString()));
    }

    @Test
    void classpathLocal() throws URISyntaxException, TransformerException {
        this.resolver = new RelativeUriResolver(
                Objects.requireNonNull(RelativeUriResolver.class.getClassLoader().getResource("loading")).toURI());
        final URL moz = RelativeUriResolverTest.class.getClassLoader().getResource("loading/main.xsd");
        assert moz != null;
        final Source resolved = this.resolver.resolve("./resources/reference.xsd", moz.toURI().toASCIIString());
        assertThat(resolved).isNotNull();
    }

    @Test
    void classpathJAR() throws URISyntaxException, TransformerException {
        this.resolver = new RelativeUriResolver(
                Objects.requireNonNull(RelativeUriResolver.class.getClassLoader().getResource("packaged")).toURI());
        final URL moz = RelativeUriResolverTest.class.getClassLoader().getResource("packaged/main.xsd");
        assert moz != null;
        final Source resolved = this.resolver.resolve("./resources/reference.xsd", moz.toURI().toASCIIString());
        assertThat(resolved).isNotNull();
    }
}
