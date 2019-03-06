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

package de.kosit.validationtool.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.cmd.assertions.AssertionType;
import de.kosit.validationtool.cmd.assertions.Assertions;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.CheckAction;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

/**
 * Überprüft den Report mittels bereitgestellter Assertions. Diese {@link CheckAction} dient der Überprüfung der von der
 * KoSIT bereitgestellten Prüfszenarien und den darin enthaltenen Artefakten.
 * 
 * @author Andreas Penski
 */
@Slf4j
@RequiredArgsConstructor
class CheckAssertionAction implements CheckAction {

    private final Assertions assertions;

    @Getter(AccessLevel.PRIVATE)
    private final Processor processor;

    private Map<String, List<AssertionType>> mappedAssertions;

    private static boolean matches(String key, String name) {
        return key.startsWith(name) || (name + ".xml").endsWith(key);
    }

    @Override
    public void check(Bag results) {
        log.info("Checking assertions for {}", results.getInput().getName());
        final List<AssertionType> toCheck = findAssertions(results.getName());
        final List<String> errors = new ArrayList<>();
        if (toCheck != null && !toCheck.isEmpty()) {
            final XdmNode node = results.getReport();
            toCheck.forEach(a -> {
                if (!check(node, a)) {
                    log.error("Assertion mismatch: {}", a.getValue());
                    errors.add(a.getValue());
                }
            });
            if (errors.isEmpty()) {
                log.info("{} assertions successfully verified for {}", toCheck.size(), results.getName());
            } else {
                log.warn("{} assertion of {} failed while checking {}", errors.size(), toCheck.size(), results.getName());
            }
            results.setAssertionResult(new Result<>(toCheck.size(), errors));
        } else {
            log.warn("Can not find assertions for {}", results.getName());
        }
    }

    private List<AssertionType> findAssertions(String name) {
        return getMapped().entrySet().stream().filter(e -> matches(e.getKey(), name)).map(Map.Entry::getValue).findFirst().orElse(null);
    }


    private boolean check(XdmNode document, AssertionType assertion) {
        try {
            final XPathSelector selector = createSelector(assertion);
            selector.setContextItem(document);
            return selector.effectiveBooleanValue();
        } catch (SaxonApiException e) {
            log.error("Error evaluating assertion {} for {}", assertion.getTest(), assertion.getReportDoc(), e);
        }
        return false;

    }

    private XPathSelector createSelector(AssertionType assertion) throws SaxonApiException {
        try {
            final XPathCompiler compiler = getProcessor().newXPathCompiler();
            assertions.getNamespace().forEach(ns -> compiler.declareNamespace(ns.getPrefix(), ns.getValue()));
            return compiler.compile(assertion.getTest()).load();
        } catch (SaxonApiException e) {
            throw new IllegalStateException(String.format("Can not compile xpath match expression '%s'",
                    StringUtils.isNotBlank(assertion.getTest()) ? assertion.getTest() : "EMPTY EXPRESSION"), e);
        }
    }

    private Map<String, List<AssertionType>> getMapped() {
        if (mappedAssertions == null) {
            mappedAssertions = new HashMap<>();
            for (AssertionType assertionType : assertions.getAssertion()) {
                List<AssertionType> list = mappedAssertions.computeIfAbsent(assertionType.getReportDoc(), k -> new ArrayList<>());
                list.add(assertionType);
            }
        }
        return mappedAssertions;
    }
}
