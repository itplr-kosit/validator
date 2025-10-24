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

import java.io.Serializable;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.impl.Helper.Invalid;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.model.scenarios.Scenarios;

/**
 * Simple test for testing the jaxb conversion service.
 * 
 * @author apenski
 */
public class ConversionServiceTest {

    private static final URL SCHEMA = ConversionServiceTest.class.getResource("/xsd/scenarios.xsd");

    private ConversionService service;

    private ContentRepository repository;

    @Before
    public void setup() {
        this.service = new ConversionService();
        this.repository = Simple.createContentRepository();
    }

    @Test(expected = ConversionService.ConversionExeption.class)
    public void testMarshalNull() {
        this.service.writeXml(null);
    }

    @Test(expected = ConversionService.ConversionExeption.class)
    public void testMarshalUnknown() {
        this.service.writeXml(new Serializable() {
        });
    }

    @Test
    public void testUnmarshal() {
        final Scenarios s = this.service.readXml(Simple.SCENARIOS, Scenarios.class);
        assertThat(s).isNotNull();
        assertThat(s.getName()).isEqualToIgnoringCase("HTML-TestSuite");
    }

    @Test
    public void testUnmarshalWithSchema() {
        final Scenarios s = this.service.readXml(Simple.SCENARIOS, Scenarios.class, this.repository.createSchema(SCHEMA));
        assertThat(s).isNotNull();
        assertThat(s.getName()).isEqualToIgnoringCase("HTML-TestSuite");
    }

    @Test(expected = ConversionService.ConversionExeption.class)
    public void testUnmarshalInvalidXml() {
        this.service.readXml(Invalid.SCENARIOS, Scenarios.class, this.repository.createSchema(SCHEMA));
    }

    @Test(expected = ConversionService.ConversionExeption.class)
    public void testUnmarshalIllFormed() {
        this.service.readXml(Invalid.SCENARIOS_ILLFORMED, Scenarios.class, this.repository.createSchema(SCHEMA));
    }

    @Test(expected = ConversionService.ConversionExeption.class)
    public void testUnmarshalEmpty() {
        this.service.readXml(null, Scenarios.class);
    }

    @Test(expected = ConversionService.ConversionExeption.class)
    public void testUnmarshalUnknownType() {
        this.service.readXml(Simple.SCENARIOS, ConversionService.class);
    }

    @Test(expected = ConversionService.ConversionExeption.class)
    public void testUnmarshalWithoutType() {
        this.service.readXml(Simple.SCENARIOS, null);
    }

}
