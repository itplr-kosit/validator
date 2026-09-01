package org.kosit.base.xml;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.validation.SchemaFactory;

import org.jspecify.annotations.Nullable;
import org.kosit.base.string.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class XmlHelper {

    public static final String DISALLOW_DOCTYPE_DECL_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

    public static final String LOAD_EXTERNAL_DTD_FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /** The character used to replace all characters that are not allowed in an {@code xs:NCName}. */
    public static final char NCNAME_REPLACEMENT_CHAR = '_';

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlHelper.class);

    private static final String JDK_XERCES_CLASS = "com.sun.org.apache.xerces.internal.impl.Constants";

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

    public static void forceOpenJdkXmlImplementation() {
        if (!isOpenJdkXmlImplementationAvailable()) {
            throw new IllegalStateException("No OpenJDK version of Xerces found");
        }
    }

    /**
     * Set a feature on a {@link DocumentBuilderFactory}, logging a warning if the feature is not supported.
     *
     * @param factory The document builder factory to set the feature on. May not be <code>null</code>.
     * @param feature The parser feature to set. May not be <code>null</code>.
     * @param bValue The value to set for the feature.
     */
    public static void setFeature(final DocumentBuilderFactory factory, final String feature, final boolean bValue) {
        try {
            factory.setFeature(feature, bValue);
        } catch (final ParserConfigurationException ex) {
            LOGGER.warn("Failed to set feature '" + feature + "' to " + bValue + " on XML DocumentBuilderFactory: " + ex.getMessage());
        }
    }

    public static DocumentBuilder createSafeDocumentBuilder() {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            setFeature(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
            setFeature(factory, DISALLOW_DOCTYPE_DECL_FEATURE, true);
            setFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
            setFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
            setFeature(factory, LOAD_EXTERNAL_DTD_FEATURE, false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file");
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
    public static void setProperty(final SchemaFactory factory, final String property, final Object value) {
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
    public static SchemaFactory createSafeSchemaFactory() {
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
    public static void setProperty(final XMLInputFactory factory, final String property, final Object value) {
        try {
            factory.setProperty(property, value);
        } catch (final IllegalArgumentException ex) {
            throw new IllegalStateException("Failed to set property '" + property + "' to '" + value
                    + "' on XML InputFactory. Maybe an unsupported JAXP implementation is used.", ex);
        }
    }

    public static XMLInputFactory createSecureXmlInputFactory() {
        final XMLInputFactory factory = XMLInputFactory.newFactory();
        setProperty(factory, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        setProperty(factory, XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        setProperty(factory, XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        return factory;
    }

    private static boolean isLatin1Letter(final char c) {
        // Letters from the Latin-1 supplement - excluding the multiplication and the division sign
        return c >= 0x00C0 && c <= 0x00FF && c != 0x00D7 && c != 0x00F7;
    }

    /**
     * @param c the character to check
     * @return <code>true</code> if the provided character may be used as the first character of an {@code xs:NCName}.
     */
    public static boolean isNCNameStartChar(final char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c == '_' || isLatin1Letter(c);
    }

    /**
     * @param c the character to check
     * @return <code>true</code> if the provided character may be used as a non-first character of an {@code xs:NCName}.
     */
    public static boolean isNCNameChar(final char c) {
        return isNCNameStartChar(c) || c >= '0' && c <= '9' || c == '-' || c == '.';
    }

    /**
     * @param value the value to check. May be <code>null</code>.
     * @return <code>true</code> if the provided value is a valid {@code xs:NCName} according to the safe subset
     *         described at {@link #createValidNCName(String)}.
     */
    public static boolean isValidNCName(final @Nullable String value) {
        if (StringHelper.isEmpty(value))
            return false;

        if (!isNCNameStartChar(value.charAt(0)))
            return false;

        for (int i = 1; i < value.length(); i++)
            if (!isNCNameChar(value.charAt(i)))
                return false;

        return true;
    }

    /**
     * Converts an arbitrary string to a valid {@code xs:NCName} - the base type of {@code xs:ID} and therefore of the
     * {@code xml:id} attribute. Every character that is not allowed is replaced with {@value #NCNAME_REPLACEMENT_CHAR}.
     * If the first character is allowed inside but not at the start of an {@code xs:NCName} - like a digit - a leading
     * {@value #NCNAME_REPLACEMENT_CHAR} is prepended.
     *
     * <p>
     * The set of characters considered valid is deliberately a safe subset of the XML 1.0 {@code NCName} production:
     * only ASCII letters, Latin-1 letters, digits, {@code '_'}, {@code '-'} and {@code '.'} are kept. That is
     * sufficient for all human readable identifiers occurring in validator configurations and guarantees that the
     * result is accepted by every XML Schema validator.
     *
     * @param value the value to be converted. May be <code>null</code>.
     * @return <code>null</code> if the provided value is <code>null</code> or empty, the unchanged value if it already
     *         is a valid {@code xs:NCName} and the converted value otherwise.
     */
    public static @Nullable String createValidNCName(final @Nullable String value) {
        if (StringHelper.isEmpty(value))
            return null;

        if (isValidNCName(value))
            return value;

        final StringBuilder ret = new StringBuilder(value.length() + 1);
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if (i == 0 && !isNCNameStartChar(c)) {
                // A digit, a '-' or a '.' may be kept, but needs a valid start character upfront
                ret.append(NCNAME_REPLACEMENT_CHAR);
                if (isNCNameChar(c))
                    ret.append(c);
            } else
                ret.append(isNCNameChar(c) ? c : NCNAME_REPLACEMENT_CHAR);
        }
        return ret.toString();
    }

    private XmlHelper() {
    }
}
