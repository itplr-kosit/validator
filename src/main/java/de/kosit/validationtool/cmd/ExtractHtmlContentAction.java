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

package de.kosit.validationtool.cmd;

import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.HtmlExtractor;
import de.kosit.validationtool.impl.tasks.CheckAction;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

/**
 * Extrahiert HTML-Dokumente aus dem Report und persistiert diese im konfigurierten Ausgabe-Verzeichnis.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
class ExtractHtmlContentAction implements CheckAction {

    private static final QName NAME_ATTRIBUTE = new QName("data-report-type");

    private final Path outputDirectory;

    private HtmlExtractor htmlExtraction;

    private Processor processor;

    public ExtractHtmlContentAction(final Processor p, final Path outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.htmlExtraction = new HtmlExtractor(p);
        this.processor = p;
    }

    @Override
    public void check(final Bag results) {
        this.htmlExtraction.extract(results.getReport()).forEach(i -> print(results.getName(), i));
    }

    private void print(final String origName, final XdmItem xdmItem) {
        final XdmNode node = (XdmNode) xdmItem;
        final String name = origName + "-" + node.getAttributeValue(NAME_ATTRIBUTE);
        final Path file = this.outputDirectory.resolve(name + ".html");
        final Serializer serializer = this.processor.newSerializer(file.toFile());
        try {
            log.info("Writing report html '{}' to {}", name, file.toAbsolutePath());
            serializer.serializeNode(node);
        } catch (final SaxonApiException e) {
            log.error("Error extracting html content to {}", file.toAbsolutePath(), e);
        }
    }

    @Override
    public boolean isSkipped(final Bag results) {
        if (results.getReport() == null) {
            log.warn("Can not extract html content. No report document found");
            return true;
        }
        return false;
    }
}
