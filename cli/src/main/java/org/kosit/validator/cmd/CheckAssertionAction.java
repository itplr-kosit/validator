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

package org.kosit.validator.cmd;

import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.builder;
import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.kosit.validator.cmd.assertions.AssertionType;
import org.kosit.validator.cmd.assertions.Assertions;
import org.kosit.validator.impl.ConversionService;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.tasks.CheckAction;
import org.kosit.validator.impl.tasks.XvrlSerializer;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.validator.model.xvrl.XVRLReport;

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

class CheckAssertionAction implements CheckAction {

    public static final Process.Key<Integer, XMLSyntaxError> KEY = new Process.Key<>(Integer.class, XMLSyntaxError.class);

    private static final String REPORT_NAME = "Assertions Validator";

    private final Assertions assertions;

    @Getter(AccessLevel.PRIVATE)
    private final Processor processor;

    private final XvrlSerializer xvrlSerializer;

    private Map<String, List<AssertionType>> mappedAssertions;

    public CheckAssertionAction(final Assertions assertions, final Processor processor, final ConversionService conversionService) {
        this.assertions = assertions;
        this.processor = processor;
        this.xvrlSerializer = new XvrlSerializer(conversionService, processor);
    }

    private static boolean matches(final String key, final String name) {
        return key.startsWith(name) || (name + ".xml").endsWith(key);
    }

    private static XVRLReport generateXVRLReport(final Result<Integer, XMLSyntaxError> assertionResult) {
        if (assertionResult.isValid()) {
            return builder(REPORT_NAME).add(detection().addMessage("Assertion succesfully checked")).build();
        }
        return builder(REPORT_NAME).addAll(assertionResult.getErrors().stream().map(e -> detection().addError(e))).build();
    }

    @Override
    public ProcessStepResult<Integer, XMLSyntaxError> check(final Process results) {
        log.info("Checking assertions for {}", results.getInput().getName());

        final ProcessStepResult<Integer, XMLSyntaxError> processStepResult = new ProcessStepResult<>(KEY);

        final List<AssertionType> toCheck = findAssertions(results.getName());
        final List<XMLSyntaxError> errors = new ArrayList<>();
        final Result<Integer, XMLSyntaxError> assertionResult;
        if (toCheck != null && !toCheck.isEmpty()) {
            try {
                final XdmNode report = this.xvrlSerializer.serialize(results.getXvrlReportSummary());
                toCheck.forEach(e -> {
                    final boolean result = check(report, e);
                    if (!result) {
                        log.error("Assertion mismatch: {}", e.getValue());
                        final XMLSyntaxError error = new XMLSyntaxError();
                        error.setMessage(e.getValue());
                        errors.add(error);
                    }
                });

            } catch (final SaxonApiException | JAXBException e) {
                // TODO
            }
            if (errors.isEmpty()) {
                log.info("{} assertions successfully verified for {}", toCheck.size(), results.getName());
            } else {
                log.warn("{} assertion of {} failed while checking {}", errors.size(), toCheck.size(), results.getName());
            }
            assertionResult = new Result<>(toCheck.size(), errors);

        } else {
            log.warn("Can not find assertions for {}", results.getName());
            final XMLSyntaxError error = new XMLSyntaxError();
            error.setMessage(String.format("Can not find assertions for %s", results.getName()));
            errors.add(error);
            assertionResult = new Result<>(-1, errors);
        }
        processStepResult.setResult(assertionResult);
        processStepResult.setReport(generateXVRLReport(assertionResult));
        return processStepResult;
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
            log.error("Error evaluating assertion {} for {}", assertion.getTest(), assertion.getReportDoc(), e);
        }
        return false;

    }

    private XPathSelector createSelector(final AssertionType assertion) throws SaxonApiException {
        try {
            final XPathCompiler compiler = getProcessor().newXPathCompiler();
            this.assertions.getNamespace().forEach(ns -> compiler.declareNamespace(ns.getPrefix(), ns.getValue()));
            return compiler.compile(assertion.getTest()).load();
        } catch (final SaxonApiException e) {
            throw new IllegalStateException(String.format("Can not compile xpath match expression '%s'",
                    StringUtils.isNotBlank(assertion.getTest()) ? assertion.getTest() : "EMPTY EXPRESSION"), e);
        }
    }

    private Map<String, List<AssertionType>> getMapped() {
        if (this.mappedAssertions == null) {
            this.mappedAssertions = new HashMap<>();
            for (final AssertionType assertionType : this.assertions.getAssertion()) {
                final List<AssertionType> list = this.mappedAssertions.computeIfAbsent(assertionType.getReportDoc(),
                        k -> new ArrayList<>());
                list.add(assertionType);
            }
        }
        return this.mappedAssertions;
    }
}
