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

package de.kosit.validationtool.impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import de.kosit.validationtool.impl.tasks.CheckAction;
import de.kosit.validationtool.model.xvrl.XVRLReport;

@Getter
@Setter
@RequiredArgsConstructor
public class ProcessStepResult<T, E> {

    private final CheckAction.Process.Key<T, E> key;

    private Result<T, E> result;

    @Setter(AccessLevel.NONE)
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
}
