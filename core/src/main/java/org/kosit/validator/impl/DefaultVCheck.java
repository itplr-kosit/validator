package org.kosit.validator.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.conformatron.api.model.source.CTReadResource;
import org.kosit.svrl.impl.SvrlConversionService;
import org.kosit.validator.api.VCheck;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.VResult;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.conformatron.engine.SchematronValidation;
import org.kosit.validator.impl.saxon.ProcessorProvider;
import org.kosit.validator.impl.tasks.CheckTask;
import org.kosit.validator.impl.tasks.CheckTask.Process;
import org.kosit.validator.impl.tasks.ComputeAcceptanceTask;
import org.kosit.validator.impl.tasks.CreateDocumentIdentificationTask;
import org.kosit.validator.impl.tasks.CreateReportsTask;
import org.kosit.validator.impl.tasks.DocumentParseTask;
import org.kosit.validator.impl.tasks.ScenarioSelectionTask;
import org.kosit.validator.impl.tasks.SchemaValidationTask;
import org.kosit.validator.impl.tasks.SchematronValidationTask;
import org.kosit.xvrl.model.XVRLMetadataType;

import net.sf.saxon.s9api.Processor;

/**
 * The reference implementation for the validation process. After initialisation, instances are threadsafe and should be
 * reused since initializing saxon runtime objects is a rather heavyweight process.
 *
 * @author Andreas Penski
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class DefaultVCheck implements VCheck, ValidationEngine<VResult> {

    private final List<VConfiguration> configuration;

    private final List<CheckTask> checkSteps;

    private final Processor processor;

    private final SchematronValidation adHocValidation;

    private final ConformanceValidation conformanceValidation;

    @Deprecated(since = "2.0.0", forRemoval = true)
    public DefaultVCheck(final EngineInformation engineInformation, final VConfiguration... configuration) {
        this(engineInformation, ProcessorProvider.getProcessor(), configuration);
    }

    /**
     * Creates a new instance for the {@link VConfiguration}.
     *
     * @param engineInformation engine info
     * @param processor Saxon processor
     * @param configuration the Configuration
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public DefaultVCheck(final EngineInformation engineInformation, final Processor processor, final VConfiguration... configuration) {
        this.configuration = Arrays.asList(configuration);
        this.processor = processor;
        this.adHocValidation = new SchematronValidation(processor);
        this.checkSteps = new ArrayList<>();
        this.checkSteps.add(new DocumentParseTask(processor));
        this.checkSteps.add(new CreateDocumentIdentificationTask());
        this.checkSteps.add(new ScenarioSelectionTask(new ScenarioRepository(configuration)));
        this.checkSteps.add(new SchemaValidationTask(processor));
        this.checkSteps.add(new SchematronValidationTask(new SvrlConversionService()));
        this.checkSteps.add(new CreateReportsTask(processor));
        this.checkSteps.add(new ComputeAcceptanceTask());
        this.conformanceValidation = new ConformanceValidation(engineInformation, this.checkSteps);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    protected XVRLMetadataType createXVRLMetadata() {
        return this.conformanceValidation.createMetadata();
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    protected boolean isSuccessful(final Map<String, VResult> results) {
        return results.entrySet().stream().allMatch(e -> e.getValue().isAcceptable());
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public VResult checkInput(final CTReadResource input) {
        final Process checkProcess = new Process(input, createXVRLMetadata());
        return runCheckInternal(checkProcess);
    }

    /**
     * Full conformance validation ({@link ValidationEngine} contract) — same run as the legacy
     * {@link #checkInput(VInput)}, executed by the {@link ConformanceValidation} mode class.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public VResult validate(final CTReadResource input) {
        return checkInput(input);
    }

    /**
     * Convenience for the ad-hoc mode: validates directly against the given Schematron using the
     * {@link SchematronValidation} engine (see {@link ValidationEngine}).
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public SchematronValidation.AdHocValidationResult validateAdHoc(final CTReadResource input, final URI schematron) {
        return this.adHocValidation.validate(input, schematron);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    protected VResult runCheckInternal(final Process checkProcess) {
        return this.conformanceValidation.run(checkProcess);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public List<VConfiguration> getConfiguration() {
        return this.configuration;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public List<CheckTask> getCheckSteps() {
        return this.checkSteps;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public Processor getProcessor() {
        return this.processor;
    }
}
