/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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
        if (results.isStopped() && results.getParserResult().isValid()) {
            // xml wurde aus irgendwelchen Gründen nicht korrekt verarbeitet, dann lassen wir es als undefined
            return;
        }
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

    private static boolean isSchematronValid(final Bag results) {
        return !hasSchematronErrors(results);
    }

    private static boolean hasSchematronErrors(final Bag results) {
        return results.getReportInput().getValidationResultsSchematron().stream().map(e -> e.getResults().getSchematronOutput())
                .flatMap(e -> e.getActivePatternAndFiredRuleAndFailedAssert().stream()).anyMatch(FailedAssert.class::isInstance);
    }

    private static void evaluateAcceptanceMatch(final Bag results, final XPathSelector selector) {
        try {
            selector.setContextItem(results.getReport());
            results.setAcceptStatus(selector.effectiveBooleanValue() ? AcceptRecommendation.ACCEPTABLE : AcceptRecommendation.REJECT);
        } catch (final SaxonApiException e) {
            final String msg = String.format("Error evaluating accept recommendation: %s", selector.getUnderlyingXPathContext().toString());
            log.error(msg, e);
            results.stopProcessing(msg);
        }
    }

    private static boolean preCondtionsMatch(final Bag results) {
        return results.getReport() != null && results.getSchemaValidationResult() != null && results.getScenarioSelectionResult() != null;
    }

}
