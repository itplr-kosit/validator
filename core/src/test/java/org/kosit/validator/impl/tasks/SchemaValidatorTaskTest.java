package org.kosit.validator.impl.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.apache.commons.io.input.BoundedInputStream;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.source.CTReadResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.SchemaProvider;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.TestObjectFactory;
import org.kosit.validator.impl.conformatron.source.ReadResource;
import org.kosit.validator.impl.conformatron.source.Resource;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.tasks.CheckTask.Process;
import org.xml.sax.SAXException;

/**
 * Tests the {@link SchemaValidationTask}.
 *
 * @author Andreas Penski
 */
public class SchemaValidatorTaskTest {

    private SchemaValidationTask service;

    @BeforeEach
    public void setup() {
        this.service = new SchemaValidationTask(TestObjectFactory.getProcessor());
    }

    @Test
    public void testSimple() {
        final Process process = TestProcessBuilder.create(TestHelper.read(Simple.SIMPLE_VALID)).build();
        final ProcessStepResult<Boolean, SimpleError> processStepResult = this.service.check(process);
        final SingleProcessingResult<?, ?> result = processStepResult.getResult();
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testValidationFailure() {
        final CTReadResource input = TestHelper.read(Simple.SCHEMA_INVALID);
        final Process process = TestProcessBuilder.create(input).build();
        final ProcessStepResult<Boolean, SimpleError> processStepResult = this.service.check(process);
        final SingleProcessingResult<Boolean, SimpleError> result = processStepResult.getResult();
        assertThat(result.isValid()).isFalse();
        result.getErrors().forEach(e -> {
            assertThat(e.getLineNumber()).isPositive();
            assertThat(e.getColumnNumber()).isPositive();
            assertThat(e.getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        });
    }

    @Test
    public void testSchemaReferences() {
        final Schema reportInputSchema = SchemaProvider.getXvrlSchema();
        assertThat(reportInputSchema).isNotNull();
    }

    @Test
    public void testNoRepeatableRead() throws Exception {
        try ( final InputStream inputStream = Simple.SIMPLE_VALID.toURL().openStream() ) {
            // don't read the real inputstream here, use a dummy result!
            final Process process = TestProcessBuilder
                    .create(ReadResource.inMemory(Resource.of(Simple.SIMPLE_VALID.toASCIIString(), inputStream)), false)
                    .setParseResult(TestHelper.read(Simple.SIMPLE_VALID)).build();
            final SingleProcessingResult<Boolean, SimpleError> result = this.service.check(process).getResult();
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }
    }

    @Test
    public void testNoRepeatableReadBigFile() throws Exception {
        try ( final InputStream inputStream = Simple.SIMPLE_VALID.toURL().openStream();
              final InputStream lis = BoundedInputStream.builder().setInputStream(inputStream).setCount(6).get() ) {
            final ReadResource input = ReadResource.inMemory(Resource.of(Simple.SIMPLE_VALID.toASCIIString(), lis));
            final Process process = TestProcessBuilder.create(input).build();
            // process.addStepResult(Helper.createParseResult(Simple.SIMPLE_VALID));

            // set limit and length for serialization to 5 bytes
            this.service.setInMemoryLimit(5L);

            final SingleProcessingResult<Boolean, SimpleError> result = this.service.check(process).getResult();
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }
    }

    @Test
    public void testProcessingError() throws IOException, SAXException {
        final Process process = TestProcessBuilder.create(TestHelper.read(Simple.SIMPLE_VALID)).build();
        final SingleProcessingResult<Scenario, String> scenarioCheckResult = process.getResult(ScenarioSelectionTask.KEY);
        final Scenario scenario = scenarioCheckResult.getObject();
        final Schema schema = mock(Schema.class);
        final Validator validator = mock(Validator.class);
        when(schema.newValidator()).thenReturn(validator);
        doThrow(SAXException.class).when(validator).validate(any());
        scenario.setSchema(schema);
        final ProcessStepResult<Boolean, SimpleError> processStepResult = this.service.check(process);
        final SingleProcessingResult<Boolean, SimpleError> result = processStepResult.getResult();
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

}
