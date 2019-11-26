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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;

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

    private static final String MESSAGE_OPEN_STREAM_ERROR = "Can not open stream from";

    @Getter
    private final String algorithm;

    InputFactory() {
        this(null);
    }

    InputFactory(final String specifiedAlgorithm) {
        this.algorithm = isNotEmpty(specifiedAlgorithm) ? specifiedAlgorithm : DEFAULT_ALGORITH;
        createDigest();
    }

    /**
     * Liest einen Prüfling von dem übergebenen Pfad. Es wird der Default-Prüfsummenalgorithmus zur Ermittlung der
     * Prüfsumme genutzt.
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
        try ( final InputStream stream = Files.newInputStream(path) ) {
            return read(stream, path.toString(), digestAlgorithm);
        } catch (final IOException e) {
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
    public static Input read(final File file) {
        return read(file, DEFAULT_ALGORITH);
    }

    /**
     * Liest einen Prüfling von der übergebenen URL. Es wird der Default-Prüfsummenalgorithmus zur Ermittlung der
     * Prüfsumme genutzt.
     *
     * @param url URL des Prüflings
     * @return ein Prüf-Eingabe-Objekt
     */
    public static Input read(final URL url) {
        return read(url, DEFAULT_ALGORITH);
    }

    public static Input read(final URI uri) {
        try {
            return read(uri.toURL(), DEFAULT_ALGORITH);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Can not read from uri %s Not a valid uri supplied", uri));
        }
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
        try {
            return read(url.openStream(), url.getFile(), digestAlgorithm);
        } catch (final IOException e) {
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
        return read(new ByteArrayInputStream(input), name, digestAlgorithm);
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
        return new InputFactory(digestAlgorithm).readStream(inputStream, name);
    }

    private Input readStream(final InputStream inputStream, final String name) {
        if (StringUtils.isNotBlank(name)) {
            final MessageDigest digest = createDigest();
            final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            final byte[] sig = new byte[4];

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            /***
             * If we want to check the file signature we need to copy the inputstream as it does not support rewind()
             * (or mark() and reset()).
             *
             * Apart from being unneccessary (we could also simply check from the file name if the input is PDF) I'm
             * doing this slow (no nio) and bad (high memory consumption)
             */
            int len;
            try {
                while ((len = inputStream.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
            } catch (final NullPointerException e1) {
                throw new IllegalArgumentException("Could not read from Null: Must supply a valid inputstream");
            } catch (final IOException e2) {

                throw new IllegalArgumentException("Could not read: Must supply a valid inputstream");

            }

            InputStream rewindedInputStream = new ByteArrayInputStream(baos.toByteArray());
            final InputStream sigCheckIS = new ByteArrayInputStream(baos.toByteArray());

            int read = 0;
            try {
                read = sigCheckIS.read(sig, 0, 4);
            } catch (final IOException e3) {
                throw new IllegalArgumentException("Could not read signature: Must supply a valid inputstream");
            }

            if (read == 4 && sig[0] == '%' && sig[1] == 'P' && sig[2] == 'D' && sig[3] == 'F') {
                /***
                 * we are reading a PDF, most likely Factur-X or ZUGFeRD, so let's extract its XML (assuming there is
                 * some)
                 */

                final ZUGFeRDImporter zi = new ZUGFeRDImporter(rewindedInputStream);
                final String zfXML = zi.getUTF8();
                log.debug("Extracted the following XML {} from the PDF file {}", zfXML, name);
                rewindedInputStream = new ByteArrayInputStream(zfXML.getBytes());
            }
            try ( final BufferedInputStream bis = new BufferedInputStream(rewindedInputStream);
                  final DigestInputStream dis = new DigestInputStream(bis, digest);
                  final ByteArrayOutputStream out = new ByteArrayOutputStream() ) {

                // read the file and update the hash calculation
                int n;
                while (EOF != (n = dis.read(buffer))) {
                    out.write(buffer, 0, n);
                }
                // get the hash value as byte array
                final byte[] hash = digest.digest();
                log.debug("Generated hashcode for {} is {}", name, DatatypeConverter.printHexBinary(hash));
                out.flush();
                return new Input(out.toByteArray(), name, hash, digest.getAlgorithm());
            } catch (final IOException e) {
                throw new IllegalArgumentException(MESSAGE_OPEN_STREAM_ERROR + name, e);
            }
        } else {
            throw new IllegalArgumentException("Must supply a valid name/identifier for the input");
        }
    }

    private MessageDigest createDigest() {
        try {
            final MessageDigest digest;
            digest = MessageDigest.getInstance(getAlgorithm());
            return digest;
        } catch (final NoSuchAlgorithmException e) {
            // should not happen
            throw new IllegalStateException(String.format("Specified method %s is not available", getAlgorithm()), e);
        }
    }

}
