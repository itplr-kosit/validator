package org.kosit.validator.impl.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kosit.validator.api.xvrl.compact.AcceptRecommendation;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.tasks.CheckTask.Process;
import org.kosit.validator.model.ValidationResultsSchematron;
import org.kosit.validator.model.XmlSyntaxError;

import net.sf.saxon.s9api.XPathExecutable;

/**
 * Tests the 'acceptMatch' functionality.
 * 
 * @author Andreas Penski
 */
public class ComputeAcceptanceTaskTest {

    private static final String DOESNOT_EXIST = "count(//doesnotExist) = 0";

    private final ComputeAcceptanceTask action = new ComputeAcceptanceTask();

    private static XPathExecutable createXpath(final String expression) {
        return new ContentRepository(TestHelper.getTestProcessor(), ResolvingMode.STRICT_RELATIVE.getStrategy(), null)
                .createXPath(expression, new HashMap<>());
    }

    @Test
    public void simpleTest() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronValid().setDummyReport().build();
        final SingleProcessingResult<AcceptRecommendation, XmlSyntaxError> result = process.getResult(ComputeAcceptanceTask.KEY);
        assertThat(result).isNull();
        final ProcessStepResult<AcceptRecommendation, XmlSyntaxError> stepResult = this.action.check(process);
        final SingleProcessingResult<AcceptRecommendation, XmlSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.xvrl.compact.AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testSchemaFailed() {
        final Process process = TestProcessBuilder.create().schemaInvalid().setDummyReport().build();
        final ProcessStepResult<AcceptRecommendation, XmlSyntaxError> stepResult = this.action.check(process);
        final SingleProcessingResult<AcceptRecommendation, XmlSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.xvrl.compact.AcceptRecommendation.REJECT);
    }

    @Test
    public void testSchematronFailed() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronInvalid().setDummyReport().build();
        final ProcessStepResult<AcceptRecommendation, XmlSyntaxError> stepResult = this.action.check(process);
        final SingleProcessingResult<AcceptRecommendation, XmlSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.xvrl.compact.AcceptRecommendation.REJECT);
    }

    @Test
    public void testValidAcceptMatch() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronValid().setDummyReport().build();
        final SingleProcessingResult<Scenario, String> scenarioSelectionResult = process.getResult(ScenarioSelectionTask.KEY);
        scenarioSelectionResult.getObject().setAcceptExecutable(createXpath(DOESNOT_EXIST));
        final ProcessStepResult<AcceptRecommendation, XmlSyntaxError> stepResult = this.action.check(process);
        final SingleProcessingResult<AcceptRecommendation, XmlSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.xvrl.compact.AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testAcceptMatchNotSatisfied() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronValid().setDummyReport().build();
        final SingleProcessingResult<Scenario, String> scenarioSelectionResult = process.getResult(ScenarioSelectionTask.KEY);
        scenarioSelectionResult.getObject().setAcceptExecutable(createXpath("count(//doesnotExist) = 1"));
        final ProcessStepResult<AcceptRecommendation, XmlSyntaxError> stepResult = this.action.check(process);
        final SingleProcessingResult<AcceptRecommendation, XmlSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.xvrl.compact.AcceptRecommendation.REJECT);
    }

    @Test
    public void testAcceptMatchOverridesSchematronErrors() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronInvalid().setDummyReport().build();
        final SingleProcessingResult<Scenario, String> scenarioSelectionResult = process.getResult(ScenarioSelectionTask.KEY);
        scenarioSelectionResult.getObject().setAcceptExecutable(createXpath(DOESNOT_EXIST));
        final ProcessStepResult<AcceptRecommendation, XmlSyntaxError> stepResult = this.action.check(process);
        final SingleProcessingResult<AcceptRecommendation, XmlSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.xvrl.compact.AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testValidAcceptMatchOnSchemaFailed() {
        final Process process = TestProcessBuilder.create().schemaInvalid().schematronValid().setDummyReport().build();
        final SingleProcessingResult<Scenario, String> scenarioSelectionResult = process.getResult(ScenarioSelectionTask.KEY);
        scenarioSelectionResult.getObject().setAcceptExecutable(createXpath(DOESNOT_EXIST));
        final ProcessStepResult<AcceptRecommendation, XmlSyntaxError> stepResult = this.action.check(process);
        final SingleProcessingResult<AcceptRecommendation, XmlSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.xvrl.compact.AcceptRecommendation.REJECT);
    }

    @Test
    public void testMissingSchemaCheck() {
        final Process process = TestProcessBuilder.create().schematronValid().setDummyReport().build();
        final ProcessStepResult<AcceptRecommendation, XmlSyntaxError> stepResult = this.action.check(process);
        final SingleProcessingResult<AcceptRecommendation, XmlSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.xvrl.compact.AcceptRecommendation.REJECT);
    }

    @Test
    public void testNoSchematronCheck() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronValid().setDummyReport().build();
        // remove schematron results
        final ProcessStepResult<List<ValidationResultsSchematron>, String> processStepResult = process
                .getActionResult(SchematronValidationTask.KEY).get();

        process.getProcessStepResults().remove(processStepResult);
        final ProcessStepResult<AcceptRecommendation, XmlSyntaxError> stepResult = this.action.check(process);
        final SingleProcessingResult<AcceptRecommendation, XmlSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.xvrl.compact.AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testMissingReport() {
        final Process process = TestProcessBuilder.create().schemaInvalid().schematronValid().build();
        final ProcessStepResult<AcceptRecommendation, XmlSyntaxError> stepResult = this.action.check(process);
        final SingleProcessingResult<AcceptRecommendation, XmlSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.xvrl.compact.AcceptRecommendation.REJECT);
    }
}
