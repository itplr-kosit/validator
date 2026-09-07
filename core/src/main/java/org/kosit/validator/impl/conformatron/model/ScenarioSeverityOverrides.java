package org.kosit.validator.impl.conformatron.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.scenario.CTScenarioMatch;
import org.jspecify.annotations.Nullable;
import org.kosit.cvr.model.SeverityOverrides;
import org.kosit.validator.scenario.v1.CreateReportType;
import org.kosit.validator.scenario.v1.CustomErrorLevel;
import org.kosit.validator.scenario.v1.ErrorLevelType;
import org.kosit.validator.scenario.v1.ScenarioType;

/**
 * Reads the {@link SeverityOverrides} out of a scenario configuration (the {@code customLevel} elements of
 * {@code scenarios-v1.xsd}).
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
     * Collects all {@code customLevel} overrides declared by the given scenario configuration (across all
     * {@code createReport} elements, token lists expanded).
     *
     * @param configuration the scenario configuration; {@code null} yields {@link SeverityOverrides#NONE}
     * @return the overrides, {@link SeverityOverrides#NONE} when the scenario declares none
     */
    public static SeverityOverrides fromConfiguration(final @Nullable ScenarioType configuration) {
        if (configuration == null) {
            return SeverityOverrides.NONE;
        }
        final Map<String, CTStandardSeverity> map = new LinkedHashMap<>();
        for (final CreateReportType report : configuration.getCreateReport()) {
            for (final CustomErrorLevel level : report.getCustomLevel()) {
                for (final String code : level.getValue()) {
                    map.put(code, toSeverity(level.getLevel()));
                }
            }
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
