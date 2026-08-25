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
package org.kosit.validator.impl.conformatron.source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.xml.transform.stream.StreamSource;

import org.conformatron.api.annotation.CheckForSigned;
import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.annotation.Nonnegative;
import org.conformatron.api.model.source.CTResource;
import org.jspecify.annotations.NonNull;
import org.kosit.validator.api.VInput;

/**
 * Validator implementation of Conformatron resources.
 *
 * @author Philip Helger
 */
public final class Resource {

    /**
     * Implementation of {@link CTResource} based on a byte array.
     * 
     * @author Philip Helger
     *
     */
    private static final class ResourceByteArray implements CTResource {

        private final @NonNull String name;

        private final byte @NonNull [] bytes;

        private ResourceByteArray(@NonNull @Nonempty final String name, final byte @NonNull [] bytes) {
            Objects.requireNonNull(name);
            Objects.requireNonNull(bytes);
            this.name = name;
            this.bytes = bytes;
        }

        public @NonNull @Nonempty String getName() {
            return name;
        }

        public @Nonnegative long getLength() {
            return bytes.length;
        }

        public @NonNull InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(bytes);
        }
    }

    /**
     * Wraps a legacy {@link VInput} as a complete XML source. The validator currently only feeds XML documents into the
     * pipeline, so the detected syntax is fixed until DETECT_SYNTAX is implemented as its own action.
     *
     * @param input the legacy input
     * @return a new source facade
     */
    public static @NonNull CTResource of(final @NonNull VInput input) {
        Objects.requireNonNull(input);
        return new CTResource() {

            public @NonNull @Nonempty String getName() {
                return input.getName();
            }

            public @CheckForSigned long getLength() {
                return -1;
            }

            public InputStream getInputStream() throws IOException {
                if (input.getSource() instanceof final StreamSource src) {
                    final InputStream ret = src.getInputStream();
                    if (ret == null)
                        throw new IOException("Failed to open InputStream from StreamSource");
                    return ret;
                }
                throw new IllegalStateException("Unsupported source: " + input.getSource().getClass().getName());
            }
        };
    }

    public static @NonNull ResourceByteArray of(final @NonNull @Nonempty String name, final byte @NonNull [] bytes) {
        return new ResourceByteArray(name, bytes);
    }

    public static @NonNull ResourceByteArray utf8(final @NonNull @Nonempty String name, final @NonNull String str) {
        return of(name, str.getBytes(StandardCharsets.UTF_8));
    }

    private Resource() {
    }
}
