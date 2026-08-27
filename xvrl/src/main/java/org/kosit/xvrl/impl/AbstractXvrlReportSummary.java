package org.kosit.xvrl.impl;

import java.io.Serializable;
import java.util.List;

import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.xvrl.api.BaseReportSummary;
import org.kosit.xvrl.model.XvrlReportType;
import org.kosit.xvrl.model.XvrlReportsType;

/**
 * Base class for XvrlReportSummary to overcome the issue that simplifying and pluralizing the choice elements of
 * xvrl:reports node (xvrl:report, xvrl:reports and xvrl:detection) leads to name clash in the resulting list
 * attributes. during JAXB generation of the XvrlReportSummary class simplifying/pluralizing and renaming of the element
 * node is not able to being executed together.
 */
public abstract class AbstractXvrlReportSummary implements BaseReportSummary {

    public abstract List<Serializable> getReportOrReportsOrDigest();

    @Override
    @ReturnsImmutableObject
    public List<XvrlReportType> getReports() {
        return getReportOrReportsOrDigest().stream().filter(XvrlReportType.class::isInstance).map(XvrlReportType.class::cast).toList();
    }

    @Override
    @ReturnsImmutableObject
    public List<XvrlReportsType> getReportSummaries() {
        return getReportOrReportsOrDigest().stream().filter(XvrlReportsType.class::isInstance).map(XvrlReportsType.class::cast).toList();
    }
}
