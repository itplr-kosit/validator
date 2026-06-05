package org.kosit.validator.cmd;

import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.tasks.CheckAction;
import org.kosit.xvrl.model.XVRLReport;

class Util {

    public static <T, E> ProcessStepResult<T, E> createResult(final CheckAction.Process.Key<T, E> key, final T result,
            final XVRLReport report) {
        final ProcessStepResult<T, E> processStepResult = new ProcessStepResult<>(key);
        processStepResult.setResult(new Result<>(result));
        processStepResult.setReport(report);
        return processStepResult;

    }

}
