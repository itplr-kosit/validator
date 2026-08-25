package org.kosit.jaxb.xml;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class XMLHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLHelper.class);

    /**
     * Set a feature on a {@link DocumentBuilderFactory}, logging a warning if the feature is not supported.
     *
     * @param factory The document builder factory to set the feature on. May not be <code>null</code>.
     * @param feature The parser feature to set. May not be <code>null</code>.
     * @param bValue The value to set for the feature.
     */
    public static void setFeature(@NonNull final DocumentBuilderFactory factory, @NonNull final String feature, final boolean bValue) {
        try {
            factory.setFeature(feature, bValue);
        } catch (final ParserConfigurationException ex) {
            LOGGER.warn("Failed to set feature '" + feature + "' to " + bValue + " on XML DocumentBuilderFactory: " + ex.getMessage());
        }
    }

    public static @NonNull DocumentBuilder createSafeDocumentBuilder() {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            setFeature(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
            setFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
            setFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
            setFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
            setFeature(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setIgnoringElementContentWhitespace(false);
            factory.setExpandEntityReferences(true);
            factory.setIgnoringComments(true);
            factory.setCoalescing(true);
            return factory.newDocumentBuilder();
        } catch (final ParserConfigurationException ex) {
            throw new IllegalStateException("Failed to create XML DocumentBuilder", ex);
        }
    }

    public static @NonNull XMLInputFactory createSafeXMLInputFactory() {
        final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        return inputFactory;
    }

    private XMLHelper() {
    }

}
