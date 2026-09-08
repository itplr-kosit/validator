package org.kosit.validator.impl.conformatron.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.scenario.CTScenarioMatch;
import org.jspecify.annotations.Nullable;
import org.kosit.cvr.model.SeverityOverrides;
import org.kosit.validator.scenario.v1.CreateReportType;
import org.kosit.validator.scenario.v1.CustomErrorLevel;
import org.kosit.validator.scenario.v1.ErrorLevelType;
import org.kosit.validator.scenario.v1.ScenarioType;
import org.kosit.validator.scenario.v1.ValidateWithSchematron;

/**
 * Reads the {@link SeverityOverrides} out of a scenario configuration (the {@code customLevel} elements of
 * {@code scenarios-v1.xsd}).
 * <p>
 * The overrides belong to the rule set and are declared with it since 2.0 ({@code validateWithSchematron/customLevel});
 * the place 1.x used, {@code createReport/customLevel}, is still read. A code named in both places takes the level of
 * its rule set.
 * </p>
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

    private static void collect(final List<CustomErrorLevel> levels, final Map<String, CTStandardSeverity> map) {
        for (final CustomErrorLevel level : levels) {
            for (final String code : level.getValue()) {
                map.put(code, toSeverity(level.getLevel()));
            }
        }
    }

    /**
     * Collects all {@code customLevel} overrides declared by the given scenario configuration (across all rule sets and
     * all legacy {@code createReport} elements, token lists expanded).
     *
     * @param configuration the scenario configuration; {@code null} yields {@link SeverityOverrides#NONE}
     * @return the overrides, {@link SeverityOverrides#NONE} when the scenario declares none
     */
    public static SeverityOverrides fromConfiguration(final @Nullable ScenarioType configuration) {
        if (configuration == null) {
            return SeverityOverrides.NONE;
        }
        final Map<String, CTStandardSeverity> map = new LinkedHashMap<>();
        // the place of 1.x first, so that the declaration at the rule set wins for a code named in both
        for (final CreateReportType report : configuration.getCreateReport()) {
            collect(report.getCustomLevel(), map);
        }
        for (final ValidateWithSchematron schematron : configuration.getValidateWithSchematron()) {
            collect(schematron.getCustomLevel(), map);
        }
        return SeverityOverrides.of(map);
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
}
