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

import static de.kosit.validationtool.impl.xvrl.XVRLReportBuilder.builder;
import static de.kosit.validationtool.impl.xvrl.XVRLReportBuilder.detection;
import static de.kosit.validationtool.impl.xvrl.XVRLReportBuilder.supplemantal;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.xml.bind.JAXBException;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.ActionMetadata;
import de.kosit.validationtool.impl.CollectingErrorEventHandler;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.model.ProcessStepResult;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.xvrl.XVRLReportBuilder;
import de.kosit.validationtool.model.XMLSyntaxError;
import de.kosit.validationtool.model.scenarios.ResourceType;
import de.kosit.validationtool.model.xvrl.XVRLReport;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Erzeugt die Reports auf Basis der gesammelten Informationen über den Prüfling. Sollte kein Szenario identifiziert
 * worden sein, so wird ein das Fallback-Szenario verwend und ein default report erzeugt.
 * 
 * @author Andreas Penski
 */
@Slf4j
public class CreateReportsAction implements CheckAction {

    public static final Process.Key<List<BusinessReport>, XMLSyntaxError> KEY = new Process.Key<>(null, XMLSyntaxError.class);

    public static final ActionMetadata METADATA = new ActionMetadata("Create report", "create_report");

    private final XvrlSerializer xvrlSerializer;

    public CreateReportsAction(final Processor processor, final ConversionService conversionService) {
        this.xvrlSerializer = new XvrlSerializer(conversionService, processor);
    }

    private static List<Scenario.Transformation> getTransformations(final Process results) {
        final Result<Scenario, String> scenarioSelection = results.getResult(ScenarioSelectionAction.KEY);
        return scenarioSelection.getObject().getReportTransformations();
    }

    private static XVRLReport generateXVRLReport(final ResourceType resourceType, final XdmNode node) {
        return XVRLReportBuilder.builder(METADATA)
                .add(detection().id(resourceType.getName()).add(supplemantal().addContent(node).id(resourceType.getName()))).build();
    }

    private static XVRLReport createErrorInformation(final ResourceType resourceType, final XMLSyntaxError error) {
        return builder(METADATA).add(detection().id("error").addError(error)).build();
    }

    @Override
    public ProcessStepResult<List<BusinessReport>, XMLSyntaxError> check(final Process process) {
        final ProcessStepResult<List<BusinessReport>, XMLSyntaxError> processStepResult = new ProcessStepResult<>(KEY);

        final Result<Scenario, String> scenarioSelection = process.getResult(ScenarioSelectionAction.KEY);
        final Scenario scenario = scenarioSelection.getObject();
        final XdmNode parsedDocument = process.getResult(DocumentParseAction.KEY).getObject();
        final List<BusinessReport> reports = getTransformations(process).stream()
                .map(t -> createReport(t, process, scenario, parsedDocument)).collect(Collectors.toList());
        processStepResult.setResult(new Result<>(reports, null));
        processStepResult.addReports(reports.stream().map(BusinessReport::getReport).collect(Collectors.toList()));

        return processStepResult;

    }

    private BusinessReport createReport(final Scenario.Transformation transformation, final Process process, final Scenario scenario,
            final XdmNode parsedDocument) {
        final BusinessReport r = new BusinessReport();
        r.setName(transformation.getResourceType().getName());
        try {
            final XdmNode root = this.xvrlSerializer.serialize(process.getXvrlReportSummary());
            final XsltTransformer transformer = transformation.getExecutable().load();
            transformer.setInitialContextNode(root);

            final CollectingErrorEventHandler e = new CollectingErrorEventHandler();
            transformer.setMessageListener(e);
            transformer.setResourceResolver(scenario.getUriResolver());

            if (scenario.getUnparsedTextURIResolver() != null) {
                transformer.getUnderlyingController().setUnparsedTextURIResolver(scenario.getUnparsedTextURIResolver());
            }
            if (parsedDocument != null) {
                transformer.setParameter(new QName("input-document"), parsedDocument);
            }
            final XdmDestination destination = new XdmDestination();
            transformer.setDestination(destination);
            transformer.transform();
            r.setContent(destination.getXdmNode());
            r.setReport(generateXVRLReport(transformation.getResourceType(), destination.getXdmNode()));
        } catch (final SaxonApiException | JAXBException e) {
            log.error("Error creating final report", e);
            process.setStopped(true);
            final XMLSyntaxError xmlSyntaxError = new XMLSyntaxError();
            xmlSyntaxError.setMessage("Can not create final report: " + e.getMessage());

            r.setReport(createErrorInformation(transformation.getResourceType(), xmlSyntaxError));
        }
        return r;

    }

    @Override
    public boolean isSkipped(final Process results) {
        return results.getResult(DocumentParseAction.KEY).isInvalid();
    }
}
