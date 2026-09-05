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
package org.kosit.cvr.model;

import java.util.Map;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.Nullable;

/**
 * The scenario's severity overrides (the {@code customLevel} elements of the scenario configuration), as an immutable
 * detection-code → severity map. Successor of the 1.x report-XSL mechanism ({@code rep:custom-level()} in
 * {@code default-report.xsl}), applied in <b>step 7</b> instead of at report-rendering time.
 * <p>
 * Semantics carried over from 1.x:
 * </p>
 * <ul>
 * <li>One {@code customLevel} element carries a <b>token list</b> of detection codes — every token maps to the same
 * level.</li>
 * <li>Overrides go in <b>both directions</b>: XRechnung downgrades CEN {@code fatal} rules to warning/information
 * <i>and</i> upgrades CEN {@code warning} rules (e.g. {@code UBL-CR-646}, {@code CII-SR-*}) to error.</li>
 * <li>Level mapping into the severity model: {@code error} → {@link CTStandardSeverity#ERROR}, {@code warning} →
 * {@link CTStandardSeverity#WARNING}, {@code information} → {@link CTStandardSeverity#NONE}.</li>
 * <li>Engine/processing detections are never overridable (1.x: {@code PROCESSING_ERROR} exemption). This is enforced
 * structurally: {@code ApplyRulesAction} applies overrides only to the findings produced by the rules themselves, never
 * to its own engine-error/skip markers.</li>
 * </ul>
 * <p>
 * This class is pure data — reading the overrides out of a scenario configuration lives in
 * {@code ScenarioSeverityOverrides}, the application (and the structural protection above) in {@code ApplyRulesAction}.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class SeverityOverrides {

    /** No overrides — every detection keeps its declared severity. */
    public static final SeverityOverrides NONE = new SeverityOverrides(Map.of());

    private final Map<String, CTStandardSeverity> byCode;

    private SeverityOverrides(final Map<String, CTStandardSeverity> byCode) {
        this.byCode = Map.copyOf(byCode);
    }

    /**
     * Creates the overrides from an already collected detection-code → severity map.
     *
     * @param byCode the overrides; {@code null} or empty yields {@link #NONE}
     * @return the overrides
     */
    public static SeverityOverrides of(final @Nullable Map<String, CTStandardSeverity> byCode) {
        return byCode == null || byCode.isEmpty() ? NONE : new SeverityOverrides(byCode);
    }

    /**
     * The effective severity for the given detection code, or {@code null} when the scenario declares no override for
     * it (the declared severity stands).
     *
     * @param code the detection code (SVRL assert id / rule code)
     * @return the override or {@code null}
     */
    public @Nullable CTStandardSeverity effectiveFor(final @Nullable String code) {
        return code == null ? null : this.byCode.get(code);
    }

    public boolean isEmpty() {
        return this.byCode.isEmpty();
    }
}
