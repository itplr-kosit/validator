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

package de.kosit.validationtool.cmd;

import static de.kosit.validationtool.impl.xvrl.XVRLReportBuilder.builder;
import static de.kosit.validationtool.impl.xvrl.XVRLReportBuilder.detection;

import java.nio.file.Path;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.model.ProcessStepResult;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.BusinessReport;
import de.kosit.validationtool.impl.tasks.CheckAction;
import de.kosit.validationtool.impl.tasks.CreateReportsAction;
import de.kosit.validationtool.impl.xvrl.XVRLReportBuilder;
import de.kosit.validationtool.model.XMLSyntaxError;
import de.kosit.validationtool.model.xvrl.XVRLReport;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

/**
 * Extrahiert Erstellten Dokumentne aus dem Report und persistiert diese im konfigurierten Ausgabe-Verzeichnis.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
class ExtractReportContentAction implements CheckAction {

    public static final Process.Key<Boolean, String> KEY = new Process.Key<>(Boolean.class, String.class);

    private static final String REPORT_NAME = "Extract Create Report Content";

    private final Path outputDirectory;

    private Processor processor;

    public ExtractReportContentAction(final Processor processor, final Path outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.processor = processor;
    }

    private static XVRLReport generateXVRLReport(final Result<Boolean, String> result) {
        if (result.isValid()) {
            return builder(REPORT_NAME).add(XVRLReportBuilder.detection().addMessage("Extraction successful")).build();
        }

        return builder(REPORT_NAME).addAll(result.getErrors().stream().map(e -> detection().addError(e))).build();
    }

    @Override
    public ProcessStepResult<Boolean, String> check(final Process results) {

        final Result<List<BusinessReport>, XMLSyntaxError> reportReposts = results.getResult(CreateReportsAction.KEY);

        reportReposts.getObject().forEach(entry -> {
            print(entry.getName(), entry.getContent());
        });

        final ProcessStepResult<Boolean, String> processStepResult = new ProcessStepResult<>(KEY);
        final Result<Boolean, String> stepResult = new Result<>(true);
        processStepResult.setResult(stepResult);
        processStepResult.setReport(generateXVRLReport(stepResult));
        return processStepResult;
    }

    private void print(final String origName, final XdmItem xdmItem) {
        final XdmNode node = (XdmNode) xdmItem;
        final String name = origName + "-Create_Report-result";
        final Path file = this.outputDirectory.resolve(name + ".xml");
        final Serializer serializer = this.processor.newSerializer(file.toFile());
        try {
            log.info("Writing create-report result '{}' to {}", name, file.toAbsolutePath());
            serializer.serializeNode(node);
        } catch (final SaxonApiException e) {
            log.error("Error extracting create-report content to {}", file.toAbsolutePath(), e);
        }
    }

    @Override
    public boolean isSkipped(final Process results) {
        final Result<List<BusinessReport>, XMLSyntaxError> createReportResult = results.getResult(CreateReportsAction.KEY);
        if (createReportResult == null || createReportResult.getObject() == null) {
            log.warn("Can not extract create-report  content. No report document found");
            return true;
        }
        return false;
    }
}
