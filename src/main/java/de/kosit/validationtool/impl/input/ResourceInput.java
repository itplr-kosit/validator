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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import de.kosit.validationtool.api.Input;

/**
 * An {@link Input} carries an {@link URL} which can be used for all 'locatable' inputs such as {@link File},
 * {@link java.nio.file.Path} and any other {@link URL}.
 * 
 * This stream is NOT read into memory. So this implementation has good in memory efficieny. The validation process MAY
 * read the stream more than once. Make sure, that the {@link URL} points to fast I/O devices
 * 
 * @author Andreas Penski
 */
@Getter
@RequiredArgsConstructor
public class ResourceInput extends AbstractInput {

    private final URL url;

    private final String name;

    private final String digestAlgorithm;

    @Override
    public Source getSource() throws IOException {
        InputStream stream = this.url.openStream();
        if (!isHashcodeComputed()) {
            stream = StreamHelper.wrapDigesting(this, stream, getDigestAlgorithm());
        }
        return new StreamSource(stream, this.name);
    }
}
