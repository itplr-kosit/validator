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
package de.kosit.validationtool.impl.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.ScenarioRepository;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import net.sf.saxon.s9api.XdmNode;

/**
 * Identifies the scenario corresponding to the input, if one is configured. Sets the fallback scenario if none could be
 * identified.
 *
 * @author Andreas Penski
 */
public class ScenarioSelectionAction implements CheckAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioSelectionAction.class);

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
            LOGGER.info("Scenario {} identified for {}", scenarioTypeResult.getObject().getName(), results.getInput().getName());
        } else {
            LOGGER.info("No valid scenario configuration found for {}", results.getInput().getName());
        }
    }

    private Result<Scenario, String> determineScenario(final XdmNode document) {
        final Result<Scenario, String> result = this.repository.selectScenario(document);
        if (result.isInvalid()) {
            return new Result<>(this.repository.getFallbackScenario());
        }
        return result;
    }

    public ScenarioSelectionAction(final ScenarioRepository repository) {
        this.repository = repository;
    }
}
