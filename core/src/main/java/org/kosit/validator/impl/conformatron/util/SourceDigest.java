/*
 * Copyright 2017-2026  Koordinierungsstelle für IT-Standards (KoSIT)
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
package org.kosit.validator.impl.conformatron.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Central helper for document hash computation (conformatron-api ADR-003): hash computation is its own concern — the
 * digest algorithm is never carried by or configured on the source objects, its name is never part of a method name,
 * and it is exchangeable in exactly one place ({@link #getAlgorithmName()}, currently SHA-512; e.g. for future
 * post-quantum requirements).
 *
 * @author Andreas Schmitz
 */
public final class SourceDigest {

    /** The current default algorithm. Exchange here — never in method names or call sites. */
    public static final String ALGORITHM = "SHA-512";

    private SourceDigest() {
        // static utility
    }

    /**
     * @return the name of the algorithm used by {@link #hashBytes(byte[])}, as required by
     *         {@code ICTParsedValidationSource#getHashAlgorithmName()}
     */
    public static String getAlgorithmName() {
        return ALGORITHM;
    }

    /**
     * Computes the hash of the given data with the central algorithm, as required by
     * {@code ICTParsedValidationSource#getHashBytes()}.
     *
     * @param data the data to hash
     * @return the hash bytes
     */
    public static byte[] hashBytes(final byte[] data) {
        try {
            return MessageDigest.getInstance(ALGORITHM).digest(data);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Central digest algorithm " + ALGORITHM + " is not available", e);
        }
    }

    /**
     * Convenience: the hash of the given data with the central algorithm, hex-encoded (lower case) — e.g. for report
     * texts and log output.
     *
     * @param data the data to hash
     * @return the hash, hex-encoded (lower case)
     */
    public static String hashHex(final byte[] data) {
        return HexFormat.of().formatHex(hashBytes(data));
    }
}
