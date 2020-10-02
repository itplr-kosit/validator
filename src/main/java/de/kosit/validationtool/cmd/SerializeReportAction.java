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

import de.kosit.validationtool.impl.tasks.CheckAction;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;

/**
 * Schreibt das Prüfergebnis als XML-Dokument an eine definierte Stelle.
 * 
 * @author Andreas Penski
 */
@Slf4j
@RequiredArgsConstructor
class SerializeReportAction implements CheckAction {

    private final Path outputDirectory;

    private final Processor processor;

    private final NamingStrategy namingStrategy;

    @Override
    public void check(final Bag results) {
        final Path file = this.outputDirectory.resolve(this.namingStrategy.createName(results.getName()));
        try {
            log.info("Serializing result to {}", file.toAbsolutePath());
            final Serializer serializer = this.processor.newSerializer(file.toFile());
            serializer.serializeNode(results.getReport());
        } catch (final SaxonApiException e) {
            log.error("Can not serialize result report to {}", file.toAbsolutePath(), e);
        }
    }

    @Override
    public boolean isSkipped(final Bag results) {
        if (results.getReport() == null) {
            log.warn("Can not serialize result report. No document found");
            return true;
        }
        return false;
    }
}
