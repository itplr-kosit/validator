package org.kosit.xvrl.impl;

import java.io.Serializable;
import java.util.List;

import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.xvrl.api.BaseReportSummary;
import org.kosit.xvrl.model.XVRLReportType;
import org.kosit.xvrl.model.XVRLReportsType;

/**
 * Base class for XVRLReportSummary to overcome the issue that simplifying and pluralizing the choice elements of
 * xvrl:reports node (xvrl:report, xvrl:reports and xvrl:detection) leads to name clash in the resulting list
 * attributes. during JAXB generation of the XVRLReportSummary class simplifying/pluralizing and renaming of the element
 * node is not able to being executed together.
 */
public abstract class AbstractXVRLReportSummary implements BaseReportSummary, Serializable {

    public abstract List<Serializable> getReportOrReportsOrDigest();

    @Override
    @ReturnsImmutableObject
    public List<XVRLReportType> getReports() {
        return getReportOrReportsOrDigest().stream().filter(XVRLReportType.class::isInstance).map(XVRLReportType.class::cast).toList();
    }

    @Override
    @ReturnsImmutableObject
    public List<XVRLReportsType> getReportSummaries() {
        return getReportOrReportsOrDigest().stream().filter(XVRLReportsType.class::isInstance).map(XVRLReportsType.class::cast).toList();
    }
}
