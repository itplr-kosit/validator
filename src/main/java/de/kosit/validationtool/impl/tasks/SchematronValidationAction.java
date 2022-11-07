/*
 * Copyright 2017-2021  Koordinierungsstelle für IT-Standards (KoSIT)
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

import static de.kosit.validationtool.impl.xvrl.XVRLReportBuilder.detection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.transform.dom.DOMSource;

import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.FiredRule;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.oclc.purl.dsdl.svrl.Text;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.CollectingErrorEventHandler;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.Scenario.Transformation;
import de.kosit.validationtool.impl.model.ProcessStepResult;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.xvrl.XVRLReportBuilder;
import de.kosit.validationtool.model.ValidationResultsSchematron;
import de.kosit.validationtool.model.ValidationResultsSchematron.Results;
import de.kosit.validationtool.model.XMLSyntaxError;
import de.kosit.validationtool.model.xvrl.XVRLReport;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Ausführung von konfigurierten Schematron Validierungen eines Szenarios.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class SchematronValidationAction implements CheckAction {

    public static final Process.Key<List<ValidationResultsSchematron>, String> KEY = new Process.Key<>(null, String.class);

    private static final String REPORT_NAME = "Schematron Validator";

    private final ConversionService conversionService;

    private final List<String> errorMessages = new ArrayList<>();

    private static Results createErrorResult(final String msg) {
        final Results results = new Results();
        final SchematronOutput schematronOutput = new SchematronOutput();
        final FailedAssert failedAssert = new FailedAssert();
        final Text errorText = new Text();
        errorText.getContent().add(msg);
        failedAssert.setText(errorText);
        schematronOutput.getActivePatternAndFiredRuleAndFailedAssert().add(failedAssert);
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
        return list.stream().filter(e -> e.getClass().equals(type)).map(e -> (T) e);
    }

    private static List<XVRLReport> generateXVRLReport(final List<ValidationResultsSchematron> validationResult) {
        return validationResult.stream().map(e -> {
            final XVRLReportBuilder builder = XVRLReportBuilder.builder(REPORT_NAME);
            builder.addSchema(e.getResource());
            final SchematronOutput schematronOutput = e.getResults().getSchematronOutput();
            filter(schematronOutput.getActivePatternAndFiredRuleAndFailedAssert(), FailedAssert.class).map(f -> detection().add(f))
                    .forEach(builder::add);
            filter(schematronOutput.getActivePatternAndFiredRuleAndFailedAssert(), ActivePattern.class).map(f -> detection().add(f))
                    .forEach(builder::add);
            filter(schematronOutput.getActivePatternAndFiredRuleAndFailedAssert(), FiredRule.class).map(f -> detection().add(f))
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
            // resolving nur relative zum Repository
            transformer.setURIResolver(scenario.getUriResolver());
            final CollectingErrorEventHandler collectingErrorEventHandler = new CollectingErrorEventHandler();
            transformer.setMessageListener(collectingErrorEventHandler);

            final XdmDestination result = new XdmDestination();
            transformer.setDestination(result);
            transformer.setInitialContextNode(document);
            transformer.transform();

            final ValidationResultsSchematron.Results r = new ValidationResultsSchematron.Results();
            r.setSchematronOutput(this.conversionService.readDocument(
                    new DOMSource(NodeOverNodeInfo.wrap(result.getXdmNode().getUnderlyingNode()).getOwnerDocument()),
                    SchematronOutput.class));
            validationResultsSchematron.setResults(r);

        } catch (final SaxonApiException e) {
            final String msg = String.format("Error processing schematron validation '%s'. Error is '%s'",
                    validation.getResourceType().getName(), e.getMessage());
            log.error(msg, e);
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
}
