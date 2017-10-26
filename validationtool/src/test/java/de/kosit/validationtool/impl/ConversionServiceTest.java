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

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.kosit.validationtool.model.scenarios.Scenarios;

/**
 * Simple test for testing the jaxb conversion service.
 * 
 * @author apenski
 */
public class ConversionServiceTest {

    private static final URL VALID_XML = ConversionServiceTest.class.getResource("/valid/scenarios.xml");

    private static final URL INVALID_XML = ConversionServiceTest.class.getResource("/invalid/scenarios-invalid.xml");

    private static final URL ILLFORMED_XML = ConversionServiceTest.class.getResource("/invalid/scenarios-illformed.xml");

    private static final URL SCHEMA = ConversionServiceTest.class.getResource("/xsd/scenarios.xsd");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ConversionService service;

    private ContentRepository repository;

    @Before
    public void setup() {
        service = new ConversionService();
        repository = new ContentRepository(ObjectFactory.createProcessor(), new File("src/test/resources/examples/repository").toURI());
    }

    @Test
    public void testMarshalNull() {
        exception.expect(ConversionService.ConversionExeption.class);
        service.writeXml(null);
    }

    @Test
    public void testMarshalUnknown() {
        exception.expect(ConversionService.ConversionExeption.class);
        service.writeXml(new Serializable() {
        });
    }

    @Test
    public void testUnmarshal() throws URISyntaxException {
        final Scenarios s = service.readXml(VALID_XML.toURI(), Scenarios.class);
        assertThat(s).isNotNull();
        assertThat(s.getName()).isEqualToIgnoringCase("XInneres");
    }

    @Test
    public void testUnmarshalWithSchema() throws URISyntaxException {
        final Scenarios s = service.readXml(VALID_XML.toURI(), Scenarios.class, repository.createSchema(SCHEMA));
        assertThat(s).isNotNull();
        assertThat(s.getName()).isEqualToIgnoringCase("XInneres");
    }

    @Test
    public void testUnmarshalInvalidXml() throws URISyntaxException {
        exception.expect(ConversionService.ConversionExeption.class);
        service.readXml(INVALID_XML.toURI(), Scenarios.class, repository.createSchema(SCHEMA));
    }

    @Test
    public void testUnmarshalIllFormed() throws URISyntaxException {
        exception.expect(ConversionService.ConversionExeption.class);
        service.readXml(ILLFORMED_XML.toURI(), Scenarios.class, repository.createSchema(SCHEMA));
    }

    @Test
    public void testUnmarshalEmpty() {
        exception.expect(ConversionService.ConversionExeption.class);
        service.readXml(null, Scenarios.class);
    }

    @Test
    public void testUnmarshalUnknownType() throws URISyntaxException {
        exception.expect(ConversionService.ConversionExeption.class);
        service.readXml(VALID_XML.toURI(), ConversionService.class);
    }

    @Test
    public void testUnmarshalWithoutType() throws URISyntaxException {
        exception.expect(ConversionService.ConversionExeption.class);
        service.readXml(VALID_XML.toURI(), null);
    }

}
