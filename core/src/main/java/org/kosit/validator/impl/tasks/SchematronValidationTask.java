package org.kosit.validator.impl.tasks;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.dom.DOMSource;

import org.kosit.base.error.SimpleError;
import org.kosit.svrl.impl.SvrlConverter;
import org.kosit.validator.impl.CollectingErrorEventHandler;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.Scenario.Transformation;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.model.ValidationResultsSchematron;
import org.kosit.validator.xvrl.XvrlDetectionBuilder;
import org.kosit.validator.xvrl.XvrlReportBuilder;
import org.kosit.xvrl.model.XvrlReport;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.oclc.purl.dsdl.svrl.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Executes the configured Schematron validations of a scenario.
 *
 * @author Andreas Penski
 */
public class SchematronValidationTask implements CheckTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchematronValidationTask.class);

    public static final Process.ProcessKey<List<ValidationResultsSchematron>, String> KEY = new Process.ProcessKey<>(null, String.class);

    private static final String REPORT_NAME = "Schematron Validator";

    private final List<String> errorMessages = new ArrayList<>();

    private static SchematronOutputType createErrorResult(final String msg) {
        final SchematronOutputType schematronOutput = new SchematronOutputType();
        final FailedAssert failedAssert = new FailedAssert();

        final Text errorText = new Text();
        errorText.getContent().add(msg);
        failedAssert.setText(errorText);

        schematronOutput.getActivePatternOrActiveGroupAndFiredRule().add(failedAssert);
        return schematronOutput;
    }

    private static boolean isSchemaInvalid(final Process results) {
        final SingleProcessingResult<Boolean, SimpleError> result = results.getResult(SchemaValidationTask.KEY);
        return result == null || result.isInvalid();
    }

    private static boolean hasNoSchematrons(final Scenario object) {
        return object.getSchematronValidations().isEmpty();
    }

    private static List<XvrlReport> generateXvrlReport(final List<ValidationResultsSchematron> validationResult) {
        return validationResult.stream().map(e -> {
            final XvrlReportBuilder reportBuilder = XvrlReportBuilder.builder(REPORT_NAME);
            reportBuilder.addSchema(e.getResource());

            final SchematronOutputType schematronOutput = e.getResults();
            for (final var f : schematronOutput.getFailedAsserts())
                reportBuilder.addDetection(XvrlDetectionBuilder.builder().add(f));
            for (final var f : schematronOutput.getActivePatterns())
                reportBuilder.addDetection(XvrlDetectionBuilder.builder().add(f));
            for (final var f : schematronOutput.getFiredRules())
                reportBuilder.addDetection(XvrlDetectionBuilder.builder().add(f));
            return reportBuilder.build();
        }).toList();
    }

    public SchematronValidationTask() {
    }

    private List<ValidationResultsSchematron> validate(final Process results, final XdmNode document, final Scenario scenario) {
        return scenario.getSchematronValidations().stream().map(v -> validate(scenario, results, document, v)).toList();
    }

    private ValidationResultsSchematron validate(final Scenario scenario, final Process process, final XdmNode document,
            final Transformation validation) {
        final ValidationResultsSchematron validationResultsSchematron = new ValidationResultsSchematron();
        validationResultsSchematron.setResource(validation.getResourceType());
        try {
            final XsltTransformer transformer = validation.getExecutable().load();
            // resolving only relative to the repository
            transformer.setResourceResolver(scenario.getUriResolver());

            final CollectingErrorEventHandler collectingErrorEventHandler = new CollectingErrorEventHandler();
            transformer.setMessageHandler(collectingErrorEventHandler);

            final XdmDestination result = new XdmDestination();
            transformer.setDestination(result);
            transformer.setInitialContextNode(document);
            transformer.transform();

            final var so = new SvrlConverter()
                    .readXml(new DOMSource(NodeOverNodeInfo.wrap(result.getXdmNode().getUnderlyingNode()).getOwnerDocument()));
            validationResultsSchematron.setResults(so);
        } catch (final SaxonApiException e) {
            final String msg = "Error processing schematron validation '" + validation.getResourceType().getName() + "'. Error is '"
                    + e.getMessage() + "'";
            LOGGER.error(msg, e);
            this.errorMessages.add(msg);
            process.setStopped(true);
            validationResultsSchematron.setResults(createErrorResult(msg));
        }
        return validationResultsSchematron;
    }

    @Override
    public ProcessStepResult<List<ValidationResultsSchematron>, String> check(final Process process) {
        final SingleProcessingResult<XdmNode, SimpleError> parseResult = process.getResult(DocumentParseTask.KEY);
        final SingleProcessingResult<Scenario, String> scenarioResult = process.getResult(ScenarioSelectionTask.KEY);
        final List<ValidationResultsSchematron> validationResult = validate(process, parseResult.getObject(), scenarioResult.getObject());
        final ProcessStepResult<List<ValidationResultsSchematron>, String> processStepResult = new ProcessStepResult<>(KEY);
        processStepResult.setResult(new SingleProcessingResult<>(validationResult, this.errorMessages));
        processStepResult.addReports(generateXvrlReport(validationResult));
        return processStepResult;
    }

    @Override
    public boolean isSkipped(final Process results) {
        final SingleProcessingResult<Scenario, String> result = results.getResult(ScenarioSelectionTask.KEY);
        return hasNoSchematrons(result.getObject()) || isSchemaInvalid(results);
    }
}
