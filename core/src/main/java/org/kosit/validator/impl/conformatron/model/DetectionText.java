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

import java.util.List;
import java.util.Locale;

import org.conformatron.api.model.detection.CTDetectionText;

/**
 * Locale-independent single-text implementation of {@link CTDetectionText}.
 *
 * @author Andreas Schmitz
 */
public final class DetectionText implements CTDetectionText {

    private final String text;

    public DetectionText(final String text) {
        if (text == null) {
            throw new IllegalArgumentException("text may not be null");
        }
        this.text = text;
    }

    @Override
    public List<Locale> getAllLocales() {
        return List.of(Locale.ROOT);
    }

    @Override
    public String getDisplayText(final Locale contentLocale) {
        return this.text;
    }
}
