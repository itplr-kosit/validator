package org.kosit.validator.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kosit.validator.api.Check;
import org.kosit.validator.api.Configuration;
import org.kosit.validator.api.Input;
import org.kosit.validator.api.Result;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.conformatron.engine.SchematronValidation;
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
public class DefaultCheck implements Check, ValidationEngine<Result> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCheck.class);

    private final XvrlConversionService xvrlConversionService;

    private final List<Configuration> configuration;

    private final List<CheckAction> checkSteps;

    private final Processor processor;

    private final EngineInformation engineInformation;

    private final SchematronValidation adHocValidation;

    private final ConformanceValidation conformanceValidation;

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
        this.adHocValidation = new SchematronValidation(processor);
        this.xvrlConversionService = new XvrlConversionService();
        this.checkSteps = new ArrayList<>();
        this.checkSteps.add(new DocumentParseAction(processor));
        this.checkSteps.add(new CreateDocumentIdentificationAction());
        this.checkSteps.add(new ScenarioSelectionAction(new ScenarioRepository(configuration)));
        this.checkSteps.add(new SchemaValidationAction(processor));
        this.checkSteps.add(new SchematronValidationAction(new SvrlConversionService()));
        this.checkSteps.add(new CreateReportsAction(processor, this.xvrlConversionService));
        this.checkSteps.add(new ComputeAcceptanceAction());
        this.conformanceValidation = new ConformanceValidation(engineInformation, this.checkSteps);
    }

    protected XVRLMetadata createXVRLMetadata() {
        return this.conformanceValidation.createMetadata();
    }

    protected boolean isSuccessful(final Map<String, Result> results) {
        return results.entrySet().stream().allMatch(e -> e.getValue().isAcceptable());
    }

    @Override
    public Result checkInput(final Input input) {
        final Process checkProcess = new Process(input, createXVRLMetadata());
        return runCheckInternal(checkProcess);
    }

    /**
     * Full conformance validation ({@link ValidationEngine} contract) — same run as the legacy
     * {@link #checkInput(Input)}, executed by the {@link ConformanceValidation} mode class.
     */
    @Override
    public Result validate(final Input input) {
        return checkInput(input);
    }

    /**
     * Convenience for the ad-hoc mode: validates directly against the given Schematron using the
     * {@link SchematronValidation} engine (see {@link ValidationEngine}).
     */
    public SchematronValidation.AdHocValidationResult validateAdHoc(final Input input, final URI schematron) {
        return this.adHocValidation.validate(input, schematron);
    }

    protected Result runCheckInternal(final Process checkProcess) {
        return this.conformanceValidation.run(checkProcess);
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
