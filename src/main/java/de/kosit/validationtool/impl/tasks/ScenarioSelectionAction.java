/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.impl.tasks;

import lombok.RequiredArgsConstructor;

import de.kosit.validationtool.impl.ScenarioRepository;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.scenarios.ScenarioType;

import net.sf.saxon.s9api.XdmNode;

/**
 * Identifiziert das der Eingabe entsprechende Szenario, sofern eines konfiguriert ist. Setzt das Fallback-Szenario,
 * wenn keines identifiziert werden konnte.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public class ScenarioSelectionAction implements CheckAction {

    private final ScenarioRepository repository;

    @Override
    public void check(final Bag results) {
        final CreateReportInput report = results.getReportInput();
        final Result<ScenarioType, String> scenarioTypeResult;

        if (results.getParserResult().isValid()) {
            scenarioTypeResult = determineScenario(results.getParserResult().getObject());
        } else {
            scenarioTypeResult = new Result<>(repository.getFallbackScenario());
        }
        results.setScenarioSelectionResult(scenarioTypeResult);
        report.setScenario(scenarioTypeResult.getObject());
    }

    private Result<ScenarioType, String> determineScenario(final XdmNode document) {
        final Result<ScenarioType, String> result = this.repository.selectScenario(document);
        if (result.isInvalid()) {
            return new Result<>(repository.getFallbackScenario());
        }
        return result;
    }

}
