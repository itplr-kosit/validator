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
import de.kosit.validationtool.model.reportInput.ProcessingError;
import de.kosit.validationtool.model.scenarios.ScenarioType;

/**
 * Identifiziert das der Eingabe entsprechende Szenario, sofern eines konfiguriert ist.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public class ScenarioSelectionAction implements CheckAction {

    private final ScenarioRepository repository;

    @Override
    public void check(Bag results) {
        final CreateReportInput report = results.getReportInput();
        final Result<ScenarioType, String> scenarioTypeResult = repository.selectScenario(results.getParserResult().getObject());
        results.setScenarioSelectionResult(scenarioTypeResult);
        if (scenarioTypeResult.isValid()) {
            final ScenarioType scenario = scenarioTypeResult.getObject();
            report.setScenario(scenario);
        } else {
            if (report.getProcessingError() == null) {
                report.setProcessingError(new ProcessingError());
            }
            report.getProcessingError().getError().addAll(scenarioTypeResult.getErrors());
        }
    }

    @Override
    public boolean isSkipped(Bag results) {
        return results.getParserResult().isInvalid();
    }
}
