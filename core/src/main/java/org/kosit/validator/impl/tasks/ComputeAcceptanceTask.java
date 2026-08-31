package org.kosit.validator.impl.tasks;

import static org.kosit.validator.xvrl.XvrlReportBuilder.builder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.kosit.base.error.DefaultSimpleError;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.api.xvrl.compact.AcceptRecommendation;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.model.ValidationResultsSchematron;
import org.kosit.validator.xvrl.XvrlDetectionBuilder;
import org.kosit.xvrl.model.XvrlReport;
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
public class ComputeAcceptanceTask implements CheckTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeAcceptanceTask.class);

    public static final Process.ProcessKey<AcceptRecommendation, SimpleError> KEY = new Process.ProcessKey<>(AcceptRecommendation.class,
            SimpleError.class);

    private static final String REPORT_NAME = "Compute Acceptance Validator";

    private static XvrlReport generateXvrlReport(final SingleProcessingResult<AcceptRecommendation, SimpleError> currentResult) {
        if (currentResult.isValid()) {
            return builder(REPORT_NAME).addDetection(XvrlDetectionBuilder.builder().addMessage(currentResult.getObject().getID())).build();
        }
        return builder(REPORT_NAME)
                .addDetections(currentResult.getErrors().stream().map(e -> XvrlDetectionBuilder.builderError().addError(e))).build();
    }

    private static SingleProcessingResult<AcceptRecommendation, SimpleError> evaluateSchemaAndSchematron(final Process results) {
        if (results.getResult(SchemaValidationTask.KEY).isValid() && isSchematronValid(results)) {
            return new SingleProcessingResult<>(AcceptRecommendation.ACCEPTABLE);
        }
        return new SingleProcessingResult<>(AcceptRecommendation.REJECT);
    }

    private static boolean isSchematronValid(final Process results) {
        return !hasSchematronErrors(results);
    }

    private static boolean hasSchematronErrors(final Process process) {
        final SingleProcessingResult<List<ValidationResultsSchematron>, String> result = process.getResult(SchematronValidationTask.KEY);
        if (result != null && result.isValid()) {
            return result.getObject().stream().map(ValidationResultsSchematron::getResults)
                    .flatMap(s -> s.getActivePatternOrActiveGroupAndFiredRule().stream()).anyMatch(FailedAssert.class::isInstance);
        }
        return false;
    }

    private static SingleProcessingResult<AcceptRecommendation, SimpleError> evaluateAcceptanceMatch(final Process results,
            final XPathSelector selector) {
        try {
            final SingleProcessingResult<List<BusinessReport>, SimpleError> reportResult = results.getResult(CreateReportsTask.KEY);
            boolean result = true;
            for (final BusinessReport report : reportResult.getObject()) {
                selector.setContextItem(report.getContent());
                result = result && selector.effectiveBooleanValue();
            }
            final AcceptRecommendation effectiveBooleanValue = result ? AcceptRecommendation.ACCEPTABLE : AcceptRecommendation.REJECT;
            return new SingleProcessingResult<>(effectiveBooleanValue);
        } catch (final SaxonApiException e) {
            final String msg = "Error evaluating accept recommendation: " + selector.getUnderlyingXPathContext().toString();
            LOGGER.error(msg, e);
            final SimpleError error = DefaultSimpleError.builderError().message(msg).location(e.getSystemId(), e.getLineNumber(), 0)
                    .linkedException(e).build();
            return new SingleProcessingResult<>(AcceptRecommendation.REJECT, Collections.singletonList(error));
        }
    }

    private static boolean preCondtionsMatch(final Process results) {
        final SingleProcessingResult<List<BusinessReport>, SimpleError> report = results.getResult(CreateReportsTask.KEY);
        return results.getResult(SchemaValidationTask.KEY) != null && results.getResult(ScenarioSelectionTask.KEY) != null;
    }

    @Override
    public ProcessStepResult<AcceptRecommendation, SimpleError> check(final Process process) {
        final ProcessStepResult<AcceptRecommendation, SimpleError> stepResult = new ProcessStepResult<>(KEY);
        SingleProcessingResult<AcceptRecommendation, SimpleError> result = new SingleProcessingResult<>(AcceptRecommendation.UNDEFINED);
        if (!process.isStopped() && process.getResult(DocumentParseTask.KEY).isValid()) {
            if (preCondtionsMatch(process)) {
                final SingleProcessingResult<Scenario, String> scenarioSelection = process.getResult(ScenarioSelectionTask.KEY);
                final Optional<XPathSelector> acceptMatch = scenarioSelection.getObject().getAcceptSelector();
                if (process.getResult(SchemaValidationTask.KEY).isValid() && acceptMatch.isPresent()) {
                    result = evaluateAcceptanceMatch(process, acceptMatch.get());
                } else {
                    result = evaluateSchemaAndSchematron(process);
                }
            } else {
                final SimpleError error = DefaultSimpleError.builderError().message("Pre-Conditions not Matched").build();
                result = new SingleProcessingResult<>(AcceptRecommendation.REJECT, Collections.singletonList(error));
            }
        }
        stepResult.setResult(result);
        stepResult.setReport(generateXvrlReport(result));
        return stepResult;
    }

    public ComputeAcceptanceTask() {
    }
}
