/*
 * Copyright 2017-2021  Koordinierungsstelle für IT-Standards (KoSIT)
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

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;

import lombok.RequiredArgsConstructor;

import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.model.xvrl.XVRLReportSummary;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

@RequiredArgsConstructor
public class XvrlSerializer {

    private final ConversionService conversionService;

    private final Processor processor;

    public XdmNode serialize(final XVRLReportSummary summary) throws JAXBException, SaxonApiException {
        final DocumentBuilder documentBuilder = this.processor.newDocumentBuilder();
        final Marshaller marshaller = this.conversionService.getJaxbContext().createMarshaller();
        final JAXBSource source = new JAXBSource(marshaller, summary);
        // wrap to circumvent inconsistency between sax and saxon
        source.setXMLReader(new ReaderWrapper(source.getXMLReader()));
        return documentBuilder.build(source);
    }

}
