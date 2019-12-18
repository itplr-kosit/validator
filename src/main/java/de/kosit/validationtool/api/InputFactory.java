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

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.input.ByteArrayInput;
import de.kosit.validationtool.impl.input.ResourceInput;
import de.kosit.validationtool.impl.input.SourceInput;
import de.kosit.validationtool.impl.input.StreamHelper;

/**
 * Service zum Einlesen des Test-Objekts in den Speicher. Beim Einlesen wird gleichzeitig eine Prüfsumme ermittelt und
 * mit dem Ergebnis mitgeführt.
 * 
 * @author Andreas Penski
 */
@Slf4j
public class InputFactory {

    static final String DEFAULT_ALGORITH = "SHA-256";

    private static final int EOF = -1;

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private static final String MESSAGE_OPEN_STREAM_ERROR = "Can not open stream from";

    @Getter
    private final String algorithm;

    InputFactory() {
        this(null);
    }

    InputFactory(final String specifiedAlgorithm) {
        this.algorithm = isNotEmpty(specifiedAlgorithm) ? specifiedAlgorithm : DEFAULT_ALGORITH;
        // check validity
        StreamHelper.createDigest(this.algorithm);
    }

    /**
     * Liest einen Prüfling von dem übergebenen Pfad. Es wird der Default-Prüfsummenalgorithmus zur Ermittlung der Prüfsumme
     * genutzt.
     *
     * @param path der Prüflings
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(final Path path) {
        return read(path, DEFAULT_ALGORITH);
    }

    /**
     * Liest einen Prüfling von der übergebenen URL. Es wird ein definierter Algorithmis zur Ermittlung der Prüfsumme
     * genutzt.
     *
     * @param path der Prüflings
     * @param digestAlgorithm der Prüfsummenalgorithmus
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(final Path path, final String digestAlgorithm) {
        checkNull(path);
        return read(path.toUri(), digestAlgorithm);
    }

    /**
     * Liest einen Prüfling von der übergebenen Datei. Es wird der Default-Prüfsummenalgorithmus zur Ermittlung der
     * Prüfsumme genutzt.
     *
     * @param file der Prüflings
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(final File file) {
        return read(file, DEFAULT_ALGORITH);
    }


    /**
     * Liest einen Prüfling von der übergebenen URI. Es wird der Default-Prüfsummenalgorithmus zur Ermittlung der Prüfsumme
     * genutzt.
     *
     * @param uri URI des Prüflings
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(final URI uri) {
        return read(uri, DEFAULT_ALGORITH);
    }

    /**
     * Liest einen Prüfling von der übergebenen URL. Es wird ein definierter Algorithmis zur Ermittlung der Prüfsumme
     * genutzt.
     *
     * @param uri URI des Prüflings
     * @param digestAlgorithm der Prüfsummenalgorithmus
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(final URI uri, final String digestAlgorithm) {
        try {
            return read(uri.toURL(), digestAlgorithm);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Can not read from uri %s Not a valid uri supplied", uri));
        }
    }

    /**
     * Liest einen Prüfling von der übergebenen URL. Es wird der Default-Prüfsummenalgorithmus zur Ermittlung der Prüfsumme
     * genutzt.
     *
     * @param url URL des Prüflings
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(final URL url) {
        return read(url, DEFAULT_ALGORITH);
    }

    /**
     * Liest einen Prüfling von der übergebenen URL. Es wird ein definierter Algorithmis zur Ermittlung der Prüfsumme
     * genutzt.
     * 
     * @param url URL des Prüflings
     * @param digestAlgorithm der Prüfsummenalgorithmus
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(final URL url, final String digestAlgorithm) {
        checkNull(url);
        checkNotEmpty(url.getFile());
        try {
            final URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
        } catch (final IOException e) {
            throw new IllegalArgumentException(MESSAGE_OPEN_STREAM_ERROR + url, e);
        }
        return new ResourceInput(url, url.getFile(), digestAlgorithm);

    }

    /**
     * Reads a test document from a {@link Source}.
     *
     * @param source source
     * @return an {@link Input}
     */
    public static Input read(final StreamSource source) {
        return read(source, DEFAULT_ALGORITH);
    }

    /**
     * Reads a test document from a {@link Source} using a specified digest algorithm.
     *
     * @param source source
     * @param digestAlgorithm the digest algorithm
     * @return an {@link Input}
     */
    public static Input read(final StreamSource source, final String digestAlgorithm) {
        return read(source, digestAlgorithm, null);
    }

    /**
     * Reads a test document from a {@link Source} using a specified digest algorithm.
     *
     * @param source source
     * @param digestAlgorithm the digest algorithm
     * @return an {@link Input}
     */
    public static Input read(final Source source, final String digestAlgorithm, final byte[] hashcode) {
        checkNull(source);
        return new SourceInput(source, source.getSystemId(), digestAlgorithm, hashcode);
    }

    /**
     * Liest einen Prüfling von der übergebenen URL. Es wird ein definierter Algorithmis zur Ermittlung der Prüfsumme
     * genutzt.
     *
     * @param file der Prüflings
     * @param digestAlgorithm der Prüfsummenalgorithmus
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(final File file, final String digestAlgorithm) {
        checkNull(file);
        try {
            return read(file.toURI().toURL(), digestAlgorithm);
        } catch (final IOException e) {
            throw new IllegalArgumentException(MESSAGE_OPEN_STREAM_ERROR + file, e);
        }

    }

    /**
     * Liest einen Prüfling von der übergebenen byte-Sequenz. Es wird ein definierter Algorithmis zur Ermittlung der
     * Prüfsumme genutzt.
     *
     * @param input URL des Prüflings
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(final byte[] input, final String name) {
        checkNull(input);
        return read(input, name, DEFAULT_ALGORITH);
    }

    /**
     * Liest einen Prüfling von der übergebenen byte-Sequenz. Es wird ein definierter Algorithmis zur Ermittlung der
     * Prüfsumme genutzt.
     *
     * @param input URL des Prüflings
     * @param digestAlgorithm der Prüfsummenalgorithmus
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(final byte[] input, final String name, final String digestAlgorithm) {
        checkNull(input);
        checkNotEmpty(name);
        return new ByteArrayInput(input, name, digestAlgorithm);
    }

    private static void checkNotEmpty(final String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Input name can not be null");
        }
    }

    private static void checkNull(final Object input) {
        if (input == null) {
            throw new IllegalArgumentException("Input can not be null");
        }
    }

    /**
     * Liest einen Prüfling vom übergebenen {@link InputStream}.
     * 
     * @param inputStream der {@link InputStream}
     * @param name der Name/Bezeichner des Prüflings
     * @return einen Prüfling in eingelesener Form
     */
    public static Input read(final InputStream inputStream, final String name) {
        return read(inputStream, name, DEFAULT_ALGORITH);
    }

    /**
     * Liest einen Prüfling vom übergebenen {@link InputStream}.
     *
     * @param inputStream der {@link InputStream}
     * @param name der Name/Bezeichner des Prüflings
     * @param digestAlgorithm der Prüfsummenalgorithmus
     * @return einen Prüfling in eingelesener Form
     */
    public static Input read(final InputStream inputStream, final String name, final String digestAlgorithm) {
        checkNull(inputStream);
        return read(new StreamSource(inputStream, name), digestAlgorithm);
    }

}
