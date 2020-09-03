package de.kosit.validationtool.config;

/**
 * Defines some keys used for supplying additional parameters internally.
 * 
 * @author Andreas Penski
 */
public final class Keys {

    /**
     * The actual scenarios file location as used with {@link ConfigurationLoader}.
     */
    public static final String SCENARIOS_FILE = "scenarios_file";

    /**
     * The actual scenarios configuration represented as serializable tree. This either loaded from file or build
     * manually via {@link ConfigurationBuilder}
     */
    public static final String SCENARIO_DEFINITION = "scenario_definition";

    private Keys() {
        // hide
    }
}
