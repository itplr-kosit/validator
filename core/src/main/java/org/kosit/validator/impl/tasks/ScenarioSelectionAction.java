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

import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.detection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.kosit.validator.impl.ActionMetadata;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.ScenarioRepository;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.xvrl.XVRLReportBuilder;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.validator.model.xvrl.XVRLDetection;
import org.kosit.validator.model.xvrl.XVRLReport;

import net.sf.saxon.s9api.XdmNode;

/**
 * Identifiziert das der Eingabe entsprechende Szenario, sofern eines konfiguriert ist. Setzt das Fallback-Szenario,
 * wenn keines identifiziert werden konnte.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class ScenarioSelectionAction implements CheckAction {

    public static final Process.Key<Scenario, String> KEY = new Process.Key<>(Scenario.class, String.class);

    public static final ActionMetadata METADATA = new ActionMetadata("Scenario Selection", "scenario_selection");

    private final ScenarioRepository repository;

    private static XVRLReport generateXVRLReport(final Result<Scenario, String> scenarioTypeResult, final String name) {
        final XVRLReportBuilder builder = XVRLReportBuilder.builder(METADATA);
        if (scenarioTypeResult.getObject().isFallback()) {
            builder.add(detection().addError(String.format("No valid scenario configuration found for '%s'", name)).code("fallback-match"));
        } else {
            builder.add(detection()
                    .addMessage(String.format("Scenario '%s' identified for '%s'", scenarioTypeResult.getObject().getName(), name))
                    .severity(XVRLDetection.Severity.INFO).code("scenario-matched"));
            builder.add(detection().id("scenario").code(scenarioTypeResult.getObject().getName()));
        }

        return builder.build();
    }

    @Override
    public ProcessStepResult<Scenario, String> check(final Process results) {
        final Result<Scenario, String> scenarioTypeResult;

        final Result<XdmNode, XMLSyntaxError> parseResult = results.getResult(DocumentParseAction.KEY);
        if (parseResult.isValid()) {
            scenarioTypeResult = determineScenario(parseResult.getObject());
        } else {
            scenarioTypeResult = new Result<>(this.repository.getFallbackScenario());
        }
        if (!scenarioTypeResult.getObject().isFallback()) {
            log.info("Scenario '{}' identified for '{}'", scenarioTypeResult.getObject().getName(), results.getInput().getName());
        } else {
            log.info("No valid scenario configuration found for '{}'", results.getInput().getName());
        }

        final ProcessStepResult<Scenario, String> result = new ProcessStepResult<>(ScenarioSelectionAction.KEY);
        result.setResult(scenarioTypeResult);
        result.setReport(generateXVRLReport(scenarioTypeResult, results.getInput().getName()));
        return result;
    }

    private Result<Scenario, String> determineScenario(final XdmNode document) {
        final Result<Scenario, String> result = this.repository.selectScenario(document);
        if (result.isInvalid()) {
            return new Result<>(this.repository.getFallbackScenario());
        }
        return result;
    }

}
