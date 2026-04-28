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

package org.kosit.validator.impl.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kosit.validator.api.AcceptRecommendation;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.tasks.CheckAction.Process;
import org.kosit.validator.model.ValidationResultsSchematron;
import org.kosit.validator.model.XMLSyntaxError;

import net.sf.saxon.s9api.XPathExecutable;

/**
 * Tests the 'acceptMatch' functionality.
 * 
 * @author Andreas Penski
 */
public class ComputeAcceptanceActionTest {

    private static final String DOESNOT_EXIST = "count(//doesnotExist) = 0";

    private final ComputeAcceptanceAction action = new ComputeAcceptanceAction();

    private static XPathExecutable createXpath(final String expression) {
        return new ContentRepository(Helper.getTestProcessor(), ResolvingMode.STRICT_RELATIVE.getStrategy(), null).createXPath(expression,
                new HashMap<>());
    }

    @Test
    public void simpleTest() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronValid().setDummyReport().build();
        final Result<AcceptRecommendation, XMLSyntaxError> result = process.getResult(ComputeAcceptanceAction.KEY);
        assertThat(result).isNull();
        final ProcessStepResult<AcceptRecommendation, XMLSyntaxError> stepResult = this.action.check(process);
        final Result<AcceptRecommendation, XMLSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testSchemaFailed() {
        final Process process = TestProcessBuilder.create().schemaInvalid().setDummyReport().build();
        final ProcessStepResult<AcceptRecommendation, XMLSyntaxError> stepResult = this.action.check(process);
        final Result<AcceptRecommendation, XMLSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.REJECT);
    }

    @Test
    public void testSchematronFailed() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronInvalid().setDummyReport().build();
        final ProcessStepResult<AcceptRecommendation, XMLSyntaxError> stepResult = this.action.check(process);
        final Result<AcceptRecommendation, XMLSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.REJECT);
    }

    @Test
    public void testValidAcceptMatch() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronValid().setDummyReport().build();
        final Result<Scenario, String> scenarioSelectionResult = process.getResult(ScenarioSelectionAction.KEY);
        scenarioSelectionResult.getObject().setAcceptExecutable(createXpath(DOESNOT_EXIST));
        final ProcessStepResult<AcceptRecommendation, XMLSyntaxError> stepResult = this.action.check(process);
        final Result<AcceptRecommendation, XMLSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testAcceptMatchNotSatisfied() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronValid().setDummyReport().build();
        final Result<Scenario, String> scenarioSelectionResult = process.getResult(ScenarioSelectionAction.KEY);
        scenarioSelectionResult.getObject().setAcceptExecutable(createXpath("count(//doesnotExist) = 1"));
        final ProcessStepResult<AcceptRecommendation, XMLSyntaxError> stepResult = this.action.check(process);
        final Result<AcceptRecommendation, XMLSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.REJECT);
    }

    @Test
    public void testAcceptMatchOverridesSchematronErrors() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronInvalid().setDummyReport().build();
        final Result<Scenario, String> scenarioSelectionResult = process.getResult(ScenarioSelectionAction.KEY);
        scenarioSelectionResult.getObject().setAcceptExecutable(createXpath(DOESNOT_EXIST));
        final ProcessStepResult<AcceptRecommendation, XMLSyntaxError> stepResult = this.action.check(process);
        final Result<AcceptRecommendation, XMLSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testValidAcceptMatchOnSchemaFailed() {
        final Process process = TestProcessBuilder.create().schemaInvalid().schematronValid().setDummyReport().build();
        final Result<Scenario, String> scenarioSelectionResult = process.getResult(ScenarioSelectionAction.KEY);
        scenarioSelectionResult.getObject().setAcceptExecutable(createXpath(DOESNOT_EXIST));
        final ProcessStepResult<AcceptRecommendation, XMLSyntaxError> stepResult = this.action.check(process);
        final Result<AcceptRecommendation, XMLSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.REJECT);
    }

    @Test
    public void testMissingSchemaCheck() {
        final Process process = TestProcessBuilder.create().schematronValid().setDummyReport().build();
        final ProcessStepResult<AcceptRecommendation, XMLSyntaxError> stepResult = this.action.check(process);
        final Result<AcceptRecommendation, XMLSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.REJECT);
    }

    @Test
    public void testNoSchematronCheck() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronValid().setDummyReport().build();
        // remove schematron results
        final ProcessStepResult<List<ValidationResultsSchematron>, String> processStepResult = process
                .getActionResult(SchematronValidationAction.KEY).get();

        process.getProcessStepResults().remove(processStepResult);
        final ProcessStepResult<AcceptRecommendation, XMLSyntaxError> stepResult = this.action.check(process);
        final Result<AcceptRecommendation, XMLSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testMissingReport() {
        final Process process = TestProcessBuilder.create().schemaInvalid().schematronValid().build();
        final ProcessStepResult<AcceptRecommendation, XMLSyntaxError> stepResult = this.action.check(process);
        final Result<AcceptRecommendation, XMLSyntaxError> checkResult = stepResult.getResult();
        assertThat(checkResult.getObject()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.REJECT);
    }
}
