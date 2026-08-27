package org.kosit.validator.impl;

import java.util.Collection;
import java.util.List;

import org.conformatron.api.model.source.CTReadResource;
import org.kosit.jaxb.JaxbHelper;
import org.kosit.validator.api.VResult;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.api.xmlerror.XmlError;
import org.kosit.validator.api.xmlerror.XmlSyntaxError;
import org.kosit.validator.api.xvrl.compact.AcceptRecommendation;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.tasks.CheckTask;
import org.kosit.validator.impl.tasks.CheckTask.Process;
import org.kosit.validator.impl.tasks.ComputeAcceptanceTask;
import org.kosit.validator.impl.tasks.DocumentParseTask;
import org.kosit.validator.impl.tasks.SchemaValidationTask;
import org.kosit.validator.impl.tasks.SchematronValidationTask;
import org.kosit.validator.model.ValidationResultsSchematron;
import org.kosit.xvrl.model.XvrlMetadataType;
import org.kosit.xvrl.model.XvrlTimestampType;
import org.kosit.xvrl.model.XvrlValidatorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <b>full conformance validation</b> mode of the {@link org.kosit.validator.api.ValidationEngine}: runs the
 * complete pipeline (all steps) over a {@link Process} and assembles the {@link VResult} — scenario
 * detection/selection, schema and schematron validation, report generation and acceptance recommendation.
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

    private final List<CheckTask> checkSteps;

    /**
     * Creates the conformance validation engine over the given pipeline steps.
     *
     * @param engineInformation engine info for the report metadata
     * @param checkSteps the configured pipeline steps, executed in order
     */
    public ConformanceValidation(final EngineInformation engineInformation, final List<CheckTask> checkSteps) {
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
     * @param input the document to validate
     * @return the assembled {@link VResult}
     */
    @Override
    public VResult validate(final CTReadResource input) {
        return run(new Process(input, createMetadata()));
    }

    /**
     * Creates the Xvrl report metadata for a validation run (engine name, version, timestamp).
     *
     * @return the metadata
     */
    public XvrlMetadataType createMetadata() {
        final XvrlMetadataType metadata = new XvrlMetadataType();
        final XvrlTimestampType timestamp = new XvrlTimestampType();
        timestamp.setValue(JaxbHelper.createTimestamp());
        metadata.getTimestamps().add(timestamp);

        final XvrlValidatorType validator = new XvrlValidatorType();
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
        for (final CheckTask action : this.checkSteps) {
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
        final org.kosit.validator.impl.model.SingleProcessingResult<AcceptRecommendation, XmlSyntaxError> acceptStatusResult = process
                .getResult(ComputeAcceptanceTask.KEY);
        final DefaultResult defaultResult = new DefaultResult(acceptStatusResult.getObject());
        defaultResult.setWellformed(process.getResult(DocumentParseTask.KEY).isValid());
        defaultResult.setReportSummary(process.getXvrlReportSummary());
        final org.kosit.validator.impl.model.SingleProcessingResult<Boolean, XmlSyntaxError> schemaValidationResult = process
                .getResult(SchemaValidationTask.KEY);
        if (schemaValidationResult != null) {
            defaultResult.setSchemaViolations(convertErrors(schemaValidationResult.getErrors()));
        }
        final org.kosit.validator.impl.model.SingleProcessingResult<List<ValidationResultsSchematron>, String> schematronValidationResult = process
                .getResult(SchematronValidationTask.KEY);
        if (schematronValidationResult != null) {
            defaultResult.setSchematronResult(schematronValidationResult.getObject().stream()
                    .map(schematronResult -> schematronResult.getResults().getSchematronOutput()).toList());
        }
        defaultResult.setProcessingSuccessful(!process.isStopped() && process.isFinished());
        return defaultResult;
    }

    private static List<XmlError> convertErrors(final Collection<XmlSyntaxError> errors) {
        // noinspection unchecked
        return (List<XmlError>) (List<?>) errors;
    }
}
