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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.tasks.CheckAction;

/**
 * Serializes the {@link de.kosit.validationtool.model.reportInput.CreateReportInput report input} document.
 *
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class SerializeReportInputAction implements CheckAction {

    private final Path outputDirectory;

    private final ConversionService conversionService;

    @Override
    public void check(final Bag results) {
        final Path file = this.outputDirectory.resolve(results.getName() + "-reportInput.xml");
        try {
            log.info("Serializing result to {}", file.toAbsolutePath());
            final String xml = this.conversionService.writeXml(results.getReportInput());
            Files.write(file, xml.getBytes());
        } catch (final IOException e) {
            log.error("Can not serialize result report to {}", file.toAbsolutePath(), e);
        }
    }

    @Override
    public boolean isSkipped(final Bag results) {
        if (results.getReportInput() == null) {
            log.warn("Can not serialize  report input. No object found");
            return true;
        }
        return false;
    }
}
