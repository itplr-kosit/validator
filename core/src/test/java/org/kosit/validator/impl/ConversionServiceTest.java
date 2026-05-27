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

package org.kosit.validator.impl;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.jaxb.JaxbConversionException;
import org.kosit.validator.api.xsd.ValidatorSchemas;
import org.kosit.validator.impl.Helper.Invalid;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.model.scenarios.Scenarios;

/**
 * Simple test for testing the jaxb conversion service.
 * 
 * @author apenski
 */
public class ConversionServiceTest {

    private static final URI SCHEMA = URI.create(ValidatorSchemas.class.getResource(ValidatorSchemas.SCENARIOS_XSD_PATH).toExternalForm());

    private ScenariosConversionService service;

    private ContentRepository repository;

    @BeforeEach
    public void setup() {
        this.service = new ScenariosConversionService();
        this.repository = Simple.createContentRepository();
    }

    @Test
    public void testMarshalNull() {
        assertThrows(JaxbConversionException.class, () -> this.service.writeXml(null));
    }

    @Test
    public void testMarshalUnknown() {
        assertThrows(JaxbConversionException.class, () -> this.service.writeXml(new Serializable() {
        }));
    }

    @Test
    public void testUnmarshal() {
        final Scenarios s = this.service.readXml(Simple.SCENARIOS, Scenarios.class);
        assertThat(s).isNotNull();
        assertThat(s.getName()).isEqualToIgnoringCase("HTML-TestSuite");
    }

    @Test
    public void testUnmarshalWithSchema() throws MalformedURLException {
        // since repository.createSchema(URI) forcibly resolves uri in repository path only, conversion to url is
        // neccesary
        final Scenarios s = this.service.withSchema(this.repository.createSchema(SCHEMA.toURL())).readXml(Simple.SCENARIOS,
                Scenarios.class);
        assertThat(s).isNotNull();
        assertThat(s.getName()).isEqualToIgnoringCase("HTML-TestSuite");
    }

    @Test
    public void testUnmarshalInvalidXml() {
        assertThrows(JaxbConversionException.class,
                () -> this.service.withSchema(this.repository.createSchema(SCHEMA.toURL())).readXml(Invalid.SCENARIOS, Scenarios.class));
    }

    @Test
    public void testUnmarshalIllFormed() {
        assertThrows(JaxbConversionException.class, () -> this.service.withSchema(this.repository.createSchema(SCHEMA.toURL()))
                .readXml(Invalid.SCENARIOS_ILLFORMED, Scenarios.class));
    }

    @Test
    public void testUnmarshalEmpty() {
        assertThrows(JaxbConversionException.class, () -> this.service.readXml((URI) null, Scenarios.class));
    }

    @Test
    public void testUnmarshalUnknownType() {
        assertThrows(JaxbConversionException.class, () -> this.service.readXml(Simple.SCENARIOS, ScenariosConversionService.class));
    }

    @Test
    public void testUnmarshalWithoutType() {
        assertThrows(JaxbConversionException.class, () -> this.service.readXml(Simple.SCENARIOS, null));
    }

}
