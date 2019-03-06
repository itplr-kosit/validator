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

import java.text.NumberFormat;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * Prints some memory usage information for debugging purposes.
 * 
 * @author Andreas Penski
 */
@Slf4j
class PrintMemoryStats implements de.kosit.validationtool.impl.tasks.CheckAction {

    private static final int BYTES_PER_K = 1024;

    @Override
    public void check(final Bag results) {
        final Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        NumberFormat format = NumberFormat.getInstance();
        final String freeStr = format.format(freeMemory / BYTES_PER_K);
        final String allocStr = format.format(allocatedMemory / BYTES_PER_K);
        final String maxStr = format.format(maxMemory / BYTES_PER_K);
        final String totalFreeStr = format.format((freeMemory + (maxMemory - allocatedMemory)) / BYTES_PER_K);
        log.info("free memory: {}MB; allocated memory: {}MB", freeStr, allocStr);
        log.info("max memory: {}MB; total free memory: {}MB", maxStr, totalFreeStr);
    }
}
