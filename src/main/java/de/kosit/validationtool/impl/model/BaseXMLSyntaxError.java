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

import org.slf4j.Logger;

import de.kosit.validationtool.model.reportInput.XMLSyntaxErrorSeverity;

/**
 * Basis-Klasse für Syntax-Error. Wird über die JAXB-generierte Klasse
 * {@link de.kosit.validationtool.model.reportInput.XMLSyntaxError} erweitert.
 * 
 * @author Andreas Penski
 */
public abstract class BaseXMLSyntaxError {

    /**
     * Logged den Syntax-Fehler über einen definierten Logger.
     * 
     * @param logger der Logger
     */
    public void log(Logger logger) {
        String msgTemplate = "{} At row {} at pos {}";
        Object[] params = { getMessage(), getRowNumber(), getColumnNumber() };
        if (getSeverity() == XMLSyntaxErrorSeverity.SEVERITY_WARNING) {
            logger.warn(msgTemplate, params);
        } else {
            logger.error(msgTemplate, params);
        }

    }

    @Override
    public String toString() {
        return String.format("%s At row %s at pos %s", getMessage(), getRowNumber(), getColumnNumber());
    }

    /**
     * Getter aus dem schema
     * 
     * @return Spalte des Fehlers
     */
    public abstract Integer getColumnNumber();

    /**
     * Getter aus dem schema
     *
     * @return Zeile des Fehlers
     */
    public abstract Integer getRowNumber();

    /**
     * Getter aus dem schema
     *
     * @return Fehlermeldung
     */
    public abstract String getMessage();

    /**
     * Getter aus dem schema
     *
     * @return severity
     */
    public abstract XMLSyntaxErrorSeverity getSeverity();
}
