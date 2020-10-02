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

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.URIResolver;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.CollectingErrorEventHandler;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.EngineInformation;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;

import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.s9api.BuildingContentHandler;
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
 * worden sein, so wird ein das Fallback-Szenario verwend und ein default report} erzeugt.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class CreateReportAction implements CheckAction {

    /**
     * Wrapper to fix some inconsistencies between sax and saxon. Saxon tries to set some properties which has no effect
     * on {@link JAXBSource}'s XMLReader, but it throws exceptions on unknown properties. This just drops this
     * exceptions.
     */
    private static class ReaderWrapper implements XMLReader {

        private static final String SAX_FEATURES_NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";

        private static final String SAX_FEATURES_NAMESPACES = "http://xml.org/sax/features/namespaces";

        private final XMLReader delegate;

        public ReaderWrapper(final XMLReader xmlReader) {
            this.delegate = xmlReader;
        }

        @Override
        public boolean getFeature(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
            if (SAX_FEATURES_NAMESPACES.equals(name)) {
                return true;
            } else if (SAX_FEATURES_NAMESPACE_PREFIXES.equals(name)) {
                return false;
            }
            // just return false on unknown properties
            return false;
        }

        @Override
        public void setFeature(final String name, final boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
            // this inverts the logic from JaxbSource pseude parser
            if (name.equals(SAX_FEATURES_NAMESPACES) && !value) {
                throw new SAXNotRecognizedException(name);
            }
            if (name.equals(SAX_FEATURES_NAMESPACE_PREFIXES) && value) {
                throw new SAXNotRecognizedException(name);
            }
        }

        @Override
        public Object getProperty(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
            return this.delegate.getProperty(name);
        }

        @Override
        public void setProperty(final String name, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
            this.delegate.setProperty(name, value);
        }

        @Override
        public void setEntityResolver(final EntityResolver resolver) {
            this.delegate.setEntityResolver(resolver);
        }

        @Override
        public EntityResolver getEntityResolver() {
            return this.delegate.getEntityResolver();
        }

        @Override
        public void setDTDHandler(final DTDHandler handler) {
            this.delegate.setDTDHandler(handler);
        }

        @Override
        public DTDHandler getDTDHandler() {
            return this.delegate.getDTDHandler();
        }

        @Override
        public void setContentHandler(final ContentHandler handler) {
            this.delegate.setContentHandler(handler);
        }

        @Override
        public ContentHandler getContentHandler() {
            return this.delegate.getContentHandler();
        }

        @Override
        public void setErrorHandler(final ErrorHandler handler) {
            this.delegate.setErrorHandler(handler);
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return this.delegate.getErrorHandler();
        }

        @Override
        public void parse(final InputSource input) throws IOException, SAXException {
            this.delegate.parse(input);
        }

        @Override
        public void parse(final String systemId) throws IOException, SAXException {
            this.delegate.parse(systemId);
        }
    }

    private static final String ERROR_MESSAGE_ELEMENT = "error-message";

    private final Processor processor;

    private final ConversionService conversionService;

    private final URIResolver resolver;

    private final UnparsedTextURIResolver unparsedTextURIResolver;

    private static XsltExecutable loadFromScenario(final Scenario object) {
        return object.getReportTransformation().getExecutable();
    }

    @Override
    public void check(final Bag results) {
        final DocumentBuilder documentBuilder = this.processor.newDocumentBuilder();
        try {

            final XdmNode parsedDocument = results.getParserResult().isValid() ? results.getParserResult().getObject()
                    : createErrorInformation(results.getParserResult().getErrors());

            final Marshaller marshaller = this.conversionService.getJaxbContext().createMarshaller();
            final JAXBSource source = new JAXBSource(marshaller, results.getReportInput());
            // wrap to circumvent inconsistency between sax and saxon
            source.setXMLReader(new ReaderWrapper(source.getXMLReader()));

            final XdmNode root = documentBuilder.build(source);
            final XsltTransformer transformer = getTransformation(results).load();
            transformer.setInitialContextNode(root);
            final CollectingErrorEventHandler e = new CollectingErrorEventHandler();
            transformer.setMessageListener(e);
            transformer.setURIResolver(this.resolver);
            if (this.unparsedTextURIResolver != null) {
                transformer.getUnderlyingController().setUnparsedTextURIResolver(this.unparsedTextURIResolver);
            }
            if (parsedDocument != null) {
                transformer.setParameter(new QName("input-document"), parsedDocument);
            }
            final XdmDestination destination = new XdmDestination();
            transformer.setDestination(destination);
            transformer.transform();
            results.setReport(destination.getXdmNode());

        } catch (final SaxonApiException | SAXException | JAXBException e) {
            log.error("Error creating final report", e);
            results.stopProcessing("Can not create final report: " + e.getMessage());
        }
    }

    private XdmNode createErrorInformation(final Collection<XMLSyntaxError> errors) throws SaxonApiException, SAXException {
        final BuildingContentHandler contentHandler = this.processor.newDocumentBuilder().newBuildingContentHandler();
        contentHandler.startDocument();
        contentHandler.startElement(EngineInformation.getFrameworkNamespace(), ERROR_MESSAGE_ELEMENT, ERROR_MESSAGE_ELEMENT,
                new AttributesImpl());
        final String message = errors.stream().map(XMLSyntaxError::getMessage).collect(Collectors.joining());
        contentHandler.characters(message.toCharArray(), 0, message.length());
        contentHandler.endElement(EngineInformation.getFrameworkNamespace(), ERROR_MESSAGE_ELEMENT, ERROR_MESSAGE_ELEMENT);
        return contentHandler.getDocumentNode();
    }

    private static XsltExecutable getTransformation(final Bag results) {
        return loadFromScenario(results.getScenarioSelectionResult().getObject());
    }

}
