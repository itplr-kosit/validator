package org.kosit.validator.impl.tasks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;

import org.conformatron.api.model.source.CTReadResource;
import org.kosit.base.error.DefaultSimpleError;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.conformatron.source.ReadResource;
import org.kosit.validator.impl.conformatron.source.Resource;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.tasks.CheckTask.Process;
import org.kosit.validator.model.ValidationResultsSchematron;
import org.kosit.xvrl.model.XvrlMetadata;
import org.kosit.xvrl.model.XvrlReport;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;

import net.sf.saxon.s9api.XdmNode;

/**
 * Utilities for creating test objects.
 *
 * @author Andreas Penski
 */
public class TestProcessBuilder {

    private Process process;

    public static TestProcessBuilder create() {
        try {
            return create(ReadResource.inMemory(Resource.utf8("someCheck", "<someXml></someXml>")));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static TestProcessBuilder create(final CTReadResource input) {
        return create(input, true);
    }

    public static TestProcessBuilder create(final CTReadResource input, final boolean parse) {
        final TestProcessBuilder builder = new TestProcessBuilder();
        builder.process = new Process(input, XvrlMetadata.builder().build());
        if (parse) {
            builder.parse(input);
        }
        builder.setScenario(TestScenarioBuilder.createDefault());
        return builder;
    }

    public static List<BusinessReport> createReport(final String id, final XdmNode node) {
        final BusinessReport r = new BusinessReport();
        r.setReport(XvrlReport.builder().build());
        r.setName(id);
        r.setContent(node);
        return Collections.singletonList(r);
    }

    public static List<BusinessReport> createReport() throws IOException {
        final XdmNode someXml = TestHelper.parseDocument(ReadResource.inMemory(Resource.utf8("someXml", "<some>xml</some>"))).getObject();
        return createReport("report", someXml);
    }

    private static ProcessStepResult<XdmNode, SimpleError> parseInput(final CTReadResource input) {
        final ProcessStepResult<XdmNode, SimpleError> stepResult = new ProcessStepResult<>(DocumentParseTask.KEY);
        stepResult.setResult(TestHelper.parseDocument(input));
        stepResult.setReport(XvrlReport.builder().build());
        return stepResult;
    }

    public TestProcessBuilder setParseResult(final CTReadResource input) {
        this.process.addStepResult(parseInput(input));
        return this;
    }

    public TestProcessBuilder setMetadata(final XvrlMetadata metadata) {
        this.process.setMetadata(metadata);
        return this;
    }

    public TestProcessBuilder schemaValid() {
        return setSchemaValidationResult(true, null);
    }

    public TestProcessBuilder schemaInvalid() {
        final SimpleError error = DefaultSimpleError.builderError().message("Default error").build();
        return setSchemaValidationResult(false, Collections.singletonList(error));

    }

    public TestProcessBuilder setSchemaValidationResult(final boolean value, final List<SimpleError> errors) {
        return setSchemaValidationResult(new SingleProcessingResult<>(value, errors));
    }

    public TestProcessBuilder setSchemaValidationResult(final SingleProcessingResult<Boolean, SimpleError> schemaResult) {
        final ProcessStepResult<Boolean, SimpleError> stepResult = new ProcessStepResult<>(SchemaValidationTask.KEY);
        stepResult.setResult(schemaResult);
        stepResult.setReport(XvrlReport.builder().build());
        this.process.addStepResult(stepResult);
        return this;
    }

    public TestProcessBuilder setSchematronResult(final List<ValidationResultsSchematron> result) {
        return setSchematronResult(new SingleProcessingResult<>(result, null));
    }

    public TestProcessBuilder setSchematronResult(
            final SingleProcessingResult<List<ValidationResultsSchematron>, String> schematronResult) {
        final ProcessStepResult<List<ValidationResultsSchematron>, String> stepResult = new ProcessStepResult<>(
                SchematronValidationTask.KEY);
        stepResult.setResult(schematronResult);
        stepResult.setReport(XvrlReport.builder().build());
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
        final ProcessStepResult<List<BusinessReport>, SimpleError> stepResult = new ProcessStepResult<>(CreateReportsTask.KEY);
        stepResult.setResult(new SingleProcessingResult<>(report, Collections.emptyList()));
        stepResult.setReport(XvrlReport.builder().build());
        this.process.addStepResult(stepResult);
        return this;
    }

    public Process build() {
        return this.process;
    }

    public TestProcessBuilder parse(final CTReadResource input) {
        final ProcessStepResult<XdmNode, SimpleError> stepResult = parseInput(input);
        this.process.addStepResult(stepResult);
        return this;
    }

    public TestProcessBuilder setScenario(final Scenario scenario) {
        final ProcessStepResult<Scenario, String> stepResult = new ProcessStepResult<>(ScenarioSelectionTask.KEY);
        stepResult.setResult(new SingleProcessingResult<>(scenario));
        stepResult.setReport(XvrlReport.builder().build());
        this.process.addStepResult(stepResult);
        return this;
    }

    public TestProcessBuilder schematronValid() {
        final ValidationResultsSchematron v = new ValidationResultsSchematron();
        v.setResults(new SchematronOutputType());
        return setSchematronResult(Collections.singletonList(v));
    }

    public TestProcessBuilder schematronInvalid() {
        final var so = new SchematronOutputType();
        so.getActivePatternOrActiveGroupAndFiredRule().add(new FailedAssert());
        final ValidationResultsSchematron v = new ValidationResultsSchematron();
        v.setResults(so);
        return setSchematronResult(Collections.singletonList(v));
    }

    public TestProcessBuilder setDummyReport() {
        return setCreateReport(createReport("report", TestHelper.load(TestHelper.Simple.SIMPLE_VALID)));
    }
}
