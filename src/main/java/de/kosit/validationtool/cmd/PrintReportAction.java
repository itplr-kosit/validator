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

import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kosit.validationtool.impl.Printer;
import de.kosit.validationtool.impl.tasks.CheckAction;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;

/**
 * Outputs the result document to std-out.
 * 
 * @author Andreas Penski
 */
class PrintReportAction implements CheckAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrintReportAction.class);

    private final Processor processor;

    @Override
    public void check(final Bag results) {
        try {
            final StringWriter writer = new StringWriter();
            final Serializer serializer = this.processor.newSerializer(writer);
            serializer.serializeNode(results.getReport());
            Printer.writeOut(writer.toString());
        } catch (final SaxonApiException e) {
            LOGGER.error("Error while printing result to stdout", e);
        }
    }

    public PrintReportAction(final Processor processor) {
        this.processor = processor;
    }
}
