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

package de.kosit.validationtool.impl.tasks;

import lombok.Getter;
import lombok.Setter;

import de.kosit.validationtool.model.xvrl.XVRLReport;

import net.sf.saxon.s9api.XdmNode;

/**
 * Result object for business report e.g. user defined transformation output.
 * 
 * @author apenski
 */
@Getter
@Setter
public class BusinessReport {

    private String name;

    private XdmNode content;

    private XVRLReport report;
}
