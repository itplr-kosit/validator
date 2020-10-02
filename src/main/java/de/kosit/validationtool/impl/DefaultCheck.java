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

package de.kosit.validationtool.impl;

import static de.kosit.validationtool.impl.DateFactory.createTimestamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.reportInput.EngineType;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;

import net.sf.saxon.s9api.Processor;

/**
 * The reference implementation for the validation process. After initialisation, instances are threadsafe and should be
 * reused since initializing saxon runtime objects is an rather heavyweight process.
 *
 * @author Andreas Penski
 */
@Slf4j
public class DefaultCheck implements Check {

    @Getter
    private final ConversionService conversionService;

    @Getter
    private final Configuration configuration;

    @Getter
    private final List<CheckAction> checkSteps;

    /**
     * Creates a new instance for the {@link Configuration}.
     *
     * @param configuration the Configuration
     */
    public DefaultCheck(final Configuration configuration) {
        this.configuration = configuration;
        final ContentRepository content = configuration.getContentRepository();
        final Processor processor = content.getProcessor();
        this.conversionService = new ConversionService();

        this.checkSteps = new ArrayList<>();
        this.checkSteps.add(new DocumentParseAction(processor));
        this.checkSteps.add(new CreateDocumentIdentificationAction());
        this.checkSteps.add(new ScenarioSelectionAction(new ScenarioRepository(configuration)));
        this.checkSteps.add(new SchemaValidationAction(content.getResolvingConfigurationStrategy(), processor));
        this.checkSteps.add(new SchematronValidationAction(content.getResolver(), this.conversionService));
        this.checkSteps.add(new ValidateReportInputAction(this.conversionService, content.getReportInputSchema()));
        this.checkSteps.add(
                new CreateReportAction(processor, this.conversionService, content.getResolver(), content.getUnparsedTextURIResolver()));
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
        final DefaultResult result = new DefaultResult(t.getReport(), t.getAcceptStatus(),
                new HtmlExtractor(this.configuration.getContentRepository().getProcessor()));
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
        // noinspection unchecked
        return (List<XmlError>) (List<?>) errors;
    }

}
