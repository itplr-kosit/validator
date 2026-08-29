package org.kosit.validator.scenario.generic;

import java.util.Objects;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.kosit.base.string.StringHelper;

/**
 * A single requirement of a {@link EScenarioKind#PDF} scenario, for example {@code pdf-a3}. Requirements were
 * introduced with scenario configuration version 3 and have no representation in version 2. This class is immutable.
 *
 * @author Philip Helger
 */
public final class ScenarioRequirement {

    private final String id;

    private ScenarioRequirement(@NonNull @Nonempty final String id) {
        if (StringHelper.isEmpty(id)) {
            throw new IllegalArgumentException("ID must not be empty");
        }
        this.id = id;
    }

    /**
     * @return the ID of the requirement. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getID() {
        return this.id;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ScenarioRequirement rhs = (ScenarioRequirement) o;
        return this.id.equals(rhs.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "ScenarioRequirement[id=" + this.id + "]";
    }

    /**
     * Factory method.
     *
     * @param id the ID of the requirement. May neither be <code>null</code> nor empty.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioRequirement of(@NonNull @Nonempty final String id) {
        return new ScenarioRequirement(id);
    }
}
