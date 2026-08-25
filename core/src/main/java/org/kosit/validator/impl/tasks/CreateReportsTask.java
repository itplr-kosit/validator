package org.kosit.validator.impl.tasks;

import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.builder;
import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.detection;
import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.supplemental;

import java.util.List;

import org.kosit.validator.impl.ActionMetadata;
import org.kosit.validator.impl.CollectingErrorEventHandler;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.xvrl.XVRLReportBuilder;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.validator.model.scenarios.ResourceType;
import org.kosit.xvrl.impl.XvrlConversionService;
import org.kosit.xvrl.model.XVRLReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBException;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Generates the reports based on the collected information about the test document. If no scenario was identified, the
 * fallback scenario is used and a default report is generated.
 *
 * @author Andreas Penski
 */
public class CreateReportsTask implements CheckTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateReportsTask.class);

    public static final Process.Key<List<BusinessReport>, XMLSyntaxError> KEY = new Process.Key<>(null, XMLSyntaxError.class);

    public static final ActionMetadata METADATA = new ActionMetadata("Create report", "create_report");

    private final XvrlSerializer xvrlSerializer;

    public CreateReportsTask(final Processor processor, final XvrlConversionService xvrlConversionService) {
        this.xvrlSerializer = new XvrlSerializer(xvrlConversionService, processor);
    }

    private static List<Scenario.Transformation> getTransformations(final Process results) {
        final Result<Scenario, String> scenarioSelection = results.getResult(ScenarioSelectionTask.KEY);
        return scenarioSelection.getObject().getReportTransformations();
    }

    private static XVRLReport generateXVRLReport(final ResourceType resourceType, final XdmNode node) {
        return XVRLReportBuilder.builder(METADATA)
                .add(detection().id(resourceType.getName()).add(supplemental().addContent(node).id(resourceType.getName()))).build();
    }

    private static XVRLReport createErrorInformation(final ResourceType resourceType, final XMLSyntaxError error) {
        return builder(METADATA).add(detection().id("error").addError(error)).build();
    }

    @Override
    public ProcessStepResult<List<BusinessReport>, XMLSyntaxError> check(final Process process) {
        final ProcessStepResult<List<BusinessReport>, XMLSyntaxError> processStepResult = new ProcessStepResult<>(KEY);
        final Result<Scenario, String> scenarioSelection = process.getResult(ScenarioSelectionTask.KEY);
        final Scenario scenario = scenarioSelection.getObject();
        final XdmNode parsedDocument = process.getResult(DocumentParseTask.KEY).getObject();
        final List<BusinessReport> reports = getTransformations(process).stream()
                .map(t -> createReport(t, process, scenario, parsedDocument)).toList();
        processStepResult.setResult(new Result<>(reports, null));
        processStepResult.addReports(reports.stream().map(BusinessReport::getReport).toList());
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
            LOGGER.error("Error creating final report", e);
            process.setStopped(true);
            final XMLSyntaxError xmlSyntaxError = new XMLSyntaxError();
            xmlSyntaxError.setMessage("Can not create final report: " + e.getMessage());
            r.setReport(createErrorInformation(transformation.getResourceType(), xmlSyntaxError));
        }
        return r;
    }

    @Override
    public boolean isSkipped(final Process results) {
        return results.getResult(DocumentParseTask.KEY).isInvalid();
    }
}
