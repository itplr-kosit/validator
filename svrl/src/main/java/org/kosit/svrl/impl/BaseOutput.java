package org.kosit.svrl.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.FiredRule;

/**
 * Base class for implementing specific extensions to the generated class
 * {@link org.oclc.purl.dsdl.svrl.SchematronOutput}.
 *
 * @author Andreas Penski
 */
public abstract class BaseOutput {

    public abstract List<Serializable> getActivePatternOrActiveGroupAndFiredRule();

    private <T> @NonNull List<@NonNull T> filter(@NonNull final Class<@NonNull T> type) {
        return getActivePatternOrActiveGroupAndFiredRule().stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }

    /**
     * Returns the list of {@link FailedAssert}.
     *
     * @return list of {@link FailedAssert}
     */
    public @NonNull List<@NonNull FailedAssert> getFailedAsserts() {
        return filter(FailedAssert.class);
    }

    /**
     * Returns the list of {@link FailedAssert}.
     *
     * @return list of {@link FailedAssert}
     */
    public @NonNull List<@NonNull FiredRule> getFiredRules() {
        return filter(FiredRule.class);
    }

    /**
     * Determines whether there were any {@link FailedAssert}s during validation.
     *
     * @return true if at least one {@link FailedAssert} is present
     */
    public boolean hasFailedAsserts() {
        return !getFailedAsserts().isEmpty();
    }

    /**
     * Returns the list of {@link ActivePattern}.
     *
     * @return list of {@link ActivePattern}
     */
    public @NonNull List<@NonNull ActivePattern> getActivePatterns() {
        return filter(ActivePattern.class);
    }

    /**
     * Searches for a {@link FailedAssert} with a defined name.
     *
     * @param name the name
     * @return Optional containing the {@link FailedAssert}
     */
    public Optional<FailedAssert> findFailedAssert(final String name) {
        return getFailedAsserts().stream().filter(e -> e.getId().equals(name)).findAny();
    }

    public @NonNull List<String> getMessages() {
        return getFailedAsserts().stream().map(FailedAssert::getText).flatMap(e -> e.getContent().stream()).map(Object::toString)
                .collect(Collectors.toList());
    }
}
