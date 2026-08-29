package org.kosit.validator.scenario.generic;

import java.util.ArrayList;
import java.util.List;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.kosit.base.ObjectHelper;
import org.kosit.base.string.StringHelper;

/**
 * A custom error level of a {@link ScenarioCreateReport}: it overrides the error level of all listed rule IDs in the
 * generated report.
 *
 * @author Philip Helger
 */
public class ScenarioCustomErrorLevel {

    private EScenarioErrorLevel level;

    private final List<String> ruleIDs = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param level the error level to apply. May not be <code>null</code>.
     */
    public ScenarioCustomErrorLevel(@NonNull final EScenarioErrorLevel level) {
        setLevel(level);
    }

    /**
     * @return the error level to apply. Never <code>null</code>.
     */
    public @NonNull EScenarioErrorLevel getLevel() {
        return this.level;
    }

    /**
     * Set the error level to apply.
     *
     * @param level the level to use. May not be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioCustomErrorLevel setLevel(@NonNull final EScenarioErrorLevel level) {
        this.level = ObjectHelper.requireNonNull(level, "Level");
        return this;
    }

    /**
     * @return the modifiable list of all rule IDs this level applies to. Never <code>null</code>.
     */
    public @NonNull List<String> getRuleIDs() {
        return this.ruleIDs;
    }

    /**
     * Add a single rule ID this level applies to.
     *
     * @param ruleID the rule ID to add. May neither be <code>null</code> nor empty.
     * @return this for chaining
     */
    public @NonNull ScenarioCustomErrorLevel addRuleID(@NonNull @Nonempty final String ruleID) {
        if (StringHelper.isEmpty(ruleID)) {
            throw new IllegalArgumentException("Rule ID must not be empty");
        }
        this.ruleIDs.add(ruleID);
        return this;
    }

    @Override
    public String toString() {
        return "ScenarioCustomErrorLevel[level=" + this.level + "; ruleIDs=" + this.ruleIDs + "]";
    }
}
