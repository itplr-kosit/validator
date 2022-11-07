/*
 * Copyright 2017-2021  Koordinierungsstelle für IT-Standards (KoSIT)
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

import java.io.StringWriter;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.Printer;
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

/**
 * Gibt das Ergebnis-Document auf std-out aus.
 * 
 * @author Andreas Penski
 */
@Slf4j
@RequiredArgsConstructor
class PrintReportAction implements CheckAction {

    public static final Process.Key<Boolean, String> KEY = new Process.Key<>(Boolean.class, String.class);

    private final Processor processor;

    private static XVRLReport createReport() {
        return XVRLReportBuilder.builder("Document wellformedness Validator").name("Print Report").setValid().build();
    }

    @Override
    public ProcessStepResult<Boolean, String> check(final Process results) {
        try {
            final StringWriter writer = new StringWriter();
            final Serializer serializer = this.processor.newSerializer(writer);
            final Result<List<BusinessReport>, XMLSyntaxError> result = results.getResult(CreateReportsAction.KEY);
            for (final BusinessReport node : result.getObject()) {
                serializer.serializeNode(node.getContent());
            }
            Printer.writeOut(writer.toString());
        } catch (final SaxonApiException e) {
            log.error("Error while printing result to stdout", e);
        }

        return Util.createResult(KEY, true, createReport());
    }
}
