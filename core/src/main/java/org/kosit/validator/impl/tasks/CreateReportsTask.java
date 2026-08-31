package org.kosit.validator.impl.tasks;

import java.util.List;

import org.kosit.base.error.DefaultSimpleError;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.impl.ActionMetadata;
import org.kosit.validator.impl.CollectingErrorEventHandler;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.saxon.SaxonHelper;
import org.kosit.validator.scenario.v1.ResourceType;
import org.kosit.validator.xvrl.XvrlHelper;
import org.kosit.validator.xvrl.XvrlSerializer;
import org.kosit.xvrl.model.XvrlDetection;
import org.kosit.xvrl.model.XvrlMetadata;
import org.kosit.xvrl.model.XvrlReport;
import org.kosit.xvrl.model.XvrlSupplemental;
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

    public static final Process.ProcessKey<List<BusinessReport>, SimpleError> KEY = new Process.ProcessKey<>(null, SimpleError.class);

    public static final ActionMetadata METADATA = new ActionMetadata("Create report", "create_report");

    private final XvrlSerializer xvrlSerializer;

    public CreateReportsTask(final Processor processor) {
        this.xvrlSerializer = new XvrlSerializer(processor);
    }

    private static List<Scenario.Transformation> getTransformations(final Process results) {
        final SingleProcessingResult<Scenario, String> scenarioSelection = results.getResult(ScenarioSelectionTask.KEY);
        return scenarioSelection.getObject().getReportTransformations();
    }

    private static XvrlReport generateXvrlReport(final ResourceType resourceType, final XdmNode node) {
        final var builder = XvrlReport.builder().metadata(XvrlMetadata.builder().validator(METADATA.name())).addDetection(XvrlDetection
                .builder().id(resourceType.getName()).addSupplemental(XvrlSupplemental.builder().addContent(SaxonHelper.toNode(node))));
        return XvrlHelper.finalizeAndBuild(builder);
    }

    private static XvrlReport createErrorInformation(final ResourceType resourceType, final SimpleError error) {
        final var builder = XvrlReport.builder().metadata(XvrlMetadata.builder().validator(METADATA.name()))
                .addDetection(XvrlDetection.builder().error(error));
        return XvrlHelper.finalizeAndBuild(builder);
    }

    @Override
    public ProcessStepResult<List<BusinessReport>, SimpleError> check(final Process process) {
        final ProcessStepResult<List<BusinessReport>, SimpleError> processStepResult = new ProcessStepResult<>(KEY);
        final SingleProcessingResult<Scenario, String> scenarioSelection = process.getResult(ScenarioSelectionTask.KEY);
        final Scenario scenario = scenarioSelection.getObject();
        final XdmNode parsedDocument = process.getResult(DocumentParseTask.KEY).getObject();
        final List<BusinessReport> reports = getTransformations(process).stream()
                .map(t -> createReport(t, process, scenario, parsedDocument)).toList();
        processStepResult.setResult(new SingleProcessingResult<>(reports, null));
        processStepResult.addReports(reports.stream().map(BusinessReport::getReport).toList());
        return processStepResult;
    }

    private BusinessReport createReport(final Scenario.Transformation transformation, final Process process, final Scenario scenario,
            final XdmNode parsedDocument) {
        final BusinessReport r = new BusinessReport();
        r.setName(transformation.getResourceType().getName());
        try {
            final XdmNode root = this.xvrlSerializer.marshalToXdmNode(process.getXvrlReportSummary());

            final XsltTransformer transformer = transformation.getExecutable().load();
            transformer.setInitialContextNode(root);

            final CollectingErrorEventHandler e = new CollectingErrorEventHandler();
            transformer.setMessageHandler(e);
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
            r.setReport(generateXvrlReport(transformation.getResourceType(), destination.getXdmNode()));
        } catch (final SaxonApiException | JAXBException e) {
            LOGGER.error("Error creating final report", e);
            process.setStopped(true);

            final var errorBuilder = DefaultSimpleError.builderError().message("Can not create final report: " + e.getMessage())
                    .linkedException(e);
            if (e instanceof final SaxonApiException saxonApiEx)
                errorBuilder.location(saxonApiEx.getSystemId(), saxonApiEx.getLineNumber(), 0);

            r.setReport(createErrorInformation(transformation.getResourceType(), errorBuilder.build()));
        }
        return r;
    }

    @Override
    public boolean isSkipped(final Process results) {
        return results.getResult(DocumentParseTask.KEY).isInvalid();
    }
}
