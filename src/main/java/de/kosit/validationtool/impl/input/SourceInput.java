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

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.input.ReaderInputStream;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import net.sf.saxon.om.TreeInfo;

/**
 * A validator {@link de.kosit.validationtool.api.Input} based on a {@link Source}.
 * <p>
 * Note: The various implementations of {@link Source} varies wether the can be read twice or no. This implementation
 * tries to handle this with respect document identification (hashcode).
 * 
 * This class is known to work with:
 * <ul>
 * <li>{@link StreamSource} - both {@link java.io.InputStream} based and {@link java.io.Reader} based</li>
 * <li>{@link javax.xml.transform.dom.DOMSource}</li>
 * <li>{@link javax.xml.bind.util.JAXBSource}</li>
 * <li>{@link TreeInfo}</li>
 * </ul>
 * 
 * Other {@link Source Sources} may work as well, please try and let us know.
 * 
 * @author Andreas Penski
 */
@Getter
@Slf4j
public class SourceInput extends AbstractInput {

    private final Source source;

    private final String name;

    private final String digestAlgorithm;

    public SourceInput(final StreamSource source, final String name, final String digestAlgorithm) {
        this(source, name, digestAlgorithm, null);
    }

    public SourceInput(final Source source, final String name, final String digestAlgorithm, final byte[] hashCode) {
        this.source = source;
        this.name = name;
        this.digestAlgorithm = digestAlgorithm;
        setHashCode(hashCode);
        validate();
    }

    @Override
    public String getName() {
        return defaultIfBlank(this.name, this.source.getClass().getSimpleName());
    }

    private void validate() {
        if (!isHashcodeComputed() && !isHashcodeComputationSupported()) {
            throw new IllegalStateException("Unsupported source. Only StreamSource supported yet");
        }
        if (!isHashcodeComputed() && ((StreamSource) this.source).getInputStream() == null) {
            log.warn("No hashcode supplied, will wrap the reader using system default charset");
        }
        if (!(isTreeInfo() || isDomSource() || isStreamSource() || isJaxbSource())) {
            log.warn("No known to be working Source implementation provided.");
        }
    }

    @Override
    public Source getSource() throws IOException {
        if (isConsumed()) {
            throw new IllegalStateException("A SourceInput can only read once");
        }
        return isHashcodeComputed() ? this.source : wrappedSource();
    }

    private boolean isHashcodeComputationSupported() {
        return isStreamSource();
    }

    private boolean isConsumed() throws IOException {
        if (isStreamSource()) {

            final StreamSource ss = (StreamSource) this.source;
            try {
                return (ss.getInputStream() != null && ss.getInputStream().available() == 0)
                        || (ss.getReader() != null && !ss.getReader().ready());
            } catch (final IOException e) {
                log.error("Error checking consumed state", e);
                return true;
            }
        }
        return false;
    }

    private boolean isStreamSource() {
        return this.source instanceof StreamSource;
    }

    private boolean isDomSource() {
        return this.source instanceof DOMSource;
    }

    public boolean isTreeInfo() {
        return this.source instanceof TreeInfo;
    }

    private boolean isJaxbSource() {
        return this.source instanceof JAXBSource;
    }

    private Source wrappedSource() {
        Source result = this.source;
        if (isStreamSource()) {
            final StreamSource ss = (StreamSource) this.source;
            if (ss.getInputStream() != null) {
                result = new StreamSource(wrap(ss.getInputStream()), this.source.getSystemId());
            } else if (ss.getReader() != null) {
                result = new StreamSource(wrap(new ReaderInputStream(ss.getReader(), Charset.defaultCharset())), this.source.getSystemId());
            }
        }
        return result;
    }

    @Override
    public boolean supportsMultipleReads() {
        return isDomSource() || isTreeInfo();
    }

}
