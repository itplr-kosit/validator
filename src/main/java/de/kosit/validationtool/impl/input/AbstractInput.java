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

import static de.kosit.validationtool.impl.input.StreamHelper.drain;

import java.io.IOException;
import java.io.InputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Input;

/**
 * Base class for all {@link Input Inputs}.
 * 
 * @author Andreas Penski
 */
@Slf4j
public abstract class AbstractInput implements Input, LazyReadInput {

    private byte[] hashCode;

    @Getter
    @Setter
    private long length;

    @Override
    public byte[] getHashCode() {
        if (this.hashCode == null) {
            log.warn("Extra calculating hashcode. This is in-efficient in most cases");
            computeHashcode();
        }
        return this.hashCode;
    }

    protected void computeHashcode() {
        try {
            drain(this);
        } catch (final IOException e) {
            log.error("Error extra computing hashcode", e);
        }
    }

    protected InputStream wrap(final InputStream stream) {
        InputStream result = stream;
        if (!isHashcodeComputed()) {
            result = StreamHelper.wrapDigesting(this, result, getDigestAlgorithm());
        }
        if (getLength() == 0) {
            result = StreamHelper.wrapCount(this, result);
        }
        return result;
    }

    @Override
    public boolean isHashcodeComputed() {
        return this.hashCode != null;
    }

    @Override
    public void setHashCode(final byte[] digest) {
        this.hashCode = digest;
    }

    public boolean supportsMultipleReads() {
        return true;
    }
}
