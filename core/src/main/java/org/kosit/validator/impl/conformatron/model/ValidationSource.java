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

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.conformatron.api.model.source.CTValidationSource;
import org.conformatron.api.model.validation.CTValidationSyntax;
import org.jspecify.annotations.NonNull;
import org.kosit.validator.api.VInput;

/**
 * Validator implementation of {@link CTValidationSource}. Facade over the legacy {@link VInput} abstraction: the
 * existing input handling keeps doing the heavy lifting while the pipeline is migrated to the conformatron-api step by
 * step.
 *
 * @author Andreas Schmitz
 */
public final class ValidationSource implements CTValidationSource {

    private final String name;

    private final CTValidationSyntax detectedSyntax;

    private final boolean complete;

    /**
     * Wraps a legacy {@link VInput} as a complete XML source. The validator currently only feeds XML documents into the
     * pipeline, so the detected syntax is fixed until DETECT_SYNTAX is implemented as its own action.
     *
     * @param input the legacy input
     * @return a new source facade
     */
    public static ValidationSource of(final VInput input) {
        Objects.requireNonNull(input);
        // TODO PH create overload for getInputStream
        return new ValidationSource(input.getName(), CTValidationSyntax.XML, true);
    }

    public ValidationSource(final String name, final CTValidationSyntax detectedSyntax, final boolean complete) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name may not be null or empty");
        }
        this.name = name;
        this.detectedSyntax = detectedSyntax;
        this.complete = complete;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public CTValidationSyntax getDetectedSyntax() {
        return this.detectedSyntax;
    }

    @Override
    public boolean isComplete() {
        return this.complete;
    }

    public boolean canReadMultiple() {
        // TODO PH
        return true;
    }

    @NonNull
    public InputStream getInputStream() throws IOException {
        // TODO PH
        throw new IOException("TODO");
    }
}
