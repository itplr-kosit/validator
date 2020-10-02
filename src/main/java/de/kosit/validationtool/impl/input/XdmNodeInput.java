/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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

package de.kosit.validationtool.impl.input;

import javax.xml.transform.Source;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import de.kosit.validationtool.api.Input;

import net.sf.saxon.s9api.XdmNode;

/**
 * An {@link Input} implementation holding saxon's {@link XdmNode} object.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Getter
public class XdmNodeInput implements Input {

    private final XdmNode node;

    private final String name;

    private final String digestAlgorithm;

    private final byte[] hashCode;

    @Override
    public Source getSource() {
        // usually not neccessary to be called.
        return this.node.getUnderlyingNode();
    }
}
