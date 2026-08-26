package org.kosit.xvrl.impl;

import java.io.Serializable;
import java.util.List;

import org.kosit.xvrl.api.BaseReportSummary;
import org.kosit.xvrl.model.XVRLReport;
import org.kosit.xvrl.model.XVRLReportSummary;

/**
 * Base class for XVRLReportSummary to overcome the issue that simplifying and pluralizing the choice elements of
 * xvrl:reports node (xvrl:report, xvrl:reports and xvrl:detection) leads to name clash in the resulting list
 * attributes. during JAXB generation of the XVRLReportSummary class simplifying/pluralizing and renaming of the element
 * node is not able to being executed together.
 */
public abstract class AbstractXVRLReportSummary implements BaseReportSummary, Serializable {

    public abstract List<Serializable> getReportOrReportsOrDigest();

    @Override
    public List<XVRLReport> getReports() {
        return getReportOrReportsOrDigest().stream().filter(XVRLReport.class::isInstance).map(XVRLReport.class::cast).toList();
    }

    @Override
    public List<XVRLReportSummary> getReportSummaries() {
        return getReportOrReportsOrDigest().stream().filter(XVRLReportSummary.class::isInstance).map(XVRLReportSummary.class::cast)
                .toList();
    }
}
