package org.kosit.validator.impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kosit.validator.impl.tasks.CheckTask;
import org.kosit.xvrl.model.XvrlReport;

public class ProcessStepResult<T, E> {

    private final CheckTask.Process.ProcessKey<T, E> key;

    private SingleProcessingResult<T, E> result;

    private List<XvrlReport> report;

    public ProcessStepResult(final CheckTask.Process.ProcessKey<T, E> key) {
        this.key = key;
    }

    public CheckTask.Process.ProcessKey<T, E> getKey() {
        return this.key;
    }

    public List<XvrlReport> getReport() {
        return this.report;
    }

    public void addReports(final List<XvrlReport> collect) {
        if (collect != null) {
            if (this.report == null) {
                this.report = new ArrayList<>();
            }
            this.report.addAll(collect);
        }
    }

    public void setReport(final XvrlReport singleReport) {
        this.report = Collections.singletonList(singleReport);
    }

    public SingleProcessingResult<T, E> getResult() {
        return this.result;
    }

    public void setResult(final SingleProcessingResult<T, E> result) {
        this.result = result;
    }
}
