package org.kosit.validator.cmd;

import java.text.NumberFormat;

import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.xvrl.XVRLReportBuilder;
import org.kosit.xvrl.model.XVRLReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prints some memory usage information for debugging purposes.
 * 
 * @author Andreas Penski
 */
class PrintMemoryStats implements org.kosit.validator.impl.tasks.CheckTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrintMemoryStats.class);

    public static final Process.ProcessKey<Boolean, String> KEY = new Process.ProcessKey<>(Boolean.class, String.class);

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
        LOGGER.info("free memory: {}MB; allocated memory: {}MB", freeStr, allocStr);
        LOGGER.info("max memory: {}MB; total free memory: {}MB", maxStr, totalFreeStr);
        return Util.createResult(KEY, true, createReport());
    }
}
