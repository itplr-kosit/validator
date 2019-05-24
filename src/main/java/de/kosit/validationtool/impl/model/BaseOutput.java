package de.kosit.validationtool.impl.model;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.FiredRule;

/**
 * Basis-Klasse um spezifische Erweiterungen an der generierten Klasse {@link org.oclc.purl.dsdl.svrl.SchematronOutput}
 * umzusetzen.
 * 
 * @author Andreas Penski
 */
public abstract class BaseOutput {

    public abstract List<Serializable> getActivePatternAndFiredRuleAndFailedAssert();

    /**
     * Gibt die Liste der {@link FailedAssert} zurück
     * 
     * @return Liste mit {@link FailedAssert}
     */
    public List<FailedAssert> getFailedAsserts() {
        return filter(FailedAssert.class);
    }

    /**
     * Gibt die Liste der {@link FailedAssert} zurück
     * 
     * @return Liste mit {@link FailedAssert}
     */
    public List<FiredRule> getFiredRules() {
        return filter(FiredRule.class);
    }

    /**
     * Ermittelt, ob es bei der Validierung {@link FailedAssert}s gab.
     * 
     * @return true wenn mindestens ein {@link FailedAssert} vorhanden ist
     */
    public boolean hasFailedAsserts() {
        return getFailedAsserts().size() > 0;
    }

    /**
     * Gibt die Liste der {@link ActivePattern} zurück
     *
     * @return Liste mit {@link ActivePattern}
     */
    public List<ActivePattern> getActivePatterns() {
        return filter(ActivePattern.class);
    }

    private <T> List<T> filter(final Class<T> type) {
        return getActivePatternAndFiredRuleAndFailedAssert().stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }

    /**
     * Sucht nach einem {@link FailedAssert} mit einem definierten Namen.
     * 
     * @param name der Name
     * @return Optional mit dem {@link FailedAssert}
     */
    public Optional<FailedAssert> findFailedAssert(final String name) {
        return getFailedAsserts().stream().filter(e -> e.getId().equals(name)).findAny();
    }

}
