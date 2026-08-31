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
import java.util.Objects;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionLocation;
import org.conformatron.api.model.detection.CTDetectionText;
import org.conformatron.api.model.detection.CTSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.validator.scenario.v1.ScenarioType;

/**
 * A detection about a validation scenario (steps 3 and 4). Carries — beyond the plain {@link CTDetection} — the
 * information a report consumer needs to identify and inspect the scenario:
 * <ul>
 * <li>the <b>scenario id</b>, reported as {@code cvrl:scenario-id} on the detection. Consumers that use the validator
 * for more than plain validation need the id on every scenario statement.</li>
 * <li>the <b>pointer into the scenario configuration</b> ({@link #getConfigurationLocation()}), reported as the
 * detection's {@code location} so the scenario can be looked up quickly.</li>
 * <li>optionally the <b>scenario configuration</b> itself ({@link #getConfiguration()}) — set for the selected scenario
 * (step 4) so the report can embed the individual scenario in its original form; {@code null} for the candidate list of
 * step 3.</li>
 * </ul>
 * Everything else is delegated to the wrapped detection, so the detection stays a plain {@link CTDetection} for all
 * consumers that do not care about scenarios.
 *
 * @author Andreas Schmitz
 */
public final class ScenarioDetection implements CTDetection {

    private final CTDetection delegate;

    private final String scenarioId;

    private final String configurationLocation;

    private final ScenarioType configuration;

    /**
     * A detection about one of several candidate scenarios (step 3): id and configuration pointer, but no embedded
     * configuration.
     *
     * @param delegate the plain detection
     * @param scenarioId the scenario id
     * @param configurationLocation pointer into the scenario configuration
     * @return the scenario detection
     */
    public static ScenarioDetection candidate(final @NonNull CTDetection delegate, final @Nullable String scenarioId,
            final @Nullable String configurationLocation) {
        return new ScenarioDetection(delegate, scenarioId, configurationLocation, null);
    }

    /**
     * A detection about the selected scenario (step 4): additionally carries the configuration so the report can embed
     * the individual scenario.
     *
     * @param delegate the plain detection
     * @param scenarioId the scenario id
     * @param configurationLocation pointer into the scenario configuration
     * @param configuration the selected scenario's configuration
     * @return the scenario detection
     */
    public static ScenarioDetection selected(final @NonNull CTDetection delegate, final @Nullable String scenarioId,
            final @Nullable String configurationLocation, final @Nullable ScenarioType configuration) {
        return new ScenarioDetection(delegate, scenarioId, configurationLocation, configuration);
    }

    private ScenarioDetection(final @NonNull CTDetection delegate, final @Nullable String scenarioId,
            final @Nullable String configurationLocation, final @Nullable ScenarioType configuration) {
        Objects.requireNonNull(delegate);
        this.delegate = delegate;
        this.scenarioId = scenarioId;
        this.configurationLocation = configurationLocation;
        this.configuration = configuration;
    }

    /** The scenario this detection is about. */
    public @Nullable String getScenarioID() {
        return this.scenarioId;
    }

    /** Pointer into the scenario configuration, for looking the scenario up. */
    public @Nullable String getConfigurationLocation() {
        return this.configurationLocation;
    }

    /** The scenario configuration for embedding, {@code null} unless this is the selected scenario. */
    public @Nullable ScenarioType getConfiguration() {
        return this.configuration;
    }

    @Override
    public OffsetDateTime getDateTimeUTC() {
        return this.delegate.getDateTimeUTC();
    }

    @Override
    public CTSeverity getSeverity() {
        return this.delegate.getSeverity();
    }

    @Override
    public String getID() {
        return this.delegate.getID();
    }

    @Override
    public String getCode() {
        return this.delegate.getCode();
    }

    @Override
    public String getField() {
        return this.delegate.getField();
    }

    @Override
    public CTDetectionLocation getLocation() {
        return this.delegate.getLocation();
    }

    @Override
    public CTDetectionText getText() {
        return this.delegate.getText();
    }

    @Override
    public CTDetectionText getSummary() {
        return this.delegate.getSummary();
    }

    @Override
    public Exception getLinkedException() {
        return this.delegate.getLinkedException();
    }
}
