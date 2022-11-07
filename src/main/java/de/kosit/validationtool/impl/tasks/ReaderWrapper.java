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

import org.xml.sax.*;

import javax.xml.bind.util.JAXBSource;
import java.io.IOException;

/**
 * Wrapper to fix some inconsistencies between sax and saxon. Saxon tries to set some properties which has no effect on
 * {@link JAXBSource}'s XMLReader, but it throws exceptions on unknown properties. This just drops this exceptions.
 */
public class ReaderWrapper implements XMLReader {

    private static final String SAX_FEATURES_NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";

    private static final String SAX_FEATURES_NAMESPACES = "http://xml.org/sax/features/namespaces";

    private final XMLReader delegate;

    public ReaderWrapper(final XMLReader xmlReader) {
        this.delegate = xmlReader;
    }

    @Override
    public boolean getFeature(final String name) {
        if (SAX_FEATURES_NAMESPACES.equals(name)) {
            return true;
        } else if (SAX_FEATURES_NAMESPACE_PREFIXES.equals(name)) {
            return false;
        }
        // just return false on unknown properties
        return false;
    }

    @Override
    public void setFeature(final String name, final boolean value) throws SAXNotRecognizedException {
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
