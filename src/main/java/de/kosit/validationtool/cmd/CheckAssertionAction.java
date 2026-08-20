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
package de.kosit.validationtool.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Verifies the report using the provided assertions. This {@link CheckAction} serves to verify the validation scenarios
 * provided by KoSIT and the artifacts contained therein.
 * 
 * @author Andreas Penski
 */
class CheckAssertionAction implements CheckAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckAssertionAction.class);

    private final Assertions assertions;

    private final Processor processor;

    private Map<String, List<AssertionType>> mappedAssertions;

    private static boolean matches(final String key, final String name) {
        return key.startsWith(name) || (name + ".xml").endsWith(key);
    }

    @Override
    public void check(final Bag results) {
        LOGGER.info("Checking assertions for {}", results.getInput().getName());
        final List<AssertionType> toCheck = findAssertions(results.getName());
        final List<String> errors = new ArrayList<>();
        if (toCheck != null && !toCheck.isEmpty()) {
            final XdmNode node = results.getReport();
            toCheck.forEach(a -> {
                if (!check(node, a)) {
                    LOGGER.error("Assertion mismatch: {}", a.getValue());
                    errors.add(a.getValue());
                }
            });
            if (errors.isEmpty()) {
                LOGGER.info("{} assertions successfully verified for {}", toCheck.size(), results.getName());
            } else {
                LOGGER.warn("{} assertion of {} failed while checking {}", errors.size(), toCheck.size(), results.getName());
            }
            results.setAssertionResult(new Result<>(toCheck.size(), errors));
        } else {
            LOGGER.warn("Can not find assertions for {}", results.getName());
        }
    }

    private List<AssertionType> findAssertions(final String name) {
        return getMapped().entrySet().stream().filter(e -> matches(e.getKey(), name)).map(Map.Entry::getValue).findFirst().orElse(null);
    }

    private boolean check(final XdmNode document, final AssertionType assertion) {
        try {
            final XPathSelector selector = createSelector(assertion);
            selector.setContextItem(document);
            return selector.effectiveBooleanValue();
        } catch (final SaxonApiException e) {
            LOGGER.error("Error evaluating assertion {} for {}", assertion.getTest(), assertion.getReportDoc(), e);
        }
        return false;
    }

    private XPathSelector createSelector(final AssertionType assertion) {
        try {
            final XPathCompiler compiler = getProcessor().newXPathCompiler();
            assertions.getNamespace().forEach(ns -> compiler.declareNamespace(ns.getPrefix(), ns.getValue()));
            return compiler.compile(assertion.getTest()).load();
        } catch (final SaxonApiException e) {
            throw new IllegalStateException("Can not compile xpath match expression \'"
                    + (StringUtils.isNotBlank(assertion.getTest()) ? assertion.getTest() : "EMPTY EXPRESSION") + "\'", e);
        }
    }

    private Map<String, List<AssertionType>> getMapped() {
        if (mappedAssertions == null) {
            mappedAssertions = new HashMap<>();
            for (final AssertionType assertionType : assertions.getAssertion()) {
                final List<AssertionType> list = mappedAssertions.computeIfAbsent(assertionType.getReportDoc(), k -> new ArrayList<>());
                list.add(assertionType);
            }
        }
        return mappedAssertions;
    }

    public CheckAssertionAction(final Assertions assertions, final Processor processor) {
        this.assertions = assertions;
        this.processor = processor;
    }

    private Processor getProcessor() {
        return this.processor;
    }
}
