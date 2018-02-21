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

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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

    public static final String MESSAGE_OPEN_STREAM_ERROR = "Can not open stream from";

    @Getter
    private final String algorithm;

    InputFactory() {
        this(null);
    }

    InputFactory(String specifiedAlgorithm) {
        this.algorithm = isNotEmpty(specifiedAlgorithm) ? specifiedAlgorithm : DEFAULT_ALGORITH;
        createDigest();
    }

    /**
     * Liest einen Prüfling von dem übergebenen Pfad. Es wird der Default-Prüfsummenalgorithmus zur Ermittlung der Prüfsumme
     * genutzt.
     *
     * @param path der Prüflings
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(Path path) {
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
    public static Input read(Path path, String digestAlgorithm) {
        checkNull(path);
        try ( InputStream stream = Files.newInputStream(path) ) {
            return read(stream, path.toString(), digestAlgorithm);
        } catch (IOException e) {
            throw new IllegalArgumentException(MESSAGE_OPEN_STREAM_ERROR + path, e);
        }
    }

    /**
     * Liest einen Prüfling von der übergebenen Datei. Es wird der Default-Prüfsummenalgorithmus zur Ermittlung der
     * Prüfsumme genutzt.
     *
     * @param file der Prüflings
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(File file) {
        return read(file, DEFAULT_ALGORITH);
    }

    /**
     * Liest einen Prüfling von der übergebenen URL. Es wird der Default-Prüfsummenalgorithmus zur Ermittlung der Prüfsumme
     * genutzt.
     * 
     * @param url URL des Prüflings
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(URL url) {
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
    public static Input read(URL url, String digestAlgorithm) {
        checkNull(url);
        try {
            return read(url.openStream(), url.getFile(), digestAlgorithm);
        } catch (IOException e) {
            throw new IllegalArgumentException(MESSAGE_OPEN_STREAM_ERROR + url, e);
        }
    }

    /**
     * Liest einen Prüfling von der übergebenen URL. Es wird ein definierter Algorithmis zur Ermittlung der Prüfsumme
     * genutzt.
     *
     * @param file der Prüflings
     * @param digestAlgorithm der Prüfsummenalgorithmus
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(File file, String digestAlgorithm) {
        checkNull(file);
        try {
            return read(file.toURI().toURL(), digestAlgorithm);
        } catch (IOException e) {
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
    public static Input read(byte[] input, String name) {
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
    public static Input read(byte[] input, String name, String digestAlgorithm) {
        checkNull(input);
        return read(new ByteArrayInputStream(input), name, digestAlgorithm);
    }

    private static void checkNull(Object input) {
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
    public static Input read(InputStream inputStream, String name) {
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
    public static Input read(InputStream inputStream, String name, String digestAlgorithm) {
        return new InputFactory(digestAlgorithm).readStream(inputStream, name);
    }

    private Input readStream(InputStream inputStream, String name) {
        if (StringUtils.isNotBlank(name)) {
            log.debug("Generating hashcode for {} using {} algorithm", name, getAlgorithm());
            MessageDigest digest = createDigest();
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            try ( BufferedInputStream bis = new BufferedInputStream(inputStream);
                  DigestInputStream dis = new DigestInputStream(bis, digest);
                  ByteArrayOutputStream out = new ByteArrayOutputStream() ) {

                // read the file and update the hash calculation
                int n;
                while (EOF != (n = dis.read(buffer))) {
                    out.write(buffer, 0, n);
                }
                // get the hash value as byte array
                byte[] hash = digest.digest();
                log.debug("Generated hashcode for {} is {}", name, DatatypeConverter.printHexBinary(hash));
                out.flush();
                return new Input(out.toByteArray(), name, hash, digest.getAlgorithm());
            } catch (IOException e) {
                throw new IllegalArgumentException(MESSAGE_OPEN_STREAM_ERROR + name, e);
            }
        } else {
            throw new IllegalArgumentException("Must supply a valid name/identifier for the input");
        }
    }

    private MessageDigest createDigest() {
        try {
            MessageDigest digest;
            digest = MessageDigest.getInstance(getAlgorithm());
            return digest;
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            throw new IllegalStateException(String.format("Specified method %s is not available", getAlgorithm()), e);
        }
    }

}
