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
package org.kosit.validator.impl.tasks;

import org.kosit.validator.model.xvrl.XVRLReport;

import net.sf.saxon.s9api.XdmNode;

/**
 * Result object for business report e.g. user defined transformation output.
 * 
 * @author apenski
 */
public class BusinessReport {

    private String name;

    private XdmNode content;

    private XVRLReport report;

    public String getName() {
        return this.name;
    }

    public XdmNode getContent() {
        return this.content;
    }

    public XVRLReport getReport() {
        return this.report;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setContent(final XdmNode content) {
        this.content = content;
    }

    public void setReport(final XVRLReport report) {
        this.report = report;
    }
}
