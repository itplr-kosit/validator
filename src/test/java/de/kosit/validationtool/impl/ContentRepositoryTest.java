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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.impl.Helper.Simple;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Testet das repository.
 * 
 * @author Andreas Penski
 */
public class ContentRepositoryTest {

    private ContentRepository repository;

    @Before
    public void setup() {
        this.repository = Simple.createContentRepository();
    }

    @Test
    public void testCreateSchema() throws MalformedURLException {
        final Schema schema = this.repository.createSchema(Helper.ASSERTION_SCHEMA.toURL());
        assertThat(schema).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSchemaNotExisting() throws Exception {
        this.repository.createSchema(Simple.NOT_EXISTING.toURL());
    }

    @Test
    public void testLoadXSLT() {
        final XsltExecutable executable = this.repository.loadXsltScript(Simple.REPORT_XSL);
        assertThat(executable).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void testLoadXSLTNotExisting() {
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

    @Test(expected = IllegalStateException.class)
    public void testXpathCreationWithoutNamespace() {
        this.repository.createXPath("//html:html", null);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalXpath() {
        this.repository.createXPath("kein Xpath Ausdruck", null);
    }

    @Test
    public void loadFromJar() throws URISyntaxException {
        assert Helper.JAR_REPOSITORY != null;
        this.repository = new ContentRepository(Helper.getTestProcessor(), ResolvingMode.STRICT_RELATIVE.getStrategy(),
                Helper.JAR_REPOSITORY.toURI());
        final XsltExecutable xsltExecutable = this.repository.loadXsltScript(URI.create("report.xsl"));
        assertThat(xsltExecutable).isNotNull();
        final Schema schema = this.repository.createSchema(URI.create("main.xsd"));
        assertThat(schema).isNotNull();
    }

}
