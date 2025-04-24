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

package de.kosit.validationtool.impl;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.api.XmlError;
import de.kosit.validationtool.impl.tasks.CheckAction;
import de.kosit.validationtool.impl.tasks.CheckAction.Bag;
import de.kosit.validationtool.impl.tasks.ComputeAcceptanceAction;
import de.kosit.validationtool.impl.tasks.CreateDocumentIdentificationAction;
import de.kosit.validationtool.impl.tasks.CreateReportAction;
import de.kosit.validationtool.impl.tasks.DocumentParseAction;
import de.kosit.validationtool.impl.tasks.ScenarioSelectionAction;
import de.kosit.validationtool.impl.tasks.SchemaValidationAction;
import de.kosit.validationtool.impl.tasks.SchematronValidationAction;
import de.kosit.validationtool.impl.tasks.ValidateReportInputAction;
import de.kosit.validationtool.impl.xml.ProcessorProvider;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.reportInput.EngineType;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.s9api.Processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.kosit.validationtool.impl.DateFactory.createTimestamp;

/**
 * The reference implementation for the validation process. After initialisation, instances are threadsafe and should be
 * reused since initializing saxon runtime objects is a rather heavyweight process.
 *
 * @author Andreas Penski
 */
@Getter
@Slf4j
public class DefaultCheck implements Check {

    private final ConversionService conversionService;

    private final List<Configuration> configuration;

    private final List<CheckAction> checkSteps;

    private final Processor processor;

    public DefaultCheck(final Configuration... configuration) {
        this(ProcessorProvider.getProcessor(), configuration);
    }

    /**
     * Creates a new instance for the {@link Configuration}.
     *
     * @param configuration the Configuration
     */
    public DefaultCheck(final Processor processor, final Configuration... configuration) {
        this.configuration = Arrays.asList(configuration);
        this.processor = processor;
        this.conversionService = new ConversionService();

        this.checkSteps = new ArrayList<>();
        this.checkSteps.add(new DocumentParseAction(processor));
        this.checkSteps.add(new CreateDocumentIdentificationAction());
        this.checkSteps.add(new ScenarioSelectionAction(new ScenarioRepository(configuration)));
        this.checkSteps.add(new SchemaValidationAction(processor));
        this.checkSteps.add(new SchematronValidationAction(this.conversionService));
        this.checkSteps.add(new ValidateReportInputAction(this.conversionService, SchemaProvider.getReportInputSchema()));
        this.checkSteps.add(new CreateReportAction(processor, this.conversionService));
        this.checkSteps.add(new ComputeAcceptanceAction());
    }

    protected static CreateReportInput createReport() {
        final CreateReportInput type = new CreateReportInput();
        final EngineType e = new EngineType();
        e.setName(EngineInformation.getName() + " " + EngineInformation.getVersion());
        type.setEngine(e);
        type.setTimestamp(createTimestamp());
        type.setFrameworkVersion(EngineInformation.getFrameworkVersion());
        return type;
    }

    protected boolean isSuccessful(final Map<String, Result> results) {
        return results.entrySet().stream().allMatch(e -> e.getValue().isAcceptable());
    }

    @Override
    public Result checkInput(final Input input) {
        final CheckAction.Bag t = new CheckAction.Bag(input, createReport());
        return runCheckInternal(t);
    }

    protected Result runCheckInternal(final CheckAction.Bag t) {
        final long started = System.currentTimeMillis();
        log.info("Checking content of {}", t.getInput().getName());
        for (final CheckAction action : this.checkSteps) {
            final long start = System.currentTimeMillis();
            if (!action.isSkipped(t)) {
                action.check(t);
            }
            log.debug("Step {} finished in {}ms", action.getClass().getSimpleName(), System.currentTimeMillis() - start);
        }
        t.setFinished(true);
        log.info("Finished check of {} in {}ms\n", t.getInput().getName(), System.currentTimeMillis() - started);
        return createResult(t);
    }

    private Result createResult(final Bag t) {
        final DefaultResult result = new DefaultResult(t.getReport(), t.getAcceptStatus(), new HtmlExtractor(this.processor));
        result.setWellformed(t.getParserResult().isValid());
        result.setReportInput(t.getReportInput());
        if (t.getSchemaValidationResult() != null) {
            result.setSchemaViolations(convertErrors(t.getSchemaValidationResult().getErrors()));
        }
        result.setProcessingSuccessful(!t.isStopped() && t.isFinished());
        result.setSchematronResult(t.getReportInput().getValidationResultsSchematron().stream().filter(e -> e.getResults() != null)
                .map(e -> e.getResults().getSchematronOutput()).collect(Collectors.toList()));
        return result;
    }

    private static List<XmlError> convertErrors(final Collection<XMLSyntaxError> errors) {
        List<XmlError> rv = new LinkedList<>();
        for (XMLSyntaxError error : errors) {
            if (error != null) {
                rv.add(error);
            }
        }
        return rv;
    }

}
