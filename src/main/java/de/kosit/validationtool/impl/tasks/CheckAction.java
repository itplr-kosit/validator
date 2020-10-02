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

package de.kosit.validationtool.impl.tasks;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import de.kosit.validationtool.api.AcceptRecommendation;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.reportInput.ProcessingError;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;

import net.sf.saxon.s9api.XdmNode;

/**
 * Interface, welches von allen Prüfschritten implementiert wird. Der Parameter vom Typ {@link Bag} dient dabei sowohl
 * als Quellce für Eingabe Parameter als auch für die Aufnahme von Ergebnisse, die an weitere Schritte weitergeleitet
 * werden sollen.
 * 
 * @author Andreas Penski
 */
@FunctionalInterface
public interface CheckAction {

    /**
     * Transport-Klasse für Eingabe und Ausgabe-Objekte für die einzelnen Prüfschritte.
     */
    @Getter
    @Setter
    class Bag {

        private Result<Scenario, String> scenarioSelectionResult;

        @Setter(AccessLevel.NONE)
        private CreateReportInput reportInput;

        /** Das finale Ergebnis */
        private XdmNode report;

        private boolean finished;

        private boolean stopped;

        private AcceptRecommendation acceptStatus = AcceptRecommendation.UNDEFINED;

        /** Das zu prüfende Dokument */
        private Input input;

        private Result<XdmNode, XMLSyntaxError> parserResult;

        private Result<Integer, String> assertionResult;

        private Result<Boolean, XMLSyntaxError> schemaValidationResult;

        public Bag(final Input input) {
            this(input, new CreateReportInput());
        }

        public Bag(final Input input, final CreateReportInput reportInput) {
            this.input = input;
            this.reportInput = reportInput;
        }

        /**
         * Signalisiert einen vorzeitigen Stop der Vearbeitung.
         */
        public void stopProcessing(final String error) {
            stopProcessing(Collections.singleton(error));
        }

        public void stopProcessing(final Collection<String> errors) {
            this.stopped = true;
            if (this.reportInput.getProcessingError() == null) {
                this.reportInput.setProcessingError(new ProcessingError());
            }
            this.reportInput.getProcessingError().getError().addAll(errors);
        }

        public void addProcessingError(final String msg) {
            stopProcessing(msg);
        }

        /**
         * Gibt den Namen des Prüflings zurück, dabei werden etwaige Pfadinformationen abgeschnitten.
         *
         * @return der Name des Prüflings
         */
        public String getName() {
            final String fileName = getInput().getName().replaceAll(".*/|.*\\\\", "");
            return FilenameUtils.getBaseName(fileName);
        }
    }

    /**
     * Ausfürhung des Prüfschrittes und Erweiterung der gesammelten Informationen.
     *
     * @param results die Informationssammlung
     */
    void check(Bag results);

    /**
     * Ermittlung, ob ein Schritt u.U. ausgelassen werden kann. Die Funktion wird vor der eigentlichen Prüfaktion
     * aufgerufen und kann somit eine Ausführung des Prüfschrittes verhindern. Entwickler können diese Funktion
     * überschreiben, um den Prüfschritt bedingt auszuführen.
     *
     * @param results die bisher gesammelten Information
     * @return <code>true</code> wenn der Schritt ausgelassen werden soll
     */
    default boolean isSkipped(final Bag results) {
        return false;
    }

}
