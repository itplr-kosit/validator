/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.CheckConfiguration;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.impl.tasks.CheckAction;
import de.kosit.validationtool.impl.tasks.CreateReportAction;
import de.kosit.validationtool.impl.tasks.DocumentParseAction;
import de.kosit.validationtool.impl.tasks.ScenarioSelectionAction;
import de.kosit.validationtool.impl.tasks.SchemaValidationAction;
import de.kosit.validationtool.impl.tasks.SchematronValidationAction;
import de.kosit.validationtool.impl.tasks.ValidateReportInputAction;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.reportInput.DocumentIdentificationType;
import de.kosit.validationtool.model.reportInput.EngineType;
import de.kosit.validationtool.model.reportInput.ProcessingError;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;

/**
 * Die Referenz-Implementierung für den Prüfprozess. Nach initialer Konfiguration ist diese Klasse threadsafe und kann
 * in Server-Umgebungen eingesetzt werden
 * 
 * @author Andreas Penski
 */
@Slf4j
public class DefaultCheck implements Check {

    private static final String ENGINE_NAME = "KoSIT Prüftool";

    private static final String ENGINE_VERSION = "1.0.0";

    private ScenarioRepository repository;

    @Getter
    private ContentRepository contentRepository;

    private ConversionService conversionService;

    @Getter
    private List<CheckAction> checkSteps;

    /**
     * Erzeugt eine neue Instanz mit der angegebenen Konfiguration.
     * 
     * @param configuration die Konfiguration
     */
    public DefaultCheck(CheckConfiguration configuration) {
        Processor processor = ObjectFactory.createProcessor();
        conversionService = new ConversionService();
        contentRepository = new ContentRepository(processor, configuration.getScenarioRepository());
        repository = new ScenarioRepository(processor, contentRepository);
        repository.initialize(configuration);
        checkSteps = new ArrayList<>();
        checkSteps.add(this::createDocumentIdentification);
        checkSteps.add(new DocumentParseAction());
        checkSteps.add(new ScenarioSelectionAction(repository));
        checkSteps.add(new SchemaValidationAction());
        checkSteps.add(new SchematronValidationAction(processor, configuration.getScenarioRepository()));
        checkSteps.add(new ValidateReportInputAction(conversionService, contentRepository.getReportInputSchema()));
        checkSteps.add(new CreateReportAction(processor, conversionService, repository, configuration.getScenarioRepository()));
    }

    protected static CreateReportInput createReport() {
        CreateReportInput type = new CreateReportInput();
        EngineType e = new EngineType();
        e.setName(ENGINE_NAME);
        type.setEngine(e);
        type.setTimestamp(ObjectFactory.createTimestamp());
        type.setFrameworkVersion(ENGINE_VERSION);
        return type;
    }

    @Override
    public XdmNode checkInput(Input input) {
        CheckAction.Bag t = new CheckAction.Bag(input, createReport());
        return runCheckInternal(t);
    }

    protected XdmNode runCheckInternal(CheckAction.Bag t) {
        long started = System.currentTimeMillis();
        log.info("Checking content of {}", t.getInput().getName());
        Iterator<CheckAction> it = checkSteps.iterator();


        while (it.hasNext()) {
            long start = System.currentTimeMillis();
            final CheckAction action = it.next();
            if (!action.isSkipped(t)) {
                action.check(t);
            }
            log.info("Step {} finished in {}ms", action.getClass().getSimpleName(), System.currentTimeMillis() - start);
            if (t.isStopped()) {
                final ProcessingError processingError = t.getReportInput().getProcessingError();
                log.error("Error processing input {}: {}", t.getInput().getName(),
                        processingError != null ? processingError.getError().stream().collect(Collectors.joining("\n")) : "");
                break;
            }
        }
        t.setFinished(true);
        log.info("Finished check of {} in {}ms\n", t.getInput().getName(), System.currentTimeMillis() - started);
        return t.getReport();
    }

    private boolean createDocumentIdentification(CheckAction.Bag transporter) {
        DocumentIdentificationType i = new DocumentIdentificationType();
        DocumentIdentificationType.DocumentHash h = new DocumentIdentificationType.DocumentHash();
        h.setHashAlgorithm(transporter.getInput().getDigestAlgorithm());
        h.setHashValue(transporter.getInput().getHashCode());
        i.setDocumentHash(h);
        i.setDocumentReference(transporter.getInput().getName());
        transporter.getReportInput().setDocumentIdentification(i);
        return true;
    }

}
