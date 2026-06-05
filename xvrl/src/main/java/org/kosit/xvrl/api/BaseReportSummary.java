package org.kosit.xvrl.api;

import java.util.List;
import java.util.stream.Collectors;

import org.kosit.xvrl.model.XVRLReport;
import org.kosit.xvrl.model.XVRLReportSummary;

public interface BaseReportSummary {

    List<XVRLReport> getReports();

    List<XVRLReportSummary> getReportSummaries();

    default List<String> getAllErrors() {
        return getReports().stream().flatMap(xvrlReport -> xvrlReport.getAllErrors().stream()).collect(Collectors.toList());
    }
}
