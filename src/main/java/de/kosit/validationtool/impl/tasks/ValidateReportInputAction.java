/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
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

import javax.xml.validation.Schema;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kosit.validationtool.impl.CollectingErrorEventHandler;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;

/**
 * Validates the gathered information about the test object. Additional check.
 *
 * @author Andreas Penski
 */
public class ValidateReportInputAction implements CheckAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateReportInputAction.class);

    private final ConversionService conversionService;

    private final Schema schema;

    @Override
    public void check(final Bag bag) {
        final Result<Boolean, XMLSyntaxError> results = validate(bag.getReportInput());
        if (!results.isValid()) {
            LOGGER.error("Report input has errors {}", results.getErrors());
            bag.stopProcessing("Report input has errors " + results.getErrors());
        }
    }

    /**
     * Validates the given JAXB object against the configured schema
     *
     * @param object the JAXB object
     * @param <T> the type of the object
     * @return a validation result
     */
    private <T> Result<Boolean, XMLSyntaxError> validate(final T object) {
        final CollectingErrorEventHandler h = new CollectingErrorEventHandler();
        final String result = this.conversionService.writeXml(object, this.schema, h);
        return new Result<>(StringUtils.isNotBlank(result), h.getErrors());
    }

    public ValidateReportInputAction(final ConversionService conversionService, final Schema schema) {
        this.conversionService = conversionService;
        this.schema = schema;
    }
}
