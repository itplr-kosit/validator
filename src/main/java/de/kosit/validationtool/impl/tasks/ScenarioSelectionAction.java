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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.ScenarioRepository;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.CreateReportInput;

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

    private final ScenarioRepository repository;

    @Override
    public void check(final Bag results) {
        final CreateReportInput report = results.getReportInput();
        final Result<Scenario, String> scenarioTypeResult;

        if (results.getParserResult().isValid()) {
            scenarioTypeResult = determineScenario(results.getParserResult().getObject());
        } else {
            scenarioTypeResult = new Result<>(this.repository.getFallbackScenario());
        }
        results.setScenarioSelectionResult(scenarioTypeResult);
        if (!scenarioTypeResult.getObject().isFallback()) {
            report.setScenario(scenarioTypeResult.getObject().getConfiguration());
            log.info("Scenario {} identified for {}", scenarioTypeResult.getObject().getName(), results.getInput().getName());
        } else {
            log.error("No valid scenario configuration found for {}", results.getInput().getName());
        }
    }

    private Result<Scenario, String> determineScenario(final XdmNode document) {
        final Result<Scenario, String> result = this.repository.selectScenario(document);
        if (result.isInvalid()) {
            return new Result<>(this.repository.getFallbackScenario());
        }
        return result;
    }

}
