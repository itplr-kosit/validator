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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.CollectingErrorEventHandler;
import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.reportInput.ValidationResultsXmlSchema;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;
import de.kosit.validationtool.model.scenarios.ScenarioType;

/**
 * Schema-Validierung der Eingabe-Datei mittels Schema-Definition aus dem identifizierten Szenario.
 * 
 * @author Andreas Penski
 */
@Slf4j
public class SchemaValidationAction implements CheckAction {

    private static Result<Boolean, XMLSyntaxError> validate(final byte[] document, final ScenarioType scenarioType,
            final Bag results) {
        log.debug("Validating document using scenario {}", scenarioType.getName());
        final CollectingErrorEventHandler errorHandler = new CollectingErrorEventHandler();
        try ( final InputStream input = new ByteArrayInputStream(document) ) {
            final Validator validator = ObjectFactory.createValidator(scenarioType.getSchema());
            validator.setErrorHandler(errorHandler);
            validator.validate(new StreamSource(input));
            return new Result<>(!errorHandler.hasErrors(), errorHandler.getErrors());
        } catch (final SAXException | IOException e) {
            final String msg = String.format("Error processing schema validation for scenario %s", scenarioType.getName());
            log.error(msg, e);
            results.addProcessingError(msg);
            return new Result<>(Boolean.FALSE);
        }
    }

    @Override
    public void check(final Bag results) {
        final CreateReportInput report = results.getReportInput();
        final ScenarioType scenario = results.getScenarioSelectionResult().getObject();
        final Result<Boolean, XMLSyntaxError> validateResult = validate(results.getInput().getContent(), scenario, results);
        results.setSchemaValidationResult(validateResult);
        final ValidationResultsXmlSchema result = new ValidationResultsXmlSchema();
        report.setValidationResultsXmlSchema(result);
        result.getResource().addAll(scenario.getValidateWithXmlSchema().getResource());
        if (!validateResult.isValid()) {
            result.getXmlSyntaxError().addAll(validateResult.getErrors());
        }
    }



    @Override
    public boolean isSkipped(final Bag results) {
        return hasNoSchema(results);
    }

    private static boolean hasNoSchema(final Bag results) {
        return results.getScenarioSelectionResult() == null || results.getScenarioSelectionResult().getObject().getSchema() == null;
    }
}
