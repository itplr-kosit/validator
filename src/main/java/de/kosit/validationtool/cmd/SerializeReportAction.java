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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.impl.tasks.CheckAction;

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

    @Override
    public void check(Bag results) {
        final Path file = outputDirectory.resolve(results.getName() + "-report.xml");
        try {
            log.info("Serializing result to {}", file.toAbsolutePath());
            final Serializer serializer = ObjectFactory.createProcessor().newSerializer(file.toFile());
            serializer.serializeNode(results.getReport());
        } catch (SaxonApiException e) {
            log.error("Can not serialize result report to {}", file.toAbsolutePath(), e);
        }
    }

    @Override
    public boolean isSkipped(Bag results) {
        if (results.getReport() == null) {
            log.warn("Can not serialize result report. No document found");
            return true;
        }
        return false;
    }
}
