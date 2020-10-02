/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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

package de.kosit.validationtool.impl.tasks;

import static de.kosit.validationtool.impl.tasks.TestBagBuilder.createBag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.XmlError.Severity;
import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.TestObjectFactory;
import de.kosit.validationtool.impl.input.SourceInput;
import de.kosit.validationtool.impl.tasks.CheckAction.Bag;
import de.kosit.validationtool.impl.xml.StrictRelativeResolvingStrategy;

/**
 * Tests die {@link SchemaValidationAction}.
 * 
 * @author Andreas Penski
 */
public class SchemaValidatorActionTest {

    public ExpectedException expectedException = ExpectedException.none();

    private SchemaValidationAction service;

    @Before
    public void setup() {
        this.service = new SchemaValidationAction(new StrictRelativeResolvingStrategy(), TestObjectFactory.createProcessor());
    }

    @Test
    public void testSimple() throws MalformedURLException {
        final CheckAction.Bag bag = createBag(InputFactory.read(Simple.SIMPLE_VALID.toURL()));
        this.service.check(bag);
        assertThat(bag.getSchemaValidationResult().isValid()).isTrue();
        assertThat(bag.getSchemaValidationResult()).isNotNull();
        assertThat(bag.getSchemaValidationResult().isValid()).isTrue();
    }

    @Test
    public void testValidationFailure() throws MalformedURLException {
        final Input input = InputFactory.read(Simple.SCHEMA_INVALID.toURL());
        final CheckAction.Bag bag = createBag(input);
        this.service.check(bag);
        assertThat(bag.getSchemaValidationResult().isValid()).isFalse();
        bag.getSchemaValidationResult().getErrors().forEach(e -> {
            assertThat(e.getRowNumber()).isGreaterThan(0);
            assertThat(e.getColumnNumber()).isGreaterThan(0);
            assertThat(e.getSeverity()).isEqualTo(Severity.SEVERITY_ERROR);
        });
    }

    @Test
    public void testSchemaReferences() {
        final Schema reportInputSchema = Simple.createContentRepository().getReportInputSchema();
        assertThat(reportInputSchema).isNotNull();
    }

    @Test
    public void testNoRepeatableRead() throws Exception {
        try ( final InputStream inputStream = Simple.SIMPLE_VALID.toURL().openStream() ) {
            final Bag bag = createBag(InputFactory.read(new StreamSource(inputStream)));
            // don't read the real inputstream here!
            bag.setParserResult(Helper.parseDocument(InputFactory.read(Simple.SIMPLE_VALID.toURL())));
            this.service.check(bag);
            assertThat(bag.getSchemaValidationResult()).isNotNull();
            assertThat(bag.getSchemaValidationResult().isValid()).isTrue();
        }
    }

    @Test
    public void testNoRepeatableReadBigFile() throws Exception {
        try ( final InputStream inputStream = Simple.SIMPLE_VALID.toURL().openStream() ) {
            final SourceInput input = (SourceInput) InputFactory.read(new StreamSource(inputStream));
            final Bag bag = createBag(input);
            // set limit and length for serialization to 5 bytes
            this.service.setInMemoryLimit(5L);
            input.setLength(6L);

            bag.setParserResult(Helper.parseDocument(InputFactory.read(Simple.SIMPLE_VALID.toURL())));
            this.service.check(bag);
            assertThat(bag.getSchemaValidationResult()).isNotNull();
            assertThat(bag.getSchemaValidationResult().isValid()).isTrue();
        }
    }

    @Test
    public void testNoRepeatableReaderInput() throws Exception {
        try ( final InputStream inputStream = Simple.SIMPLE_VALID.toURL().openStream();
              final Reader reader = new InputStreamReader(inputStream) ) {
            final SourceInput input = (SourceInput) InputFactory.read(new StreamSource(reader));
            final Bag bag = createBag(input);
            bag.setParserResult(Helper.parseDocument(InputFactory.read(Simple.SIMPLE_VALID.toURL())));
            this.service.check(bag);
            this.service.check(bag);
            assertThat(bag.getSchemaValidationResult()).isNotNull();
            assertThat(bag.getSchemaValidationResult().isValid()).isTrue();
        }
    }

    @Test
    public void testNoRepeatableReaderInputBigFile() throws Exception {
        try ( final InputStream inputStream = Simple.SIMPLE_VALID.toURL().openStream();
              final Reader reader = new InputStreamReader(inputStream) ) {
            final SourceInput input = (SourceInput) InputFactory.read(new StreamSource(reader));
            final Bag bag = createBag(input);
            // set limit and length for serialization to 5 bytes
            this.service.setInMemoryLimit(5L);
            bag.setParserResult(Helper.parseDocument(InputFactory.read(Simple.SIMPLE_VALID.toURL())));
            this.service.check(bag);
            this.service.check(bag);
            assertThat(bag.getSchemaValidationResult()).isNotNull();
            assertThat(bag.getSchemaValidationResult().isValid()).isTrue();
        }
    }

    @Test
    public void testProcessingError() throws IOException, SAXException {
        final CheckAction.Bag bag = createBag(InputFactory.read(Simple.SIMPLE_VALID.toURL()));
        final Scenario scenario = bag.getScenarioSelectionResult().getObject();
        final Schema schema = mock(Schema.class);
        final Validator validator = mock(Validator.class);
        when(schema.newValidator()).thenReturn(validator);
        doThrow(SAXException.class).when(validator).validate(any());
        scenario.setSchema(schema);
        this.service.check(bag);
        assertThat(bag.getReportInput().getProcessingError().getError()).isNotEmpty();
    }

}
