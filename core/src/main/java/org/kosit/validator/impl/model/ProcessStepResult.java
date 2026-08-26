package org.kosit.validator.impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kosit.validator.impl.tasks.CheckTask;
import org.kosit.xvrl.model.XVRLReport;

public class ProcessStepResult<T, E> {

    private final CheckTask.Process.ProcessKey<T, E> key;

    private SingleProcessingResult<T, E> result;

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

    public CheckTask.Process.ProcessKey<T, E> getKey() {
        return this.key;
    }

    public SingleProcessingResult<T, E> getResult() {
        return this.result;
    }

    public List<XVRLReport> getReport() {
        return this.report;
    }

    public void setResult(final SingleProcessingResult<T, E> result) {
        this.result = result;
    }

    public ProcessStepResult(final CheckTask.Process.ProcessKey<T, E> key) {
        this.key = key;
    }
}
