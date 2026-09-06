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
package org.kosit.conformatron.detection;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionLocation;
import org.conformatron.api.model.detection.CTDetectionText;
import org.conformatron.api.model.detection.CTSeverity;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Immutable implementation of {@link CTDetection}. Instances are created through the {@link Builder}, as all fields
 * except severity and location are optional.
 *
 * @author Andreas Schmitz
 */
public final class Detection implements CTDetection {

    private final OffsetDateTime dateTimeUTC;

    private final CTSeverity severity;

    /** The severity declared by the rule when {@link #severity} is a scenario override. */
    private final CTSeverity originalSeverity;

    private final String id;

    private final String code;

    private final String field;

    private final CTDetectionLocation location;

    private final CTDetectionText text;

    private final CTDetectionText summary;

    private final Exception linkedException;

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
        return builder().dateTimeUTC(base.getDateTimeUtc()).severity(effectiveSeverity).originalSeverity(base.getSeverity())
                .id(base.getId()).code(base.getCode()).field(base.getField()).location(base.getLocation()).text(base.getText())
                .summary(base.getSummary()).linkedException(base.getLinkedException()).build();
    }

    private Detection(final Builder builder) {
        Objects.requireNonNull(builder.severity, "severity must not be null");
        Objects.requireNonNull(builder.location, "location must not be null");
        this.dateTimeUTC = builder.dateTimeUTC != null ? builder.dateTimeUTC : OffsetDateTime.now(ZoneOffset.UTC);
        this.severity = builder.severity;
        this.originalSeverity = builder.originalSeverity;
        this.id = builder.id;
        this.code = builder.code;
        this.field = builder.field;
        this.location = builder.location;
        this.text = builder.text;
        this.summary = builder.summary;
        this.linkedException = builder.linkedException;
    }

    /**
     * The severity declared by the rule, when the effective {@link #getSeverity()} is a scenario {@code customLevel}
     * override; {@code null} when no override was applied.
     */
    public @Nullable CTSeverity getOriginalSeverity() {
        return this.originalSeverity;
    }

    @Override
    public OffsetDateTime getDateTimeUtc() {
        return this.dateTimeUTC;
    }

    @Override
    public CTSeverity getSeverity() {
        return this.severity;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getField() {
        return this.field;
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
        return this.summary;
    }

    @Override
    public Exception getLinkedException() {
        return this.linkedException;
    }

    /**
     * @return a new empty builder. Never <code>null</code>.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static Builder builderError() {
        return builder().severity(CTStandardSeverity.ERROR);
    }

    public static Builder builderWarning() {
        return builder().severity(CTStandardSeverity.WARNING);
    }

    public static Builder builderNone() {
        return builder().severity(CTStandardSeverity.NONE);
    }

    /**
     * @return a new builder prefilled with the state of this object. Never <code>null</code>.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for {@link Detection}.
     */
    public static final class Builder {

        private @Nullable OffsetDateTime dateTimeUTC;

        private @Nullable CTSeverity severity;

        private @Nullable CTSeverity originalSeverity;

        private @Nullable String id;

        private @Nullable String code;

        private @Nullable String field;

        private @Nullable CTDetectionLocation location;

        private @Nullable CTDetectionText text;

        private @Nullable CTDetectionText summary;

        private @Nullable Exception linkedException;

        private Builder() {
        }

        private Builder(final Detection src) {
            this.dateTimeUTC = src.dateTimeUTC;
            this.severity = src.severity;
            this.originalSeverity = src.originalSeverity;
            this.id = src.id;
            this.code = src.code;
            this.field = src.field;
            this.location = src.location;
            this.text = src.text;
            this.summary = src.summary;
            this.linkedException = src.linkedException;
        }

        /**
         * @param dateTimeUTC the creation date and time in UTC. May be <code>null</code> in which case the time of
         *            {@link #build()} is used.
         * @return this for chaining
         */
        public Builder dateTimeUTC(final @Nullable OffsetDateTime dateTimeUTC) {
            this.dateTimeUTC = dateTimeUTC;
            return this;
        }

        public Builder severity(final @Nullable CTSeverity severity) {
            this.severity = severity;
            return this;
        }

        /**
         * @return the severity set so far. May be <code>null</code>.
         */
        public @Nullable CTSeverity getSeverity() {
            return this.severity;
        }

        /**
         * @param originalSeverity the severity declared by the rule, when {@link #severity(CTSeverity)} is a scenario
         *            override. May be <code>null</code>.
         * @return this for chaining
         */
        public Builder originalSeverity(final @Nullable CTSeverity originalSeverity) {
            this.originalSeverity = originalSeverity;
            return this;
        }

        public Builder id(final @Nullable String id) {
            this.id = id;
            return this;
        }

        public Builder code(final @Nullable String code) {
            this.code = code;
            return this;
        }

        public Builder field(final @Nullable String field) {
            this.field = field;
            return this;
        }

        public Builder location(final DetectionLocation.@Nullable Builder builder) {
            return location(builder == null ? null : builder.build());
        }

        public Builder location(final @Nullable CTDetectionLocation location) {
            this.location = location;
            return this;
        }

        public Builder text(final @Nullable CTDetectionText text) {
            this.text = text;
            return this;
        }

        /**
         * @param message the locale independent detection text. May be <code>null</code>.
         * @return this for chaining
         */
        public Builder text(final @Nullable String message) {
            return text(message == null ? null : new DetectionText(message));
        }

        public Builder summary(final @Nullable CTDetectionText summary) {
            this.summary = summary;
            return this;
        }

        /**
         * @param summary the locale independent summary text. May be <code>null</code>.
         * @return this for chaining
         */
        public Builder summary(final @Nullable String summary) {
            return summary(summary == null ? null : new DetectionText(summary));
        }

        public Builder linkedException(final @Nullable Exception linkedException) {
            this.linkedException = linkedException;
            return this;
        }

        /**
         * @return the immutable object created from the current builder state. Never <code>null</code>.
         */
        public Detection build() {
            return new Detection(this);
        }
    }
}
