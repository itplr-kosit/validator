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

import de.kosit.validationtool.impl.CollectingErrorEventHandler;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.impl.RelativeUriResolver;
import de.kosit.validationtool.impl.ScenarioRepository;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.scenarios.ScenarioType;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

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

    private static XsltExecutable loadFromScenario(final ScenarioType object) {
        return object.getReportTransformation().getExecutable();
    }

    @Override
    public void check(final Bag results) {
        final DocumentBuilder documentBuilder = this.processor.newDocumentBuilder();
        try {

            final XdmNode parsedDocument = results.getParserResult().isValid() ? results.getParserResult().getObject()
                    : ObjectFactory.createProcessor().newDocumentBuilder().newBuildingContentHandler().getDocumentNode();

            final Document reportInput = this.conversionService.writeDocument(results.getReportInput());
            final XdmNode root = documentBuilder.build(new DOMSource(reportInput));
            final XsltTransformer transformer = getTransformation(results).load();
            transformer.setInitialContextNode(root);
            final CollectingErrorEventHandler e = new CollectingErrorEventHandler();
            final RelativeUriResolver resolver = new RelativeUriResolver(this.contentRepository);
            transformer.setMessageListener(e);
            transformer.setURIResolver(resolver);
            transformer.getUnderlyingController().setUnparsedTextURIResolver(resolver);
            transformer.setParameter(new QName("input-document"), parsedDocument);
            final XdmDestination destination = new XdmDestination();
            transformer.setDestination(destination);
            transformer.transform();
            results.setReport(destination.getXdmNode());

        } catch (final SaxonApiException e) {
            throw new IllegalStateException("Can not create final report", e);
        }
    }

    private static XsltExecutable getTransformation(final Bag results) {
        final Result<ScenarioType, String> scenario = results.getScenarioSelectionResult();
        return loadFromScenario(scenario.getObject());
    }


}
