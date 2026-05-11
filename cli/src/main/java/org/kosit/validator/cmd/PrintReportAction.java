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
package org.kosit.validator.cmd;

import java.io.StringWriter;
import java.util.List;

import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.tasks.BusinessReport;
import org.kosit.validator.impl.tasks.CheckAction;
import org.kosit.validator.impl.tasks.CreateReportsAction;
import org.kosit.validator.impl.xvrl.XVRLReportBuilder;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.validator.model.xvrl.XVRLReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;

/**
 * Prints the result document to stdout.
 *
 * @author Andreas Penski
 */
class PrintReportAction implements CheckAction {

    private static final Logger log = LoggerFactory.getLogger(PrintReportAction.class);

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

    public PrintReportAction(final Processor processor) {
        this.processor = processor;
    }
}
