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

package de.kosit.validationtool.impl.tasks;

import javax.xml.validation.Schema;

import org.apache.commons.lang3.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.CollectingErrorEventHandler;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;

/**
 * Validiert die gesammelten Informationen über den Prüfling. Zusätzlich Check.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class ValidateReportInputAction implements CheckAction {

    private final ConversionService conversionService;

    private final Schema schema;

    @Override
    public void check(final Bag bag) {
        final Result<Boolean, XMLSyntaxError> results = validate(bag.getReportInput());
        if (!results.isValid()) {
            log.error("Report input has errors {}", results.getErrors());
            bag.stopProcessing(String.format("Report input has errors %s", results.getErrors()));
        }
    }

    /**
     * Validatiert das gegebene JAXB-Objekt gegen das konfigurierte Schema
     *
     * @param object das JAXB-Objekt
     * @param <T> der Typ des Objekts
     * @return ein Validierungsergebnis
     */
    private <T> Result<Boolean, XMLSyntaxError> validate(final T object) {
        final CollectingErrorEventHandler h = new CollectingErrorEventHandler();
        final String result = this.conversionService.writeXml(object, this.schema, h);
        return new Result<>(StringUtils.isNotBlank(result), h.getErrors());
    }
}
