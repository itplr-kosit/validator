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

import java.util.Collections;
import java.util.List;

import org.kosit.validator.api.Input;
import org.kosit.validator.api.InputFactory;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.tasks.CheckAction.Process;
import org.kosit.validator.model.ValidationResultsSchematron;
import org.kosit.validator.model.ValidationResultsSchematron.Results;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.xvrl.model.XVRLMetadata;
import org.kosit.xvrl.model.XVRLReport;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;

import net.sf.saxon.s9api.XdmNode;

/**
 * Utilities for creating test objects.
 * 
 * @author Andreas Penski
 */
public class TestProcessBuilder {

    private Process process;

    public static TestProcessBuilder create() {
        return create(InputFactory.read("<someXml></someXml>".getBytes(), "someCheck"));
    }

    public static TestProcessBuilder create(final Input input) {
        return create(input, true);
    }

    public static TestProcessBuilder create(final Input input, final boolean parse) {
        final TestProcessBuilder builder = new TestProcessBuilder();
        builder.process = new Process(input, new XVRLMetadata());
        if (parse) {
            builder.parse(input);
        }
        builder.setScenario(TestScenarioBuilder.createDefault());
        return builder;
    }

    public static List<BusinessReport> createReport(final String id, final XdmNode node) {
        final BusinessReport r = new BusinessReport();
        r.setReport(new XVRLReport());
        r.setName(id);
        r.setContent(node);
        return Collections.singletonList(r);
    }

    public static List<BusinessReport> createReport() {
        final XdmNode someXml = Helper.parseDocument(InputFactory.read("<some>xml</some>".getBytes(), "someXml")).getObject();
        return createReport("report", someXml);
    }

    private static ProcessStepResult<XdmNode, XMLSyntaxError> parseInput(final Input input) {
        final ProcessStepResult<XdmNode, XMLSyntaxError> stepResult = new ProcessStepResult<>(DocumentParseAction.KEY);
        stepResult.setResult(Helper.parseDocument(input));
        stepResult.setReport(new XVRLReport());
        return stepResult;
    }

    public TestProcessBuilder setParseResult(final Input input) {
        this.process.addStepResult(parseInput(input));
        return this;
    }

    public TestProcessBuilder setMetadata(final XVRLMetadata metadata) {
        this.process.setMetadata(metadata);
        return this;
    }

    public TestProcessBuilder schemaValid() {
        return setSchemaValidationResult(true, null);
    }

    public TestProcessBuilder schemaInvalid() {
        final XMLSyntaxError error = new XMLSyntaxError();
        error.setMessage("Default error");
        return setSchemaValidationResult(false, Collections.singletonList(error));

    }

    public TestProcessBuilder setSchemaValidationResult(final boolean value, final List<XMLSyntaxError> errors) {
        return setSchemaValidationResult(new Result<>(value, errors));
    }

    public TestProcessBuilder setSchemaValidationResult(final Result<Boolean, XMLSyntaxError> schemaResult) {
        final ProcessStepResult<Boolean, XMLSyntaxError> stepResult = new ProcessStepResult<>(SchemaValidationAction.KEY);
        stepResult.setResult(schemaResult);
        stepResult.setReport(new XVRLReport());
        this.process.addStepResult(stepResult);
        return this;
    }

    public TestProcessBuilder setSchematronResult(final List<ValidationResultsSchematron> result) {
        return setSchematronResult(new Result<>(result, null));
    }

    public TestProcessBuilder setSchematronResult(final Result<List<ValidationResultsSchematron>, String> schematronResult) {
        final ProcessStepResult<List<ValidationResultsSchematron>, String> stepResult = new ProcessStepResult<>(
                SchematronValidationAction.KEY);
        stepResult.setResult(schematronResult);
        stepResult.setReport(new XVRLReport());
        this.process.addStepResult(stepResult);
        return this;
    }

    public TestProcessBuilder setCreateReport(final XdmNode report) {
        return setCreateReport(createReport("report", report));
    }

    public TestProcessBuilder setCreateReport(final BusinessReport report) {
        return setCreateReport(Collections.singletonList(report));
    }

    public TestProcessBuilder setCreateReport(final List<BusinessReport> report) {
        final ProcessStepResult<List<BusinessReport>, XMLSyntaxError> stepResult = new ProcessStepResult<>(CreateReportsAction.KEY);
        stepResult.setResult(new Result<>(report, Collections.emptyList()));
        stepResult.setReport(new XVRLReport());
        this.process.addStepResult(stepResult);
        return this;
    }

    public Process build() {
        return this.process;
    }

    public TestProcessBuilder parse(final Input input) {
        final ProcessStepResult<XdmNode, XMLSyntaxError> stepResult = parseInput(input);
        this.process.addStepResult(stepResult);
        return this;
    }

    public TestProcessBuilder setScenario(final Scenario scenario) {
        final ProcessStepResult<Scenario, String> stepResult = new ProcessStepResult<>(ScenarioSelectionAction.KEY);
        stepResult.setResult(new Result<>(scenario));
        stepResult.setReport(new XVRLReport());
        process.getProcessStepResults().removeIf(r -> r.getKey().equals(ScenarioSelectionAction.KEY));
        this.process.addStepResult(stepResult);
        return this;
    }

    public TestProcessBuilder schematronValid() {
        final ValidationResultsSchematron v = new ValidationResultsSchematron();
        final Results results = new Results();
        results.setSchematronOutput(new SchematronOutput());
        v.setResults(results);
        return setSchematronResult(Collections.singletonList(v));
    }

    public TestProcessBuilder schematronInvalid() {
        final ValidationResultsSchematron v = new ValidationResultsSchematron();
        final Results results = new Results();
        results.setSchematronOutput(new SchematronOutput());
        results.getSchematronOutput().getActivePatternAndFiredRuleAndFailedAssert().add(new FailedAssert());
        v.setResults(results);
        return setSchematronResult(Collections.singletonList(v));
    }

    public TestProcessBuilder setDummyReport() {
        return setCreateReport(createReport("report", Helper.load(Helper.Simple.SIMPLE_VALID)));
    }
}
