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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
        repository = new ContentRepository(ObjectFactory.createProcessor(), Helper.REPOSITORY);
    }


    @Test
    public void testCreateSchema() throws MalformedURLException {
        final Schema schema = repository.createSchema(Helper.ASSERTION_SCHEMA.toURL());
        assertThat(schema).isNotNull();
    }

    @Test
    public void testSchemaCaching() throws MalformedURLException {
        final Schema schema = repository.getReportInputSchema();
        assertThat(repository.getReportInputSchema()).isSameAs(schema);
    }

    @Test
    public void testCreateSchemaNotExisting()throws Exception {
        exception.expect(IllegalStateException.class);
        repository.createSchema(Helper.ASSERTION_SCHEMA.resolve("noexisting").toURL());
    }

    @Test
    public void testLoadXSLT() throws MalformedURLException {
        final XsltExecutable executable = repository.loadXsltScript(Helper.SAMPLE_XSLT);
        assertThat(executable).isNotNull();
    }

    @Test
    public void testLoadXSLTNotExisting() throws MalformedURLException {
        exception.expect(IllegalStateException.class);
        repository.loadXsltScript(Helper.SAMPLE_XSLT.resolve("notexisting"));
    }

    @Test
    public void testXpathCreation() throws MalformedURLException {
        XPathExecutable xPath =  repository.createXPath("//html", null);
        assertThat(xPath).isNotNull();
        xPath = repository.createXPath("//html", Collections.emptyMap());
        assertThat(xPath).isNotNull();
        Map<String,String> namespace = new HashMap<>();
        namespace.put("html", "http://www.w3.org/1999/xhtml");
        xPath =  repository.createXPath("//html:html", namespace );
        assertThat(xPath).isNotNull();
    }

    @Test
    public void testXpathCreationWithoutNamespace(){
        exception.expect(IllegalStateException.class);
        repository.createXPath("//html:html", null );
    }

    @Test
    public void testIllegalXpath(){
        exception.expect(IllegalStateException.class);
        repository.createXPath("kein Xpath Ausdruck", null );
    }
}
