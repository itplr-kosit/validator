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
package org.kosit.validator.impl.conformatron.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.xml.transform.stream.StreamSource;

import org.conformatron.api.model.source.CTInput;
import org.jspecify.annotations.NonNull;
import org.kosit.validator.api.VInput;

/**
 * Validator implementation of {@link CTInput}.
 *
 * @author Philip Helger
 */
public final class ValidationInput {

    /**
     * Wraps a legacy {@link VInput} as a complete XML source. The validator currently only feeds XML documents into the
     * pipeline, so the detected syntax is fixed until DETECT_SYNTAX is implemented as its own action.
     *
     * @param input the legacy input
     * @return a new source facade
     */
    public static CTInput of(final VInput input) {
        Objects.requireNonNull(input);
        return new CTInput() {

            public @NonNull String getName() {
                return input.getName();
            }

            public InputStream getInputStream() throws IOException {
                if (input.getSource() instanceof final StreamSource aSS)
                    return aSS.getInputStream();
                throw new IllegalStateException("Unsupported source: " + input.getSource().getClass().getName());
            }

            public boolean isReadMultiple() {
                return true;
            }
        };
    }

    public static @NonNull CTInput of(final byte @NonNull [] bytes, @NonNull final String name) {
        Objects.requireNonNull(bytes);
        Objects.requireNonNull(name);
        return new CTInput() {

            public @NonNull String getName() {
                return name;
            }

            public @NonNull InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(bytes);
            }

            public boolean isReadMultiple() {
                return true;
            }
        };
    }

    private ValidationInput() {
    }
}
