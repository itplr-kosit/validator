/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kosit.validator.impl.xvrl;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;
import java.util.stream.Collectors;

import org.kosit.validator.model.xvrl.XVRLReport;
import org.kosit.validator.model.xvrl.XVRLReportSummary;
import org.kosit.validator.api.xvrl.BaseReportSummary;

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
