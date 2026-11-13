package org.kosit.validator.scenario.convert;

/**
 * How a migration of a scenario configuration ended.
 *
 * @author Andreas Schmitz
 * @see ScenarioMigration
 */
public enum EMigrationOutcome {
    /** The configuration was converted, see {@link MigrationResult#converted()}. */
    CONVERTED,
    /** The configuration is written in version 3 already, there is nothing to convert. */
    ALREADY_VERSION_3,
    /** The file is no scenario configuration of version 2: its root element is in another namespace, or none. */
    NO_SCENARIO_CONFIGURATION,
    /**
     * The configuration leaves coordinates off that version 3 requires. They are proposed, see
     * {@link MigrationResult#proposals()}, but nothing was converted, because nobody accepted them.
     */
    COORDINATES_PROPOSED,
    /** Version 3 can not express the configuration as it stands, see {@link MigrationResult#findings()}. */
    NOT_EXPRESSIBLE
}
