package org.kosit.validator.impl.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.transform.dom.DOMSource;

import org.kosit.svrl.impl.SvrlConversionService;
import org.kosit.validator.impl.CollectingErrorEventHandler;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.Scenario.Transformation;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.xvrl.XVRLReportBuilder;
import org.kosit.validator.model.ValidationResultsSchematron;
import org.kosit.validator.model.ValidationResultsSchematron.Results;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.xvrl.model.XVRLReport;
import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.FiredRule;
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
public class SchematronValidationAction implements CheckAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchematronValidationAction.class);

    public static final Process.Key<List<ValidationResultsSchematron>, String> KEY = new Process.Key<>(null, String.class);

    private static final String REPORT_NAME = "Schematron Validator";

    private final SvrlConversionService conversionService;

    private final List<String> errorMessages = new ArrayList<>();

    private static Results createErrorResult(final String msg) {
        final Results results = new Results();
        final SchematronOutputType schematronOutput = new SchematronOutputType();
        final FailedAssert failedAssert = new FailedAssert();
        final Text errorText = new Text();
        errorText.getContent().add(msg);
        failedAssert.setText(errorText);
        schematronOutput.getActivePatternOrActiveGroupAndFiredRule().add(failedAssert);
        results.setSchematronOutput(schematronOutput);
        return results;
    }

    private static boolean isSchemaInvalid(final Process results) {
        final Result<Boolean, XMLSyntaxError> result = results.getResult(SchemaValidationAction.KEY);
        return result == null || result.isInvalid();
    }

    private static boolean hasNoSchematrons(final Scenario object) {
        return object.getSchematronValidations().isEmpty();
    }

    private static <T> Stream<T> filter(final List<Serializable> list, final Class<T> type) {
        return list.stream().filter(e -> e.getClass().equals(type)).map(type::cast);
    }

    private static List<XVRLReport> generateXVRLReport(final List<ValidationResultsSchematron> validationResult) {
        return validationResult.stream().map(e -> {
            final XVRLReportBuilder builder = XVRLReportBuilder.builder(REPORT_NAME);
            builder.addSchema(e.getResource());
            final SchematronOutputType schematronOutput = e.getResults().getSchematronOutput();
            filter(schematronOutput.getActivePatternOrActiveGroupAndFiredRule(), FailedAssert.class).map(f -> detection().add(f))
                    .forEach(builder::add);
            filter(schematronOutput.getActivePatternOrActiveGroupAndFiredRule(), ActivePattern.class).map(f -> detection().add(f))
                    .forEach(builder::add);
            filter(schematronOutput.getActivePatternOrActiveGroupAndFiredRule(), FiredRule.class).map(f -> detection().add(f))
                    .forEach(builder::add);
            return builder.build();
        }).collect(Collectors.toList());
    }

    private List<ValidationResultsSchematron> validate(final Process results, final XdmNode document, final Scenario scenario) {
        return scenario.getSchematronValidations().stream().map(v -> validate(scenario, results, document, v)).collect(Collectors.toList());
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
            transformer.setMessageListener(collectingErrorEventHandler);
            final XdmDestination result = new XdmDestination();
            transformer.setDestination(result);
            transformer.setInitialContextNode(document);
            transformer.transform();
            final ValidationResultsSchematron.Results r = new ValidationResultsSchematron.Results();
            r.setSchematronOutput(this.conversionService.readXml(
                    new DOMSource(NodeOverNodeInfo.wrap(result.getXdmNode().getUnderlyingNode()).getOwnerDocument()),
                    SchematronOutputType.class));
            validationResultsSchematron.setResults(r);
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
        final Result<XdmNode, XMLSyntaxError> parseResult = process.getResult(DocumentParseAction.KEY);
        final Result<Scenario, String> scenarioResult = process.getResult(ScenarioSelectionAction.KEY);
        final List<ValidationResultsSchematron> validationResult = validate(process, parseResult.getObject(), scenarioResult.getObject());
        final ProcessStepResult<List<ValidationResultsSchematron>, String> processStepResult = new ProcessStepResult<>(KEY);
        processStepResult.setResult(new Result<>(validationResult, this.errorMessages));
        processStepResult.addReports(generateXVRLReport(validationResult));
        return processStepResult;
    }

    @Override
    public boolean isSkipped(final Process results) {
        final Result<Scenario, String> result = results.getResult(ScenarioSelectionAction.KEY);
        return hasNoSchematrons(result.getObject()) || isSchemaInvalid(results);
    }

    public SchematronValidationAction(final SvrlConversionService conversionService) {
        this.conversionService = conversionService;
    }
}
