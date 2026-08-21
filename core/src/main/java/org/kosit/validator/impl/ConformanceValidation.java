package org.kosit.validator.impl;

import static org.kosit.validator.impl.DateFactory.createTimestamp;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.kosit.validator.api.AcceptRecommendation;
import org.kosit.validator.api.VInput;
import org.kosit.validator.api.VResult;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.api.XmlError;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.tasks.CheckAction;
import org.kosit.validator.impl.tasks.CheckAction.Process;
import org.kosit.validator.impl.tasks.ComputeAcceptanceAction;
import org.kosit.validator.impl.tasks.DocumentParseAction;
import org.kosit.validator.impl.tasks.SchemaValidationAction;
import org.kosit.validator.impl.tasks.SchematronValidationAction;
import org.kosit.validator.model.ValidationResultsSchematron;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.xvrl.model.Timestamp;
import org.kosit.xvrl.model.Validator;
import org.kosit.xvrl.model.XVRLMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <b>full conformance validation</b> mode of the {@link org.kosit.validator.api.ValidationEngine}: runs the
 * complete pipeline (all steps) over a {@link Process} and assembles the {@link VResult} — scenario detection/selection,
 * schema and schematron validation, report generation and acceptance recommendation.
 * <p>
 * Individual class per validator design philosophy: the {@code ValidationEngine} interface is a pure contract, the mode
 * behavior lives here. Counterpart for the technical ad-hoc mode:
 * {@link org.kosit.validator.impl.conformatron.SchematronValidation}.
 * </p>
 *
 * @author Andreas Schmitz
 */
public class ConformanceValidation implements ValidationEngine<VResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConformanceValidation.class);

    private final EngineInformation engineInformation;

    private final List<CheckAction> checkSteps;

    /**
     * Creates the conformance validation engine over the given pipeline steps.
     *
     * @param engineInformation engine info for the report metadata
     * @param checkSteps the configured pipeline steps, executed in order
     */
    public ConformanceValidation(final EngineInformation engineInformation, final List<CheckAction> checkSteps) {
        if (engineInformation == null) {
            throw new IllegalArgumentException("engineInformation may not be null");
        }
        if (checkSteps == null) {
            throw new IllegalArgumentException("checkSteps may not be null");
        }
        this.engineInformation = engineInformation;
        this.checkSteps = checkSteps;
    }

    /**
     * Full conformance validation ({@link ValidationEngine} contract): runs the complete pipeline over the document.
     *
     * @param VInput the document to validate
     * @return the assembled {@link VResult}
     */
    @Override
    public VResult validate(final VInput VInput) {
        return run(new Process(VInput, createMetadata()));
    }

    /**
     * Creates the XVRL report metadata for a validation run (engine name, version, timestamp).
     *
     * @return the metadata
     */
    public XVRLMetadata createMetadata() {
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

    /**
     * Runs all pipeline steps over the given process and assembles the result.
     *
     * @param checkProcess the process carrying the input and collecting the step results
     * @return the assembled {@link VResult}
     */
    public VResult run(final Process checkProcess) {
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

    private static VResult createResult(final Process process) {
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

    private static List<XmlError> convertErrors(final Collection<XMLSyntaxError> errors) {
        // noinspection unchecked
        return (List<XmlError>) (List<?>) errors;
    }
}
