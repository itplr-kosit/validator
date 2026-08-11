package org.kosit.validator.impl.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.Configuration;
import org.kosit.validator.api.InputFactory;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.Scenario.Transformation;
import org.kosit.svrl.impl.SvrlConversionService;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.model.ValidationResultsSchematron;
import org.kosit.validator.model.scenarios.ResourceType;
import org.kosit.validator.model.scenarios.ValidateWithSchematron;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Tests {@link SchematronValidationAction}.
 * 
 * @author Andreas Penski
 */
public class SchematronValidationActionTest {

    private SchematronValidationAction action;

    @BeforeEach
    public void setup() {
        this.action = new SchematronValidationAction(new SvrlConversionService());
    }

    @Test
    public void testProcessingError() throws IOException, SaxonApiException {
        final CheckAction.Process process = TestProcessBuilder.create(InputFactory.read(Simple.SIMPLE_VALID.toURL())).build();

        final Result<Scenario, String> scenarioResult = process.getResult(ScenarioSelectionAction.KEY);
        final Scenario scenario = scenarioResult.getObject();
        final XsltExecutable exec = mock(XsltExecutable.class);
        final XsltTransformer transformer = mock(XsltTransformer.class);
        doThrow(new SaxonApiException("invalid")).when(transformer).transform();
        when(exec.load()).thenReturn(transformer);
        final ResourceType resourceType = new ResourceType();
        resourceType.setName("invalid internal");
        scenario.setSchematronValidations(Collections.singletonList(new Transformation(exec, resourceType)));
        final ProcessStepResult<List<ValidationResultsSchematron>, String> processStepResult = this.action.check(process);
        final Result<List<ValidationResultsSchematron>, String> result = processStepResult.getResult();
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    public void testSchxsltRuntimeProcessingError() throws IOException {
        final CheckAction.Process process = TestProcessBuilder.create(InputFactory.read(Simple.SIMPLE_VALID.toURL())).build();
        final Scenario scenario = process.getResult(ScenarioSelectionAction.KEY).getObject();

        // real SchXslt compilation (no mock): the schematron compiles fine but raises a dynamic
        // XPath error (FORG0001) when validating simple.xml, whose inner element is not a number
        final ContentRepository repo = new ContentRepository(Helper.getTestProcessor(), ResolvingMode.STRICT_RELATIVE.getStrategy(),
                Simple.REPOSITORY_URI);
        final ValidateWithSchematron validateWithSchematron = new ValidateWithSchematron();
        final ResourceType resource = new ResourceType();
        resource.setName("runtime error schematron");
        resource.setLocation("simple-runtime-error.sch");
        validateWithSchematron.setResource(resource);
        scenario.setSchematronValidations(Collections.singletonList(repo.createSchematronTransformation(validateWithSchematron)));

        final ProcessStepResult<List<ValidationResultsSchematron>, String> processStepResult = this.action.check(process);
        final Result<List<ValidationResultsSchematron>, String> result = processStepResult.getResult();

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().iterator().next()).contains("Error processing schematron validation")
                .contains("runtime error schematron");
        assertThat(process.isStopped()).isTrue();
        // the processing error also contributes to the report (synthetic failed-assert)
        assertThat(processStepResult.getReport()).isNotEmpty();
    }

    @Test
    public void testXsltValid() throws MalformedURLException {
        final Configuration c = Configuration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI).build(Helper.getTestProcessor());
        final CheckAction.Process process = TestProcessBuilder.create(InputFactory.read(Simple.SIMPLE_VALID.toURL()))
                .setScenario(c.getScenarios().get(0)).build();
        final ProcessStepResult<List<ValidationResultsSchematron>, String> processStepResult = this.action.check(process);
        final Result<List<ValidationResultsSchematron>, String> result = processStepResult.getResult();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    public void testSchCompiledValid() throws MalformedURLException {
        final Configuration c = Configuration.load(Simple.SCENARIOS_WITH_SCH, Simple.REPOSITORY_URI).build(Helper.getTestProcessor());
        final CheckAction.Process process = TestProcessBuilder.create(InputFactory.read(Simple.SIMPLE_VALID.toURL()))
                .setScenario(c.getScenarios().get(0)).build();
        final ProcessStepResult<List<ValidationResultsSchematron>, String> processStepResult = this.action.check(process);
        final Result<List<ValidationResultsSchematron>, String> result = processStepResult.getResult();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    public void testIsoSchCompiledValid() throws MalformedURLException {
        final Configuration c = Configuration.load(Simple.SCENARIOS_WITH_SCH, Simple.REPOSITORY_URI).build(Helper.getTestProcessor());
        final CheckAction.Process process = TestProcessBuilder.create(InputFactory.read(Simple.SIMPLE_ISO_VALID.toURL()))
                .setScenario(c.getScenarios().get(0)).build();
        final ProcessStepResult<List<ValidationResultsSchematron>, String> processStepResult = this.action.check(process);
        final Result<List<ValidationResultsSchematron>, String> result = processStepResult.getResult();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }
}
