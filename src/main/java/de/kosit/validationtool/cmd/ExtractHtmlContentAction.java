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

package de.kosit.validationtool.cmd;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.tasks.CheckAction;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
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

    private final ContentRepository repository;

    private final Path outputDirectory;

    private XPathExecutable executable;

    @Override
    public void check(Bag results) {
        try {
            final XPathSelector selector = getSelector();
            final XdmNode xdmSource = results.getReport();
            selector.setContextItem(xdmSource);
            selector.forEach(m -> print(results.getName(), m));

        } catch (SaxonApiException e) {
            throw new IllegalStateException("Can not extract html content", e);
        }
    }

    private void print(String origName, XdmItem xdmItem) {
        XdmNode node = (XdmNode) xdmItem;
        final String name = origName + "-" + node.getAttributeValue(NAME_ATTRIBUTE);
        final Path file = outputDirectory.resolve(name + ".html");
        final Serializer serializer = repository.getProcessor().newSerializer(file.toFile());
        try {
            log.info("Writing report html '{}' to {}", name, file.toAbsolutePath());
            serializer.serializeNode(node);
        } catch (SaxonApiException e) {
            log.info("Error extracting html content to {}", file.toAbsolutePath(), e);
        }
    }

    private XPathSelector getSelector() {
        if (executable == null) {
            Map<String, String> ns = new HashMap<>();
            ns.put("html", "http://www.w3.org/1999/xhtml");
            executable = repository.createXPath("//html:html", ns);
        }
        return executable.load();
    }

    @Override
    public boolean isSkipped(Bag results) {
        if (results.getReport() == null) {
            log.warn("Can not extract html content. No report document found");
            return true;
        }
        return false;
    }
}
