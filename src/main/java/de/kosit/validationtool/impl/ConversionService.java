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

package de.kosit.validationtool.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringJoiner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import lombok.extern.slf4j.Slf4j;

/**
 * JAXB Conversion Utility.
 */
@Slf4j
public class ConversionService {

    /**
     * Exception while serializing/deserializing with jaxb.
     */
    public class ConversionExeption extends RuntimeException {

        /**
         * Constructor.
         *
         * @param message the message.
         * @param cause the cause
         */
        public ConversionExeption(final String message, final Exception cause) {
            super(message, cause);
        }

        /**
         * Constructor.
         *
         * @param message the message.
         */
        public ConversionExeption(final String message) {
            super(message);
        }
    }

    private static final int MAX_LOG_CONTENT = 50;
    // context setup
    private JAXBContext jaxbContext;

    private static <T> QName createQName(final T model) {
        return new QName(model.getClass().getSimpleName().toLowerCase());
    }

    private void checkInputEmpty(final URI xml) {
        if (xml == null) {
            throw new ConversionExeption("Can not unmarshal from empty input");
        }
    }

    private <T> void checkTypeEmpty(final Class<T> type) {
        if (type == null) {
            throw new ConversionExeption("Can not unmarshal without type information. Need to specify a target type");
        }
    }

    /**
     * Initialisiert den default context; Alle Packages mit {@link XmlRegistry XmlRegistries}.
     */
    public void initialize() {
        final Collection<Package> p = new ArrayList<>();
        p.add(de.kosit.validationtool.model.reportInput.ObjectFactory.class.getPackage());
        p.add(de.kosit.validationtool.model.scenarios.ObjectFactory.class.getPackage());
        initialize(p);
    }

    public void initialize(final Package... context) {
        initialize(Arrays.asList(context));
    }

    /**
     * Initialisiert den conversion service mit den angegegebenen Packages.
     *
     * @param context packages für den JAXB Kontext
     */
    public void initialize(final Collection<Package> context) {
        final String[] packages = context != null ? context.stream().map(Package::getName).toArray(String[]::new) : new String[0];
        final StringJoiner joiner = new StringJoiner(":");
        Arrays.stream(packages).forEach(p -> joiner.add(p));
        initialize(joiner.toString());
    }

    /**
     * Initialsiert den conversion service mit dem angegebenen Kontextpfad
     *
     * @param contextPath der Kontextpfad
     */
    private void initialize(final String contextPath) {
        try {
            this.jaxbContext = JAXBContext.newInstance(contextPath);
        } catch (final JAXBException e) {
            throw new IllegalStateException(String.format("Can not create JAXB context for given context: %s", contextPath), e);
        }
    }

    private JAXBContext getJaxbContext() {
        if (this.jaxbContext == null) {
            initialize();
        }
        return this.jaxbContext;
    }

    /**
     * Unmarshalls a specifc xml model into a defined java object.
     *
     * @param xml the xml
     * @param type the expected type created
     * @param <T> type information
     * @return the created object
     */
    public <T> T readXml(final URI xml, final Class<T> type) {
        return readXml(xml, type, null, null);
    }

    public <T> T readXml(final URI xml, final Class<T> type, final Schema schema) {
        return readXml(xml, type, schema, null);
    }

    public <T> T readXml(final URI xml, final Class<T> type, final Schema schema, final ValidationEventHandler handler) {
        checkInputEmpty(xml);
        checkTypeEmpty(type);
        CollectingErrorEventHandler defaultHandler = null;
        ValidationEventHandler handler2Use = handler;
        if (schema != null && handler == null) {
            defaultHandler = new CollectingErrorEventHandler();
            handler2Use = defaultHandler;
        }
        try {
            final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
            inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
            inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            final XMLStreamReader xsr = inputFactory.createXMLStreamReader(new StreamSource(xml.toASCIIString()));
            final Unmarshaller u = getJaxbContext().createUnmarshaller();
            u.setSchema(schema);

            u.setEventHandler(handler2Use);
            final T value = u.unmarshal(xsr, type).getValue();
            if (defaultHandler != null && defaultHandler.hasErrors()) {
                throw new ConversionExeption(
                        String.format("Schema errors while reading content from %s: %s", xml, defaultHandler.getErrorDescription()));
            }

            return value;
        } catch (final JAXBException | XMLStreamException e) {
            throw new ConversionExeption(String.format("Can not unmarshal to type %s from %s", type.getSimpleName(), xml.toString()), e);
        }
    }

    /**
     * Serializing an object to xml.
     *
     * @param model the object
     * @param <T> type of the object
     * @return the serialized form.
     */
    public <T> String writeXml(final T model) {
        return writeXml(model, null, null);
    }

    public <T> String writeXml(final T model, final Schema schema, final ValidationEventHandler handler) {
        if (model == null) {
            throw new ConversionExeption("Can not serialize null");
        }
        try ( final StringWriter w = new StringWriter() ) {
            final JAXBIntrospector introspector = getJaxbContext().createJAXBIntrospector();
            final Marshaller marshaller = getJaxbContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setSchema(schema);
            marshaller.setEventHandler(handler);
            final XMLOutputFactory xof = XMLOutputFactory.newFactory();
            final XMLStreamWriter xmlStreamWriter = xof.createXMLStreamWriter(w);
            if (null == introspector.getElementName(model)) {
                final JAXBElement jaxbElement = new JAXBElement(createQName(model), model.getClass(), model);
                marshaller.marshal(jaxbElement, xmlStreamWriter);
            } else {
                marshaller.marshal(model, xmlStreamWriter);
            }
            xmlStreamWriter.flush();
            return w.toString();
        } catch (final JAXBException | IOException | XMLStreamException e) {
            throw new ConversionExeption(String.format("Error serializing Object %s", model.getClass().getName()), e);
        }
    }

    public <T> Document writeDocument(final T input) {
        if (input == null) {
            throw new ConversionExeption("Can not serialize null");
        }
        final DocumentBuilder builder = ObjectFactory.createDocumentBuilder(false);
        final Document document = builder.newDocument();

        // Marshal the Object to a Document
        Marshaller marshaller = null;
        try {
            marshaller = getJaxbContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(input, document);
            return document;
        } catch (final JAXBException e) {
            throw new ConversionExeption(String.format("Error serializing Object %s to document", input.getClass().getName()), e);
        }
    }

    public <T> T readDocument(final Source source, final Class<T> type) {
        try {
            final Unmarshaller u = getJaxbContext().createUnmarshaller();

            return u.unmarshal(source, type).getValue();

        } catch (final JAXBException e) {
            throw new ConversionExeption(String.format("Can not unmarshal to type %s: %s", type.getSimpleName(),
                    StringUtils.abbreviate(source.getSystemId(), MAX_LOG_CONTENT)), e);
        }
    }
}
