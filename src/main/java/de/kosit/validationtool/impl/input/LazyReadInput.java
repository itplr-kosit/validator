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

import java.io.InputStream;

import de.kosit.validationtool.api.Input;

/**
 * Internal interface used for lazy generation of the hashcode for document identification.
 * 
 * @see StreamHelper#wrapDigesting(LazyReadInput, InputStream, String) for details
 * @author Andreas Penski
 */
interface LazyReadInput {

    /**
     * Sets a hashcode
     * 
     * @param digest the digest
     */
    void setHashCode(byte[] digest);

    /**
     * Determines whether a hashcode has been computed yet
     * 
     * @return true when computed
     */
    boolean isHashcodeComputed();

    /**
     * Setting the length of the {@link Input}.
     * 
     * @param length the length
     */
    void setLength(long length);

}
