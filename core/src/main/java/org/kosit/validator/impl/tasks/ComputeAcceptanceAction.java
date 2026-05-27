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

import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.builder;
import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.detection;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kosit.validator.api.AcceptRecommendation;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.model.ValidationResultsSchematron;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.xvrl.model.XVRLReport;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;

/**
 * Computes a {@link AcceptRecommendation} for this instance. This is either based on an 'acceptMatch'-configuration of
 * the active scenario or based on overall evaluation about schema and semantic (schematron) correctness of the
 * 
 * @author Andreas Penski
 */
public class ComputeAcceptanceAction implements CheckAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeAcceptanceAction.class);

    public static final Process.Key<AcceptRecommendation, XMLSyntaxError> KEY = new Process.Key<>(AcceptRecommendation.class,
            XMLSyntaxError.class);

    private static final String REPORT_NAME = "Compute Acceptance Validator";

    private static XVRLReport generateXVRLReport(final Result<AcceptRecommendation, XMLSyntaxError> currentResult) {
        if (currentResult.isValid()) {
            return builder(REPORT_NAME).add(detection().addMessage(currentResult.getObject().name())).build();
        }
        return builder(REPORT_NAME)
                .addAll(currentResult.getErrors().stream().map(e -> detection().addError(e)).collect(Collectors.toList())).build();
    }

    private static Result<AcceptRecommendation, XMLSyntaxError> evaluateSchemaAndSchematron(final Process results) {
        if (results.getResult(SchemaValidationAction.KEY).isValid() && isSchematronValid(results)) {
            return new Result<>(org.kosit.validator.api.AcceptRecommendation.ACCEPTABLE);
        }
        return new Result<>(org.kosit.validator.api.AcceptRecommendation.REJECT);
    }

    private static boolean isSchematronValid(final Process results) {
        return !hasSchematronErrors(results);
    }

    private static boolean hasSchematronErrors(final Process process) {
        final Result<List<ValidationResultsSchematron>, String> result = process.getResult(SchematronValidationAction.KEY);
        if (result != null && result.isValid()) {
            return result.getObject().stream().map(v -> v.getResults().getSchematronOutput())
                    .flatMap(s -> s.getActivePatternAndFiredRuleAndFailedAssert().stream()).anyMatch(FailedAssert.class::isInstance);
        }
        return false;
    }

    private static Result<AcceptRecommendation, XMLSyntaxError> evaluateAcceptanceMatch(final Process results,
            final XPathSelector selector) {
        try {
            final Result<List<BusinessReport>, XMLSyntaxError> reportResult = results.getResult(CreateReportsAction.KEY);
            boolean result = true;
            for (final BusinessReport report : reportResult.getObject()) {
                selector.setContextItem(report.getContent());
                result = result && selector.effectiveBooleanValue();
            }
            final AcceptRecommendation effectiveBooleanValue = result ? org.kosit.validator.api.AcceptRecommendation.ACCEPTABLE
                    : org.kosit.validator.api.AcceptRecommendation.REJECT;
            return new Result<>(effectiveBooleanValue);
        } catch (final SaxonApiException e) {
            final String msg = "Error evaluating accept recommendation: " + selector.getUnderlyingXPathContext().toString();
            LOGGER.error(msg, e);
            final XMLSyntaxError xmlSyntaxError = new XMLSyntaxError();
            xmlSyntaxError.setMessage(msg);
            return new Result<>(org.kosit.validator.api.AcceptRecommendation.REJECT, Collections.singletonList(xmlSyntaxError));
        }
    }

    private static boolean preCondtionsMatch(final Process results) {
        final Result<List<BusinessReport>, XMLSyntaxError> report = results.getResult(CreateReportsAction.KEY);
        return results.getResult(SchemaValidationAction.KEY) != null && results.getResult(ScenarioSelectionAction.KEY) != null;
    }

    @Override
    public ProcessStepResult<AcceptRecommendation, XMLSyntaxError> check(final Process process) {
        final ProcessStepResult<AcceptRecommendation, XMLSyntaxError> stepResult = new ProcessStepResult<>(KEY);
        Result<AcceptRecommendation, XMLSyntaxError> result = new Result<>(org.kosit.validator.api.AcceptRecommendation.UNDEFINED);
        if (!process.isStopped() && process.getResult(DocumentParseAction.KEY).isValid()) {
            if (preCondtionsMatch(process)) {
                final Result<Scenario, String> scenarioSelection = process.getResult(ScenarioSelectionAction.KEY);
                final Optional<XPathSelector> acceptMatch = scenarioSelection.getObject().getAcceptSelector();
                if (process.getResult(SchemaValidationAction.KEY).isValid() && acceptMatch.isPresent()) {
                    result = evaluateAcceptanceMatch(process, acceptMatch.get());
                } else {
                    result = evaluateSchemaAndSchematron(process);
                }
            } else {
                final XMLSyntaxError xmlSyntaxError = new XMLSyntaxError();
                xmlSyntaxError.setMessage("Pre-Conditions not Matched");
                result = new Result<>(org.kosit.validator.api.AcceptRecommendation.REJECT, Collections.singleton(xmlSyntaxError));
            }
        }
        stepResult.setResult(result);
        stepResult.setReport(generateXVRLReport(result));
        return stepResult;
    }

    public ComputeAcceptanceAction() {
    }
}
