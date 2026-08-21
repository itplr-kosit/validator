package org.kosit.validator.impl.tasks;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VInput;
import org.kosit.validator.api.VInputFactory;
import org.kosit.validator.api.XmlError.Severity;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.SchemaProvider;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.TestObjectFactory;
import org.kosit.validator.impl.input.SourceVInput;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.tasks.CheckAction.Process;
import org.kosit.validator.model.XMLSyntaxError;
import org.xml.sax.SAXException;

/**
 * Tests the {@link SchemaValidationAction}.
 *
 * @author Andreas Penski
 */
public class SchemaValidatorActionTest {

    private SchemaValidationAction service;

    @BeforeEach
    public void setup() {
        this.service = new SchemaValidationAction(TestObjectFactory.createProcessor());
    }

    @Test
    public void testSimple() throws MalformedURLException {
        final Process process = TestProcessBuilder.create(VInputFactory.read(Simple.SIMPLE_VALID.toURL())).build();
        final ProcessStepResult<Boolean, XMLSyntaxError> processStepResult = this.service.check(process);
        final Result<?, ?> result = processStepResult.getResult();
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testValidationFailure() throws MalformedURLException {
        final VInput VInput = VInputFactory.read(Simple.SCHEMA_INVALID.toURL());
        final Process process = TestProcessBuilder.create(VInput).build();
        final ProcessStepResult<Boolean, XMLSyntaxError> processStepResult = this.service.check(process);
        final Result<Boolean, XMLSyntaxError> result = processStepResult.getResult();
        assertThat(result.isValid()).isFalse();
        result.getErrors().forEach(e -> {
            assertThat(e.getRowNumber()).isPositive();
            assertThat(e.getColumnNumber()).isPositive();
            assertThat(e.getSeverity()).isEqualTo(Severity.SEVERITY_ERROR);
        });
    }

    @Test
    public void testSchemaReferences() {
        final Schema reportInputSchema = SchemaProvider.getXVRLSchema();
        assertThat(reportInputSchema).isNotNull();
    }

    @Test
    public void testNoRepeatableRead() throws Exception {
        try ( final InputStream inputStream = Simple.SIMPLE_VALID.toURL().openStream() ) {
            // don't read the real inputstream here, use a dummy result!
            final Process process = TestProcessBuilder.create(VInputFactory.read(new StreamSource(inputStream)), false)
                    .setParseResult(VInputFactory.read(Simple.SIMPLE_VALID)).build();
            final Result<Boolean, XMLSyntaxError> result = this.service.check(process).getResult();
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }
    }

    @Test
    public void testNoRepeatableReadBigFile() throws Exception {
        try ( final InputStream inputStream = Simple.SIMPLE_VALID.toURL().openStream() ) {
            final SourceVInput input = (SourceVInput) VInputFactory.read(new StreamSource(inputStream));
            final Process process = TestProcessBuilder.create(input).build();
            // process.addStepResult(Helper.createParseResult(Simple.SIMPLE_VALID));

            // set limit and length for serialization to 5 bytes
            this.service.setInMemoryLimit(5L);
            input.setLength(6L);

            final Result<Boolean, XMLSyntaxError> result = this.service.check(process).getResult();
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }
    }

    @Test
    public void testNoRepeatableReaderInput() throws Exception {
        try ( final InputStream inputStream = Simple.SIMPLE_VALID.toURL().openStream();
              final Reader reader = new InputStreamReader(inputStream) ) {
            final SourceVInput input = (SourceVInput) VInputFactory.read(new StreamSource(reader));
            final Process process = TestProcessBuilder.create(input).build();
            this.service.check(process);
            final ProcessStepResult<Boolean, XMLSyntaxError> processStepResult = this.service.check(process);
            final Result<Boolean, XMLSyntaxError> result = processStepResult.getResult();
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }
    }

    @Test
    public void testNoRepeatableReaderInputBigFile() throws Exception {
        try ( final InputStream inputStream = Simple.SIMPLE_VALID.toURL().openStream();
              final Reader reader = new InputStreamReader(inputStream) ) {
            final SourceVInput input = (SourceVInput) VInputFactory.read(new StreamSource(reader));
            final Process process = TestProcessBuilder.create(input).setParseResult(VInputFactory.read(Simple.SIMPLE_VALID)).build();
            // set limit and length for serialization to 5 bytes
            this.service.setInMemoryLimit(5L);
            this.service.check(process);
            final ProcessStepResult<Boolean, XMLSyntaxError> processStepResult = this.service.check(process);
            final Result<Boolean, XMLSyntaxError> result = processStepResult.getResult();
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }
    }

    @Test
    public void testProcessingError() throws IOException, SAXException {
        final Process process = TestProcessBuilder.create(VInputFactory.read(Simple.SIMPLE_VALID.toURL())).build();
        final Result<Scenario, String> scenarioCheckResult = process.getResult(ScenarioSelectionAction.KEY);
        final Scenario scenario = scenarioCheckResult.getObject();
        final Schema schema = mock(Schema.class);
        final Validator validator = mock(Validator.class);
        when(schema.newValidator()).thenReturn(validator);
        doThrow(SAXException.class).when(validator).validate(any());
        scenario.setSchema(schema);
        final ProcessStepResult<Boolean, XMLSyntaxError> processStepResult = this.service.check(process);
        final Result<Boolean, XMLSyntaxError> result = processStepResult.getResult();
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

}
