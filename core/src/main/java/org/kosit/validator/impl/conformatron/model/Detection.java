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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionLocation;
import org.conformatron.api.model.detection.CTDetectionText;
import org.conformatron.api.model.detection.CTSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Immutable implementation of {@link CTDetection}.
 *
 * @author Andreas Schmitz
 */
public final class Detection implements CTDetection {

    private final OffsetDateTime dateTimeUTC;

    private final CTSeverity severity;

    private final String code;

    private final CTDetectionLocation location;

    private final CTDetectionText text;

    private final Exception linkedException;

    public static Detection of(final @NonNull CTSeverity severity, final @Nullable String code, final @NonNull CTDetectionLocation location,
            final @Nullable String message) {
        return new Detection(severity, code, location, message, null);
    }

    public Detection(final @NonNull CTSeverity severity, final @Nullable String code, final @NonNull CTDetectionLocation location,
            final @Nullable String message, final @Nullable Exception linkedException) {
        Objects.requireNonNull(severity);
        Objects.requireNonNull(location);
        this.dateTimeUTC = OffsetDateTime.now(ZoneOffset.UTC);
        this.severity = severity;
        this.code = code;
        this.location = location;
        this.text = message != null ? new DetectionText(message) : null;
        this.linkedException = linkedException;
    }

    @Override
    public OffsetDateTime getDateTimeUTC() {
        return this.dateTimeUTC;
    }

    @Override
    public CTSeverity getSeverity() {
        return this.severity;
    }

    @Override
    public String getID() {
        return null;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getField() {
        return null;
    }

    @Override
    public CTDetectionLocation getLocation() {
        return this.location;
    }

    @Override
    public CTDetectionText getText() {
        return this.text;
    }

    @Override
    public CTDetectionText getSummary() {
        return null;
    }

    @Override
    public Exception getLinkedException() {
        return this.linkedException;
    }
}
