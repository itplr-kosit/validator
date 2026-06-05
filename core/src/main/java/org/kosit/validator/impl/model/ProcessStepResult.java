package org.kosit.validator.impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kosit.validator.impl.tasks.CheckAction;
import org.kosit.xvrl.model.XVRLReport;

public class ProcessStepResult<T, E> {

    private final CheckAction.Process.Key<T, E> key;

    private Result<T, E> result;

    private List<XVRLReport> report;

    public void setReport(final XVRLReport singleReport) {
        this.report = Collections.singletonList(singleReport);
    }

    public void addReports(final List<XVRLReport> collect) {
        if (collect != null) {
            if (this.report == null) {
                this.report = new ArrayList<>();
            }
            this.report.addAll(collect);
        }
    }

    public CheckAction.Process.Key<T, E> getKey() {
        return this.key;
    }

    public Result<T, E> getResult() {
        return this.result;
    }

    public List<XVRLReport> getReport() {
        return this.report;
    }

    public void setResult(final Result<T, E> result) {
        this.result = result;
    }

    public ProcessStepResult(final CheckAction.Process.Key<T, E> key) {
        this.key = key;
    }
}
