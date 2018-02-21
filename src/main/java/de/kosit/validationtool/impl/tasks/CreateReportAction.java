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

import java.net.URI;

import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

import lombok.RequiredArgsConstructor;

import de.kosit.validationtool.impl.*;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.scenarios.ScenarioType;

import net.sf.saxon.s9api.*;

/**
 * Erzeugt den Report auf Basis der gesammelten Informationen über den Prüfling. Sollte kein Szenario identifiziert
 * worden sein, so wird ein {@link ScenarioRepository#getNoScenarioReport() default report} erzeugt.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public class CreateReportAction implements CheckAction {

    private final Processor processor;

    private final ConversionService conversionService;

    private final ScenarioRepository repository;

    private final URI contentRepository;

    private static XsltExecutable loadFromScenario(ScenarioType object) {
        return object.getReportTransformation().getExecutable();
    }

    @Override
    public void check(Bag results) {
        final DocumentBuilder documentBuilder = processor.newDocumentBuilder();
        try {

            final Document inputDoc = results.getParserResult().isValid() ? results.getParserResult().getObject()
                    : ObjectFactory.createDocumentBuilder(true).newDocument();

            final XdmNode parsedDocument = documentBuilder.build(new DOMSource(inputDoc));
            final Document reportInput = conversionService.writeDocument(results.getReportInput());
            final XdmNode root = documentBuilder.build(new DOMSource(reportInput));
            final XsltTransformer transformer = getTransformation(results).load();
            transformer.setInitialContextNode(root);
            CollectingErrorEventHandler e = new CollectingErrorEventHandler();
            RelativeUriResolver resolver = new RelativeUriResolver(contentRepository);
            transformer.setMessageListener(e);
            transformer.setURIResolver(resolver);
            transformer.getUnderlyingController().setUnparsedTextURIResolver(resolver);
            transformer.setParameter(new QName("input-document"), parsedDocument);
            Document result = ObjectFactory.createDocumentBuilder(false).newDocument();
            transformer.setDestination(new DOMDestination(result));
            transformer.transform();
            results.setReport(result);

        } catch (SaxonApiException e) {
            throw new IllegalStateException("Can not create final report", e);
        }
    }

    private XsltExecutable getTransformation(Bag results) {
        final Result<ScenarioType, String> scenario = results.getScenarioSelectionResult();
        return scenario != null && scenario.isValid() ? loadFromScenario(scenario.getObject()) : loadFallback();
    }

    private XsltExecutable loadFallback() {
        return repository.getNoScenarioReport();
    }
}
