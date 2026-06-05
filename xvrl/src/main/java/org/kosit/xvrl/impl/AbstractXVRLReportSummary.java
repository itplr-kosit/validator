package org.kosit.xvrl.impl;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;
import java.util.stream.Collectors;

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

    static class FilteredList<T extends Serializable> extends AbstractList<T> {

        private final List<Serializable> unfiltered;

        private final Class<T> type;

        public FilteredList(List<Serializable> unfiltered, Class<T> type) {
            this.unfiltered = unfiltered;
            this.type = type;
        }

        @Override
        public T get(int index) {
            return type.cast(unfiltered.stream().filter(type::isInstance).collect(Collectors.toList()).get(index));
        }

        @Override
        public int size() {
            return (int) unfiltered.stream().filter(type::isInstance).count();
        }

        @Override
        public boolean add(T element) {
            return unfiltered.add(element);
        }
    }

    public abstract List<Serializable> getReportOrReportsOrDigest();

    @Override
    public List<XVRLReport> getReports() {
        return new FilteredList<>(getReportOrReportsOrDigest(), XVRLReport.class);
    }

    @Override
    public List<XVRLReportSummary> getReportSummaries() {
        return new FilteredList<>(getReportOrReportsOrDigest(), XVRLReportSummary.class);
    }
}
