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

package de.kosit.validationtool.api;

import java.util.List;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.w3c.dom.Document;

import net.sf.saxon.s9api.XdmNode;

/**
 * API result object holding various information of the validation process results.
 *
 * @author Andreas Penski
 */
public interface Result {

    /**
     * Zeigt an, ob die Verarbeitung durch den Validator erfolgreich durchlaufen wurde. Diese Funktion macht
     * ausdrücklich keine Aussage über die zur Akzeptanz.
     *
     * @return true, wenn die Verarbeitung komplett und erfolgreich durchlaufen wurde
     * @see #getAcceptRecommendation()
     */
    boolean isProcessingSuccessful();

    /**
     * Gibt eine Liste mit Verarbeitungsfehlermeldungen zurück.
     *
     * @return Liste mit Fehlermeldungen
     */
    List<String> getProcessingErrors();

    /**
     * Der generierte Report.
     */
    XdmNode getReport();

    /**
     * The Recommendation based on the evaluation of this Result.
     *
     * @return AcceptRecommendation
     */
    AcceptRecommendation getAcceptRecommendation();

    /**
     * Gibt den Report als W3C-{@link Document} zurück.
     *
     * @return der Report
     */
    Document getReportDocument();

    /**
     * Schnellzugriff auf die Empfehlung zur Weiterverarbeitung des Dokuments.
     *
     * @return true wenn {@link AcceptRecommendation#ACCEPTABLE}
     */
    boolean isAcceptable();

    /**
     * Gibt eine Liste mit gefundenen Schema-Validation-Fehler zurück. Diese Liste ist leer, wenn keine Fehler gefunden
     * wurden.
     */
    List<XmlError> getSchemaViolations();

    /**
     * Liefert die Ergebnisse der Schematron-Prüfungen, in der Reihenfolge der Szenario-Konfiguration.
     *
     * @return Liste mit Schematron-Ergebnissen
     */
    List<SchematronOutput> getSchematronResult();

    /**
     * Returns {@link org.oclc.purl.dsdl.svrl.FailedAssert FailedAsserts} of a schematron evaluation.
     *
     * @return list of {@link org.oclc.purl.dsdl.svrl.FailedAssert FailedAsserts}, if any, empty list otherwise
     */
    List<FailedAssert> getFailedAsserts();

    /**
     * Liefert ein true, wenn keine Schema-Violations vorhanden sind.
     *
     * @return true wenn Schema-valide
     */
    boolean isSchemaValid();

    /**
     * Liefert ein true, wenn der Prüfling eine well-formed XML-Datei ist.
     *
     * @return true wenn well-formed
     */
    boolean isWellformed();

    /**
     * Returns true, if schematron has been checked and the result does not contain any {@link FailedAssert
     * FailedAsserts}.
     *
     * @return true, if valid
     */
    boolean isSchematronValid();
}
