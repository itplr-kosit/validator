/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.api;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;

/**
 * An input for the validator.
 *
 * @author apenski
 */

public interface Input {

    /**
     * The name of the input for document identification
     * 
     * @return the name
     */
    String getName();

    /**
     * The hashcode for document identification
     * 
     * @return the computed hashcode
     */
    byte[] getHashCode();

    /**
     * The digest algorithm used for computing the {@link #getHashCode()}
     * 
     * @return the name of the digest algorith
     */
    String getDigestAlgorithm();

    /**
     * Opens a new {@link InputStream } for this input which carries the actual data
     * 
     * @return an open {@link InputStream}
     * @throws IOException on I/O while opening the stream
     */
    Source getSource() throws IOException;

}
