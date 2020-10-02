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

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;

import org.oclc.purl.dsdl.svrl.SchematronOutput;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.CollectingErrorEventHandler;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.reportInput.ValidationResultsSchematron;
import de.kosit.validationtool.model.reportInput.ValidationResultsSchematron.Results;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Ausführung von konfigurierten Schematron Validierungen eines Szenarios.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class SchematronValidationAction implements CheckAction {

    private final URIResolver resolver;

    private final ConversionService conversionService;

    private List<ValidationResultsSchematron> validate(final Bag results, final XdmNode document, final Scenario scenario) {
        return scenario.getSchematronValidations().stream().map(v -> validate(results, document, v)).collect(Collectors.toList());
    }

    private ValidationResultsSchematron validate(final Bag results, final XdmNode document, final Scenario.Transformation validation) {
        final ValidationResultsSchematron s = new ValidationResultsSchematron();
        s.setResource(validation.getResourceType());
        try {
            final XsltTransformer transformer = validation.getExecutable().load();
            // resolving nur relative zum Repository
            transformer.setURIResolver(this.resolver);
            final CollectingErrorEventHandler e = new CollectingErrorEventHandler();
            transformer.setMessageListener(e);

            final XdmDestination result = new XdmDestination();
            transformer.setDestination(result);
            transformer.setInitialContextNode(document);
            transformer.transform();

            final ValidationResultsSchematron.Results r = new ValidationResultsSchematron.Results();
            r.setSchematronOutput(this.conversionService.readDocument(
                    new DOMSource(NodeOverNodeInfo.wrap(result.getXdmNode().getUnderlyingNode()).getOwnerDocument()),
                    SchematronOutput.class));
            s.setResults(r);

        } catch (final SaxonApiException e) {
            final String msg = String.format("Error processing schematron validation %s. Error is %s",
                    validation.getResourceType().getName(), e.getMessage());
            log.error(msg, e);
            results.addProcessingError(msg);
            s.setResults(createErrorResult());
        }
        return s;
    }

    private static Results createErrorResult() {
        final Results r = new Results();
        r.setSchematronOutput(new SchematronOutput());
        return r;
    }

    @Override
    public void check(final Bag results) {
        final CreateReportInput report = results.getReportInput();
        final List<ValidationResultsSchematron> validationResult = validate(results, results.getParserResult().getObject(),
                results.getScenarioSelectionResult().getObject());
        report.getValidationResultsSchematron().addAll(validationResult);
    }

    @Override
    public boolean isSkipped(final Bag results) {
        return hasNoSchematrons(results.getScenarioSelectionResult().getObject()) || isSchemaInvalid(results);
    }

    private static boolean isSchemaInvalid(final Bag results) {
        return results.getSchemaValidationResult() == null || results.getSchemaValidationResult().isInvalid();
    }

    private static boolean hasNoSchematrons(final Scenario object) {
        return object.getSchematronValidations().isEmpty();
    }
}
