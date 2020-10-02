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

package de.kosit.validationtool.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.model.Result;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

/**
 * Repository for die aktiven Szenario einer Prüfinstanz.
 * 
 * @author Andreas Penski
 */
@Slf4j

public class ScenarioRepository {

    private final Configuration configuration;

    public ScenarioRepository(final Configuration configuration) {
        this.configuration = configuration;
        log.info("Loaded scenarios for {} by {} from {}. The following scenarios are available:\n\n{}", configuration.getName(),
                configuration.getAuthor(), configuration.getDate(), summarizeScenarios());
    }

    public Scenario getFallbackScenario() {
        return this.configuration.getFallbackScenario();
    }

    public List<Scenario> getScenarios() {
        return this.configuration.getScenarios();
    }

    private String summarizeScenarios() {
        final StringBuilder b = new StringBuilder();
        getScenarios().forEach(s -> {
            b.append(s.getName());
            b.append("\n");
        });
        return b.toString();
    }

    /**
     * Ermittelt für das gegebene Dokument das passende Szenario.
     *
     * @param document das Eingabedokument
     * @return ein Ergebnis-Objekt zur weiteren Verarbeitung
     */
    public Result<Scenario, String> selectScenario(final XdmNode document) {
        final Result<Scenario, String> result;
        final List<Scenario> collect = getScenarios().stream().filter(s -> match(document, s)).collect(Collectors.toList());
        if (collect.size() == 1) {
            result = new Result<>(collect.get(0));
        } else if (collect.isEmpty()) {
            result = new Result<>(getFallbackScenario(),
                    Collections.singleton("None of the loaded scenarios matches the specified document"));
        } else {
            result = new Result<>(getFallbackScenario(), Collections.singleton("More than on scenario matches the specified document"));
        }
        return result;

    }

    private static boolean match(final XdmNode document, final Scenario scenario) {
        try {
            final XPathSelector selector = scenario.getMatchSelector();
            selector.setContextItem(document);
            return selector.effectiveBooleanValue();
        } catch (final SaxonApiException e) {
            log.error("Error evaluating xpath expression", e);
        }
        return false;
    }

}
