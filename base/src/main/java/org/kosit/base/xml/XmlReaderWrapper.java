package org.kosit.base.xml;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Wrapper to fix some inconsistencies between sax and saxon. Saxon tries to set some properties which has no effect on
 * {@code jakarta.xml.bind.util.JAXBSource}'s XMLReader, but it throws exceptions on unknown properties. This just drops
 * this exceptions.
 */
public class XmlReaderWrapper implements XMLReader {

    private static final String SAX_FEATURES_NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";

    private static final String SAX_FEATURES_NAMESPACES = "http://xml.org/sax/features/namespaces";

    private final XMLReader delegate;

    public XmlReaderWrapper(final XMLReader xmlReader) {
        this.delegate = xmlReader;
    }

    @Override
    public boolean getFeature(final String name) {
        if (SAX_FEATURES_NAMESPACES.equals(name)) {
            return true;
        }
        if (SAX_FEATURES_NAMESPACE_PREFIXES.equals(name)) {
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
    public EntityResolver getEntityResolver() {
        return this.delegate.getEntityResolver();
    }

    @Override
    public void setEntityResolver(final EntityResolver resolver) {
        this.delegate.setEntityResolver(resolver);
    }

    @Override
    public DTDHandler getDTDHandler() {
        return this.delegate.getDTDHandler();
    }

    @Override
    public void setDTDHandler(final DTDHandler handler) {
        this.delegate.setDTDHandler(handler);
    }

    @Override
    public ContentHandler getContentHandler() {
        return this.delegate.getContentHandler();
    }

    @Override
    public void setContentHandler(final ContentHandler handler) {
        this.delegate.setContentHandler(handler);
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return this.delegate.getErrorHandler();
    }

    @Override
    public void setErrorHandler(final ErrorHandler handler) {
        this.delegate.setErrorHandler(handler);
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
