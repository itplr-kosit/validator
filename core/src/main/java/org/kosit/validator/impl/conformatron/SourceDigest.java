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
package org.kosit.validator.impl.conformatron;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Computes document digests. Hash computation is its own concern (conformatron-api ADR-003): the digest algorithm is
 * never carried by or configured on the source objects.
 *
 * @author Andreas Schmitz
 */
public final class SourceDigest {

    private static final String SHA_512 = "SHA-512";

    private SourceDigest() {
        // static utility
    }

    /**
     * Computes the SHA-512 hash of the given data as required by {@code ICTParsedValidationSource#getSha512Hash()}.
     *
     * @param data the data to hash
     * @return the hash, hex-encoded (lower case)
     */
    public static String sha512Hex(final byte[] data) {
        return hex(SHA_512, data);
    }

    /**
     * Computes a hash of the given data with the given algorithm.
     *
     * @param algorithm a {@link MessageDigest} algorithm name
     * @param data the data to hash
     * @return the hash, hex-encoded (lower case)
     */
    public static String hex(final String algorithm, final byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance(algorithm).digest(data));
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unknown digest algorithm " + algorithm, e);
        }
    }
}
