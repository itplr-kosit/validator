package org.kosit.validator.impl.conformatron.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.scenario.CTScenarioMatch;
import org.jspecify.annotations.Nullable;
import org.kosit.cvr.model.SeverityOverrides;
import org.kosit.validator.scenario.v2.CustomErrorLevel;
import org.kosit.validator.scenario.v2.ErrorLevelType;
import org.kosit.validator.scenario.v2.ScenarioType;
import org.kosit.validator.scenario.v2.ValidateWithSchematron;

/**
 * Reads the {@link SeverityOverrides} out of a scenario configuration: the {@code customLevel} elements declared with
 * the rule sets ({@code validateWithSchematron/customLevel}, {@code scenarios-v1.xsd}).
 * <p>
 * {@link SeverityOverrides} itself is a plain detection-code → severity map and therefore lives in the
 * {@code validator-schematron} module; knowing that a scenario declares them is a concern of this module.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class ScenarioSeverityOverrides {

    private ScenarioSeverityOverrides() {
        // static utility
    }

    private static CTStandardSeverity toSeverity(final ErrorLevelType level) {
        return switch (level) {
            case ERROR -> CTStandardSeverity.ERROR;
            case WARNING -> CTStandardSeverity.WARNING;
            case INFORMATION -> CTStandardSeverity.NONE;
        };
    }

    /**
     * Collects all {@code customLevel} overrides declared by the given scenario configuration (across all rule sets,
     * token lists expanded).
     *
     * @param configuration the scenario configuration; {@code null} yields {@link SeverityOverrides#NONE}
     * @return the overrides, {@link SeverityOverrides#NONE} when the scenario declares none
     */
    public static SeverityOverrides fromConfiguration(final @Nullable ScenarioType configuration) {
        if (configuration == null) {
            return SeverityOverrides.NONE;
        }
        final Map<String, CTStandardSeverity> map = new LinkedHashMap<>();
        collect(configuration, map);
        return SeverityOverrides.of(map);
    }

    private static void collect(final ScenarioType configuration, final Map<String, CTStandardSeverity> map) {
        for (final ValidateWithSchematron schematron : configuration.getValidateWithSchematron()) {
            for (final CustomErrorLevel level : schematron.getCustomLevel()) {
                for (final String code : level.getValue()) {
                    map.put(code, toSeverity(level.getLevel()));
                }
            }
        }
    }

    /**
     * Convenience accessor for pipeline assembly: the overrides carried by the selected scenario match.
     *
     * @param match the selected scenario from step 4; foreign implementations yield {@link SeverityOverrides#NONE}
     * @return the scenario's overrides
     */
    public static SeverityOverrides of(final @Nullable CTScenarioMatch match) {
        return match instanceof final ScenarioMatch scenarioMatch ? scenarioMatch.getSeverityOverrides() : SeverityOverrides.NONE;
    }

    /**
     * The overrides of every scenario a run applies, in one map for step 7: a code declared by several scenarios takes
     * the level of the last one in application order. Scoping the overrides to the rule sets of their own scenario is a
     * follow-up of the conformance targets per rule set.
     *
     * @param applied the scenarios step 4 selected; foreign implementations contribute nothing
     * @return the combined overrides, {@link SeverityOverrides#NONE} when none of them declares any
     */
    public static SeverityOverrides ofAll(final List<? extends CTScenarioMatch> applied) {
        final Map<String, CTStandardSeverity> map = new LinkedHashMap<>();
        for (final CTScenarioMatch match : applied) {
            if (match instanceof final ScenarioMatch scenarioMatch && scenarioMatch.getConfiguration() != null) {
                collect(scenarioMatch.getConfiguration(), map);
            }
        }
        return map.isEmpty() ? SeverityOverrides.NONE : SeverityOverrides.of(map);
    }
}
