package org.kosit.validator.cmd;

import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.tasks.CheckTask;
import org.kosit.xvrl.model.XVRLReport;

class Util {

    public static <T, E> ProcessStepResult<T, E> createResult(final CheckTask.Process.ProcessKey<T, E> key, final T result,
            final XVRLReport report) {
        final ProcessStepResult<T, E> processStepResult = new ProcessStepResult<>(key);
        processStepResult.setResult(new SingleProcessingResult<>(result));
        processStepResult.setReport(report);
        return processStepResult;

    }

}
