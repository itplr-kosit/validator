/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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
        return !getFailedAsserts().isEmpty();
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

    public List<String> getMessages() {
        return getFailedAsserts().stream().map(FailedAssert::getText).flatMap(e -> e.getContent().stream()).map(Object::toString)
                .collect(Collectors.toList());
    }
}
