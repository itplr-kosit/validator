package org.kosit.validator.impl;

import static org.kosit.validator.impl.DateFactory.createTimestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kosit.validator.api.AcceptRecommendation;
import org.kosit.validator.api.Check;
import org.kosit.validator.api.Configuration;
import org.kosit.validator.api.Input;
import org.kosit.validator.api.Result;
import org.kosit.validator.api.XmlError;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.tasks.CheckAction;
import org.kosit.validator.impl.tasks.CheckAction.Process;
import org.kosit.validator.impl.tasks.ComputeAcceptanceAction;
import org.kosit.validator.impl.tasks.CreateDocumentIdentificationAction;
import org.kosit.validator.impl.tasks.CreateReportsAction;
import org.kosit.validator.impl.tasks.DocumentParseAction;
import org.kosit.validator.impl.tasks.ScenarioSelectionAction;
import org.kosit.validator.impl.tasks.SchemaValidationAction;
import org.kosit.validator.impl.tasks.SchematronValidationAction;
import org.kosit.validator.impl.xml.ProcessorProvider;
import org.kosit.validator.model.ValidationResultsSchematron;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.svrl.impl.SvrlConversionService;
import org.kosit.xvrl.impl.XvrlConversionService;
import org.kosit.xvrl.model.Timestamp;
import org.kosit.xvrl.model.Validator;
import org.kosit.xvrl.model.XVRLMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;

/**
 * The reference implementation for the validation process. After initialisation, instances are threadsafe and should be
 * reused since initializing saxon runtime objects is a rather heavyweight process.
 *
 * @author Andreas Penski
 */
public class DefaultCheck implements Check {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCheck.class);

    private final XvrlConversionService xvrlConversionService;

    private final List<Configuration> configuration;

    private final List<CheckAction> checkSteps;

    private final Processor processor;

    private final EngineInformation engineInformation;

    public DefaultCheck(final EngineInformation engineInformation, final Configuration... configuration) {
        this(engineInformation, ProcessorProvider.getProcessor(), configuration);
    }

    /**
     * Creates a new instance for the {@link Configuration}.
     *
     * @param engineInformation engine info
     * @param processor Saxon processor
     * @param configuration the Configuration
     */
    public DefaultCheck(EngineInformation engineInformation, final Processor processor, final Configuration... configuration) {
        this.engineInformation = engineInformation;
        this.configuration = Arrays.asList(configuration);
        this.processor = processor;
        this.xvrlConversionService = new XvrlConversionService();
        this.checkSteps = new ArrayList<>();
        this.checkSteps.add(new DocumentParseAction(processor));
        this.checkSteps.add(new CreateDocumentIdentificationAction());
        this.checkSteps.add(new ScenarioSelectionAction(new ScenarioRepository(configuration)));
        this.checkSteps.add(new SchemaValidationAction(processor));
        this.checkSteps.add(new SchematronValidationAction(new SvrlConversionService()));
        this.checkSteps.add(new CreateReportsAction(processor, this.xvrlConversionService));
        this.checkSteps.add(new ComputeAcceptanceAction());
    }

    protected XVRLMetadata createXVRLMetadata() {
        final XVRLMetadata metadata = new XVRLMetadata();
        final Timestamp timestamp = new Timestamp();
        timestamp.setValue(createTimestamp());
        metadata.getTimestamps().add(timestamp);
        final Validator validator = new Validator();
        validator.setName(this.engineInformation.getName());
        validator.setVersion(this.engineInformation.getVersion());
        metadata.getValidators().add(validator);
        return metadata;
    }

    private static List<XmlError> convertErrors(final Collection<XMLSyntaxError> errors) {
        // noinspection unchecked
        return (List<XmlError>) (List<?>) errors;
    }

    private static Result createResult(final Process process) {
        final org.kosit.validator.impl.model.Result<AcceptRecommendation, XMLSyntaxError> acceptStatusResult = process
                .getResult(ComputeAcceptanceAction.KEY);
        final DefaultResult defaultResult = new DefaultResult(acceptStatusResult.getObject());
        defaultResult.setWellformed(process.getResult(DocumentParseAction.KEY).isValid());
        defaultResult.setReportSummary(process.getXvrlReportSummary());
        final org.kosit.validator.impl.model.Result<Boolean, XMLSyntaxError> schemaValidationResult = process
                .getResult(SchemaValidationAction.KEY);
        if (schemaValidationResult != null) {
            defaultResult.setSchemaViolations(convertErrors(schemaValidationResult.getErrors()));
        }
        final org.kosit.validator.impl.model.Result<List<ValidationResultsSchematron>, String> schematronValidationResult = process
                .getResult(SchematronValidationAction.KEY);
        if (schematronValidationResult != null) {
            defaultResult.setSchematronResult(schematronValidationResult.getObject().stream()
                    .map(schematronResult -> schematronResult.getResults().getSchematronOutput()).collect(Collectors.toList()));
        }
        defaultResult.setProcessingSuccessful(!process.isStopped() && process.isFinished());
        return defaultResult;
    }

    protected boolean isSuccessful(final Map<String, Result> results) {
        return results.entrySet().stream().allMatch(e -> e.getValue().isAcceptable());
    }

    @Override
    public Result checkInput(final Input input) {
        final Process checkProcess = new Process(input, createXVRLMetadata());
        return runCheckInternal(checkProcess);
    }

    protected Result runCheckInternal(final Process checkProcess) {
        final long started = System.currentTimeMillis();
        LOGGER.info("Checking content of {}", checkProcess.getInput().getName());
        for (final CheckAction action : this.checkSteps) {
            final long start = System.currentTimeMillis();
            if (!action.isSkipped(checkProcess)) {
                final ProcessStepResult<?, ?> result = action.check(checkProcess);
                checkProcess.addStepResult(result);
            }
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Step {} finished in {}ms", action.getClass().getSimpleName(), System.currentTimeMillis() - start);
        }
        checkProcess.setFinished(true);
        LOGGER.info("Finished check of {} in {}ms\n", checkProcess.getInput().getName(), System.currentTimeMillis() - started);
        return createResult(checkProcess);
    }

    public XvrlConversionService getXvrlConversionService() {
        return this.xvrlConversionService;
    }

    public List<Configuration> getConfiguration() {
        return this.configuration;
    }

    public List<CheckAction> getCheckSteps() {
        return this.checkSteps;
    }

    public Processor getProcessor() {
        return this.processor;
    }
}
