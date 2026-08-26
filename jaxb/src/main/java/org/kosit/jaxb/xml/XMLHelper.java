package org.kosit.jaxb.xml;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.validation.SchemaFactory;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class XMLHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLHelper.class);

    private static final String JDK_XERCES_CLASS = "com.sun.org.apache.xerces.internal.impl.Constants";

    public static void forceOpenJdkXmlImplementation() {
        if (!isOpenJdkXmlImplementationAvailable()) {
            throw new IllegalStateException("No OpenJDK version of Xerces found");
        }
    }

    public static boolean isOpenJdkXmlImplementationAvailable() {
        try {
            Class.forName(JDK_XERCES_CLASS);
            return true;
        } catch (final ClassNotFoundException e) {
            LOGGER.warn("No OpenJDK version of Xerces found. Configured security features may not have any effect.");
            LOGGER.warn("Please take care of XML security while checking your xml contents");
            return false;
        }
    }

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

    /**
     * Set a property on a {@link SchemaFactory}, failing if the property is not supported.
     *
     * @param factory The schema factory to set the property on. May not be <code>null</code>.
     * @param property The property name to set. May not be <code>null</code>.
     * @param value The value to set for the property.
     * @throws IllegalStateException if the property is not supported by the used JAXP implementation
     */
    public static void setProperty(@NonNull final SchemaFactory factory, @NonNull final String property, final Object value) {
        try {
            factory.setProperty(property, value);
        } catch (final SAXException ex) {
            throw new IllegalStateException("Failed to set property '" + property + "' to '" + value
                    + "' on XML SchemaFactory. Maybe an unsupported JAXP implementation is used.", ex);
        }
    }

    /**
     * Creates a {@link SchemaFactory} for W3C XML Schema that is hardened against XXE style attacks: external DTD
     * access is disabled and external schema access is limited to the <code>file</code> scheme.
     *
     * @return the created {@link SchemaFactory}. Never <code>null</code>.
     * @throws IllegalStateException if one of the security properties can not be set
     */
    public static @NonNull SchemaFactory createSafeSchemaFactory() {
        forceOpenJdkXmlImplementation();

        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        setProperty(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setProperty(factory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file");
        return factory;
    }

    /**
     * Set a property on a {@link XMLInputFactory}, failing if the property is not supported.
     *
     * @param factory The schema factory to set the property on. May not be <code>null</code>.
     * @param property The property name to set. May not be <code>null</code>.
     * @param value The value to set for the property.
     * @throws IllegalStateException if the property is not supported by the used JAXP implementation
     */
    public static void setProperty(@NonNull final XMLInputFactory factory, @NonNull final String property, final Object value) {
        try {
            factory.setProperty(property, value);
        } catch (final IllegalArgumentException ex) {
            throw new IllegalStateException("Failed to set property '" + property + "' to '" + value
                    + "' on XML InputFactory. Maybe an unsupported JAXP implementation is used.", ex);
        }
    }

    public static @NonNull XMLInputFactory createSafeXMLInputFactory() {
        final XMLInputFactory factory = XMLInputFactory.newFactory();
        setProperty(factory, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        setProperty(factory, XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        setProperty(factory, XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        return factory;
    }

    private XMLHelper() {
    }
}
