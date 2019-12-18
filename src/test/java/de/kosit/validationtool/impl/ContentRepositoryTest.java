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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.kosit.validationtool.impl.Helper.Simple;

import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Testet das ContentRepository.
 * 
 * @author Andreas Penski
 */
public class ContentRepositoryTest {

    private ContentRepository repository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        this.repository = new ContentRepository(ObjectFactory.createProcessor(), Simple.REPOSITORY);
    }

    @Test
    public void testCreateSchema() throws MalformedURLException {
        final Schema schema = ContentRepository.createSchema(Helper.ASSERTION_SCHEMA.toURL());
        assertThat(schema).isNotNull();
    }

    @Test
    public void testSchemaCaching() {
        final Schema schema = this.repository.getReportInputSchema();
        assertThat(this.repository.getReportInputSchema()).isSameAs(schema);
    }

    @Test
    public void testCreateSchemaNotExisting() throws Exception {
        this.exception.expect(IllegalStateException.class);
        ContentRepository.createSchema(Simple.NOT_EXISTING.toURL());
    }

    @Test
    public void testLoadXSLT() {
        final XsltExecutable executable = this.repository.loadXsltScript(Simple.REPORT_XSL);
        assertThat(executable).isNotNull();
    }

    @Test
    public void testLoadXSLTNotExisting() {
        this.exception.expect(IllegalStateException.class);
        this.repository.loadXsltScript(Simple.NOT_EXISTING);
    }

    @Test
    public void testXpathCreation() {
        XPathExecutable xPath = this.repository.createXPath("//html", null);
        assertThat(xPath).isNotNull();
        xPath = this.repository.createXPath("//html", Collections.emptyMap());
        assertThat(xPath).isNotNull();
        final Map<String, String> namespace = new HashMap<>();
        namespace.put("html", "http://www.w3.org/1999/xhtml");
        xPath = this.repository.createXPath("//html:html", namespace);
        assertThat(xPath).isNotNull();
    }

    @Test
    public void testXpathCreationWithoutNamespace() {
        this.exception.expect(IllegalStateException.class);
        this.repository.createXPath("//html:html", null);
    }

    @Test
    public void testIllegalXpath() {
        this.exception.expect(IllegalStateException.class);
        this.repository.createXPath("kein Xpath Ausdruck", null);
    }

    @Test
    public void loadFromJar() throws URISyntaxException {
        this.repository = new ContentRepository(ObjectFactory.createProcessor(), Helper.JAR_REPOSITORY.toURI());
        final XsltExecutable xsltExecutable = this.repository.loadXsltScript(URI.create("resources/eRechnung/report.xsl"));
        assertThat(xsltExecutable).isNotNull();
    }

    @Test
    public void testLoadSchema() {
        final URL main = RelativeUriResolverTest.class.getClassLoader().getResource("loading/main.xsd");
        final Schema schema = ContentRepository.createSchema(main, new ClassPathResourceResolver("/loading"));
        assertThat(schema).isNotNull();
    }

    @Test
    public void testLoadSchemaPackaged() throws URISyntaxException {
        final URL main = RelativeUriResolverTest.class.getClassLoader().getResource("packaged/main.xsd");
        final Schema schema = ContentRepository.createSchema(main,
                new ClassPathResourceResolver(RelativeUriResolverTest.class.getClassLoader().getResource("packaged/").toURI()));
        assertThat(schema).isNotNull();
    }

}
