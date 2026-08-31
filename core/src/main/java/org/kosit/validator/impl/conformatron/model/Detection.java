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

    /** The severity declared by the rule when {@link #severity} is a scenario override. */
    private final CTSeverity originalSeverity;

    private final String code;

    private final CTDetectionLocation location;

    private final CTDetectionText text;

    private final Exception linkedException;

    public static Detection of(final @NonNull CTSeverity severity, final @Nullable String code, final @NonNull CTDetectionLocation location,
            final @Nullable String message) {
        return new Detection(severity, code, location, message, null);
    }

    /**
     * Derives a detection whose severity was overridden by the scenario ({@code customLevel}): same code, location,
     * text and linked exception, but the effective severity — the declared one stays retrievable via
     * {@link #getOriginalSeverity()} for auditability.
     *
     * @param base the detection as produced by the rules
     * @param effectiveSeverity the severity after applying the scenario override
     * @return the overridden detection
     */
    public static Detection overridden(final @NonNull CTDetection base, final @NonNull CTSeverity effectiveSeverity) {
        Objects.requireNonNull(base);
        return new Detection(effectiveSeverity, base.getSeverity(), base.getCode(), base.getLocation(), base.getText(),
                base.getLinkedException());
    }

    public Detection(final @NonNull CTSeverity severity, final @Nullable String code, final @NonNull CTDetectionLocation location,
            final @Nullable String message, final @Nullable Exception linkedException) {
        this(severity, null, code, location, message != null ? new DetectionText(message) : null, linkedException);
    }

    private Detection(final @NonNull CTSeverity severity, final @Nullable CTSeverity originalSeverity, final @Nullable String code,
            final @NonNull CTDetectionLocation location, final @Nullable CTDetectionText text, final @Nullable Exception linkedException) {
        Objects.requireNonNull(severity);
        Objects.requireNonNull(location);
        this.dateTimeUTC = OffsetDateTime.now(ZoneOffset.UTC);
        this.severity = severity;
        this.originalSeverity = originalSeverity;
        this.code = code;
        this.location = location;
        this.text = text;
        this.linkedException = linkedException;
    }

    /**
     * The severity declared by the rule, when the effective {@link #getSeverity()} is a scenario {@code customLevel}
     * override; {@code null} when no override was applied.
     */
    public @Nullable CTSeverity getOriginalSeverity() {
        return this.originalSeverity;
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
