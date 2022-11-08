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

import java.text.NumberFormat;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.model.ProcessStepResult;
import de.kosit.validationtool.impl.xvrl.XVRLReportBuilder;
import de.kosit.validationtool.model.xvrl.XVRLReport;

/**
 *
 * Prints some memory usage information for debugging purposes.
 * 
 * @author Andreas Penski
 */
@Slf4j
class PrintMemoryStats implements de.kosit.validationtool.impl.tasks.CheckAction {

    public static final Process.Key<Boolean, String> KEY = new Process.Key<>(Boolean.class, String.class);

    private static final int BYTES_PER_K = 1024;

    private static XVRLReport createReport() {
        return XVRLReportBuilder.builder("Document wellformedness Validator").name("Print Memory Stats").setValid().build();
    }

    @Override
    public ProcessStepResult<Boolean, String> check(final Process results) {
        final Runtime runtime = Runtime.getRuntime();
        final long maxMemory = runtime.maxMemory();
        final long allocatedMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();

        final NumberFormat format = NumberFormat.getInstance();
        final String freeStr = format.format(freeMemory / BYTES_PER_K);
        final String allocStr = format.format(allocatedMemory / BYTES_PER_K);
        final String maxStr = format.format(maxMemory / BYTES_PER_K);
        final String totalFreeStr = format.format((freeMemory + (maxMemory - allocatedMemory)) / BYTES_PER_K);
        log.info("free memory: {}MB; allocated memory: {}MB", freeStr, allocStr);
        log.info("max memory: {}MB; total free memory: {}MB", maxStr, totalFreeStr);
        return Util.createResult(KEY, true, createReport());
    }
}
