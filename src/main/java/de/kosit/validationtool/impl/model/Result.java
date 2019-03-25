/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Ein Ergebnisobjekt, dass das eigentliche Ergebnis hält und optional auch verschiedene Fehlerobjekte.
 * 
 * @param <T> der Typ des Ergebnis-Objekts
 * @param <E> der Typ des Fehler-Objekts
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Result<T, E> {

    private T object;

    private Collection<E> errors = new ArrayList<>();

    /**
     * Erzeugt ein neues Ergebnis mit Fehler
     * 
     * @param errors die Fehler
     */
    public Result(Collection<E> errors) {
        this(null, errors);
    }

    /**
     * Erzeugt ein neues Ergebnis mit einem Ergebnisobjekt
     * 
     * @param o
     */
    public Result(T o) {
        this(o, Collections.emptyList());
    }

    /**
     * Zeigt an, ob das Ergebnis valide, also ohne Fehler ist.
     * 
     * @return true wenn erfolgreich
     */
    public boolean isValid() {
        return object != null && errors.isEmpty();
    }

    /**
     * Zeigt an, ob das Ergebnis nicht valide ist, als entsprechend Fehler gesammelt wurden.
     * 
     * @return true wenn erfolgreich wenn Fehler vorhanden sind.
     */
    public boolean isInvalid() {
        return !isValid();
    }
}