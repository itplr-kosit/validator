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

import org.conformatron.api.model.source.ICTValidationSource;
import org.conformatron.api.model.validation.ECTValidationBaseType;
import org.kosit.validator.api.Input;

/**
 * Validator implementation of {@link ICTValidationSource}. Facade over the legacy {@link Input} abstraction: the
 * existing input handling keeps doing the heavy lifting while the pipeline is migrated to the conformatron-api step by
 * step.
 *
 * @author Andreas Schmitz
 */
public final class ValidationSource implements ICTValidationSource {

    private final String name;

    private final ECTValidationBaseType detectedSyntax;

    private final boolean complete;

    public ValidationSource(final String name, final ECTValidationBaseType detectedSyntax, final boolean complete) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name may not be null or empty");
        }
        this.name = name;
        this.detectedSyntax = detectedSyntax;
        this.complete = complete;
    }

    /**
     * Wraps a legacy {@link Input} as a complete XML source. The validator currently only feeds XML documents into the
     * pipeline, so the detected syntax is fixed until DETECT_SYNTAX is implemented as its own action.
     *
     * @param input the legacy input
     * @return a new source facade
     */
    public static ValidationSource of(final Input input) {
        if (input == null) {
            throw new IllegalArgumentException("input may not be null");
        }
        return new ValidationSource(input.getName(), ECTValidationBaseType.XML, true);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ECTValidationBaseType getDetectedSyntax() {
        return this.detectedSyntax;
    }

    @Override
    public boolean isComplete() {
        return this.complete;
    }
}
