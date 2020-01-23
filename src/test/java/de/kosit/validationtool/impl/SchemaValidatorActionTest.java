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
import java.net.URL;
import java.util.Collections;

import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.CheckAction;
import de.kosit.validationtool.impl.tasks.SchemaValidationAction;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.scenarios.ResourceType;
import de.kosit.validationtool.model.scenarios.ScenarioType;
import de.kosit.validationtool.model.scenarios.ValidateWithXmlSchema;

/**
 * Testet die {@linkSchemaValidatorAction}.
 * 
 * @author Andreas Penski
 */
public class SchemaValidatorActionTest {

    private static final URL VALID_EXAMPLE = SchemaValidatorActionTest.class
            .getResource("/examples/UBLReady/UBLReady_EU_UBL-NL_20170102_FULL.xml");

    private static final URI INVALID_EXAMPLE = Helper.TEST_ROOT.resolve("invalid/scenarios-invalid.xml");

    public ExpectedException expectedException = ExpectedException.none();

    private SchemaValidationAction service;

    private ContentRepository repository;

    @Before
    public void setup() {
        this.service = new SchemaValidationAction();
        this.repository = new ContentRepository(ObjectFactory.createProcessor(), Helper.REPOSITORY);

    }

    @Test
    public void testSimple() {
        final CheckAction.Bag bag = new CheckAction.Bag(InputFactory.read(VALID_EXAMPLE), new CreateReportInput());
        final ScenarioType t = new ScenarioType();
        final ValidateWithXmlSchema v = new ValidateWithXmlSchema();
        final ResourceType r = new ResourceType();
        r.setLocation("resources/eRechnung/UBL-2.1/xsdrt/maindoc/UBL-Invoice-2.1.xsd");
        r.setName("invoice");
        v.getResource().add(r);
        t.setValidateWithXmlSchema(v);
        t.initialize(this.repository, true);
        bag.setScenarioSelectionResult(new Result<>(t, Collections.emptyList()));
        this.service.check(bag);
        assertThat(bag.getSchemaValidationResult().isValid()).isTrue();
        assertThat(bag.getSchemaValidationResult()).isNotNull();
        assertThat(bag.getSchemaValidationResult().isValid()).isTrue();
    }

    @Test
    public void testValidationFailure() throws MalformedURLException {
        final CheckAction.Bag bag = new CheckAction.Bag(InputFactory.read(INVALID_EXAMPLE.toURL()), new CreateReportInput());
        final ScenarioType t = new ScenarioType();
        final ValidateWithXmlSchema v = new ValidateWithXmlSchema();
        final ResourceType r = new ResourceType();
        r.setLocation(Helper.REPOSITORY.relativize(Helper.SCENARIO_SCHEMA).getRawPath());
        r.setName("invoice");
        v.getResource().add(r);
        t.setValidateWithXmlSchema(v);
        t.initialize(this.repository, true);
        bag.setScenarioSelectionResult(new Result<>(t, Collections.emptyList()));
        this.service.check(bag);
        assertThat(bag.getSchemaValidationResult().isValid()).isFalse();
        bag.getSchemaValidationResult().getErrors().forEach(e->{
            assertThat(e.getRowNumber()).isGreaterThan(0);
            assertThat(e.getColumnNumber()).isGreaterThan(0);
        });
    }


    @Test
    public void testSchemaReferences() {
        final Schema reportInputSchema = this.repository.getReportInputSchema();
        assertThat(reportInputSchema).isNotNull();
    }

}
