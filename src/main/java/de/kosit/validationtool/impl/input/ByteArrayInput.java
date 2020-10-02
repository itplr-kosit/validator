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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Classical in-memory {@link de.kosit.validationtool.api.Input}. It is not memory efficient to read the whole file into
 * memory prio validating. Consider using the {@link ResourceInput}.
 * 
 * @author Andreas Penski
 */
@Getter
@AllArgsConstructor
public class ByteArrayInput extends AbstractInput {

    private final byte[] content;

    private final String name;

    private final String digestAlgorithm;

    @Override
    public long getLength() {
        return this.content != null ? this.content.length : 0;
    }

    @Override
    public Source getSource() {
        final InputStream stream = wrap(new ByteArrayInputStream(this.content));
        return new StreamSource(stream, getName());
    }

}
