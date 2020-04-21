package de.kosit.validationtool.impl.tasks;

import java.util.Optional;

import org.oclc.purl.dsdl.svrl.FailedAssert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.AcceptRecommendation;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;

/**
 * Computes a {@link AcceptRecommendation} for this instance. This is either based on an 'acceptMatch'-configuration of
 * the active scenario or based on overall evaluation about schema and semantic (schematron) correctness of the
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class ComputeAcceptanceAction implements CheckAction {

    @Override
    public void check(final Bag results) {
        if (preCondtionsMatch(results)) {
            final Optional<XPathSelector> acceptMatch = results.getScenarioSelectionResult().getObject().getAcceptSelector();
            if (results.getSchemaValidationResult().isValid() && acceptMatch.isPresent()) {
                evaluateAcceptanceMatch(results, acceptMatch.get());
            } else {
                evaluateSchemaAndSchematron(results);
            }
        } else {
            results.setAcceptStatus(AcceptRecommendation.REJECT);
        }
    }

    private void evaluateSchemaAndSchematron(final Bag results) {
        if (results.getSchemaValidationResult().isValid() && isSchematronValid(results)) {
            results.setAcceptStatus(AcceptRecommendation.ACCEPTABLE);
        } else {
            results.setAcceptStatus(AcceptRecommendation.REJECT);
        }
    }

    private boolean isSchematronValid(final Bag results) {
        return !hasSchematronErrors(results);
    }

    private boolean hasSchematronErrors(final Bag results) {
        return results.getReportInput().getValidationResultsSchematron().stream().map(e -> e.getResults().getSchematronOutput())
                .flatMap(e -> e.getActivePatternAndFiredRuleAndFailedAssert().stream()).anyMatch(FailedAssert.class::isInstance);
    }

    private static void evaluateAcceptanceMatch(final Bag results, final XPathSelector selector) {
        try {
            selector.setContextItem(results.getReport());
            results.setAcceptStatus(selector.effectiveBooleanValue() ? AcceptRecommendation.ACCEPTABLE : AcceptRecommendation.REJECT);
        } catch (final SaxonApiException e) {
            final String msg = "Error evaluating accept recommendation: %s";
            log.error(msg);
            results.addProcessingError(msg);
        }
    }

    private static boolean preCondtionsMatch(final Bag results) {
        return results.getReport() != null && results.getSchemaValidationResult() != null && results.getScenarioSelectionResult() != null;
    }

}
