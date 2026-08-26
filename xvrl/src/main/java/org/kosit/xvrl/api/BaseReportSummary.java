package org.kosit.xvrl.api;

import java.util.List;

import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.xvrl.model.XVRLReportType;
import org.kosit.xvrl.model.XVRLReportsType;

public interface BaseReportSummary {

    List<XVRLReportType> getReports();

    List<XVRLReportsType> getReportSummaries();

    @ReturnsImmutableObject
    default List<String> getAllErrors() {
        return getReports().stream().flatMap(xvrlReport -> xvrlReport.getAllErrors().stream()).toList();
    }
}
