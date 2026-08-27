package org.kosit.xvrl.api;

import java.util.List;

import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.xvrl.model.XvrlReportType;
import org.kosit.xvrl.model.XvrlReportsType;

public interface BaseReportSummary {

    List<XvrlReportType> getReports();

    List<XvrlReportsType> getReportSummaries();

    @ReturnsImmutableObject
    default List<String> getAllErrors() {
        return getReports().stream().flatMap(xvrlReport -> xvrlReport.getAllErrors().stream()).toList();
    }
}
