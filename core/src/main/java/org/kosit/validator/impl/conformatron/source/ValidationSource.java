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

import java.util.Objects;

import org.conformatron.api.model.source.CTReadResource;
import org.conformatron.api.model.source.CTValidationSource;
import org.conformatron.api.model.validation.CTSyntax;
import org.jspecify.annotations.NonNull;

/**
 * Validator implementation of {@link CTValidationSource}. Facade over the legacy {@link VInput} abstraction: the
 * existing input handling keeps doing the heavy lifting while the pipeline is migrated to the conformatron-api step by
 * step.
 *
 * @author Andreas Schmitz
 * @author Philip Helger
 */
public final class ValidationSource implements CTValidationSource {

    private final CTReadResource readResource;

    private final CTSyntax detectedSyntax;

    private final boolean complete;

    public static final @NonNull ValidationSource completeXml(final @NonNull CTReadResource readResource) {
        return new ValidationSource(readResource, CTSyntax.XML, false);
    }

    public ValidationSource(final @NonNull CTReadResource readResource, final @NonNull CTSyntax detectedSyntax, final boolean complete) {
        Objects.requireNonNull(readResource);
        Objects.requireNonNull(detectedSyntax);
        this.readResource = readResource;
        this.detectedSyntax = detectedSyntax;
        this.complete = complete;
    }

    @Override
    public CTReadResource getReadResource() {
        return this.readResource;
    }

    @Override
    public CTSyntax getDetectedSyntax() {
        return this.detectedSyntax;
    }

    @Override
    public boolean isComplete() {
        return this.complete;
    }
}
