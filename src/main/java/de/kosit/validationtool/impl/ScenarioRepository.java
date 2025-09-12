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

package de.kosit.validationtool.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.model.Result;
import lombok.extern.slf4j.Slf4j;
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

    public static final String DEFAULT = "default";

    public static final String DEFAULT_ID = DEFAULT + "_1";

    private final List<Configuration> configuration;

    public ScenarioRepository(final Configuration... configuration) {
        if (configuration.length == 0) {
            throw new IllegalArgumentException("Must provide at least one configuration");
        }
        this.configuration = Arrays.asList(configuration);
        this.configuration.forEach(v -> log.info("Loaded scenarios for {} by {} from {}.", v.getName(), v.getAuthor(), v.getDate()));
        log.info("The following scenarios are available:\n{}", summarizeScenarios());
    }

    public Scenario getFallbackScenario() {
        if (this.configuration.size() > 1) {
            log.warn("Multiple configurations found. Using fallback scenario from first configuration");
        }
        return this.configuration.get(0).getFallbackScenario();
    }

    public List<Scenario> getScenarios() {
        return this.configuration.stream().flatMap(c -> c.getScenarios().stream()).collect(Collectors.toList());
    }

    private String summarizeScenarios() {
        final StringBuilder b = new StringBuilder();
        getScenarios().forEach(s -> b.append(s.getName()).append('\n'));
        return b.toString();
    }

    /**
     * Determine the matching Scenario for the provided input document
     *
     * @param document input document
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
            result = new Result<>(getFallbackScenario(), Collections.singleton("More than one scenario matches the specified document"));
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
