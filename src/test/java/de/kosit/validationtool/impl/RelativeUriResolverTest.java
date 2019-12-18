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

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private URIResolver resolver = new RelativeUriResolver(BASE);

    @Test
    public void testSucces() throws TransformerException {
        final Source resource = this.resolver.resolve("ubl-0001.xml", BASE.toASCIIString());
        assertThat(resource).isNotNull();
    }

    @Test
    public void testNotExisting() throws TransformerException {
        this.exception.expect(IllegalStateException.class);
        this.resolver.resolve("ubl-0001", BASE.toASCIIString());
    }

    @Test
    public void testOutOfPath() throws TransformerException {
        this.exception.expect(IllegalStateException.class);
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
}
