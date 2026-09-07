package org.kosit.base.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlHelperTest {

    private static final String XML_WITH_CHILD = "<doc><child>text</child></doc>";

    private static final String XML_WITH_DOCTYPE = "<!DOCTYPE doc [<!ENTITY e 'x'>]><doc/>";

    private static final String XML_WITH_ENTITY = "<!DOCTYPE doc [<!ENTITY e 'expanded'>]><doc>&e;</doc>";

    private static final String NCNAME_XSD = """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:element name="e">
                <xs:complexType>
                  <xs:attribute name="v" type="xs:NCName"/>
                </xs:complexType>
              </xs:element>
            </xs:schema>""";

    private static boolean isAcceptedByXmlSchema(final String value) {
        try {
            final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = schemaFactory.newSchema(new StreamSource(new StringReader(NCNAME_XSD)));
            final Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader("<e v=\"" + value + "\"/>")));
            return true;
        } catch (final Exception ex) {
            return false;
        }
    }

    @Test
    public void isNCNameStartCharAndIsNCNameChar() {
        assertThat(XmlHelper.isNCNameStartChar('a')).isTrue();
        assertThat(XmlHelper.isNCNameStartChar('Z')).isTrue();
        assertThat(XmlHelper.isNCNameStartChar('_')).isTrue();
        assertThat(XmlHelper.isNCNameStartChar('ä')).isTrue();
        assertThat(XmlHelper.isNCNameStartChar('0')).isFalse();
        assertThat(XmlHelper.isNCNameStartChar('-')).isFalse();
        assertThat(XmlHelper.isNCNameStartChar('.')).isFalse();
        assertThat(XmlHelper.isNCNameStartChar(' ')).isFalse();
        assertThat(XmlHelper.isNCNameStartChar(':')).isFalse();
        assertThat(XmlHelper.isNCNameStartChar('×')).isFalse();

        assertThat(XmlHelper.isNCNameChar('a')).isTrue();
        assertThat(XmlHelper.isNCNameChar('0')).isTrue();
        assertThat(XmlHelper.isNCNameChar('-')).isTrue();
        assertThat(XmlHelper.isNCNameChar('.')).isTrue();
        assertThat(XmlHelper.isNCNameChar(' ')).isFalse();
        assertThat(XmlHelper.isNCNameChar(':')).isFalse();
    }

    @Test
    public void isValidNCName() {
        assertThat(XmlHelper.isValidNCName(null)).isFalse();
        assertThat(XmlHelper.isValidNCName("")).isFalse();
        assertThat(XmlHelper.isValidNCName("report")).isTrue();
        assertThat(XmlHelper.isValidNCName("_report-1.0")).isTrue();
        assertThat(XmlHelper.isValidNCName("Prüfbericht")).isTrue();
        assertThat(XmlHelper.isValidNCName("1report")).isFalse();
        assertThat(XmlHelper.isValidNCName("Report for eInvoice")).isFalse();
        assertThat(XmlHelper.isValidNCName("xvrl:report")).isFalse();
    }

    @Test
    public void createValidNCName() {
        assertThat(XmlHelper.createValidNCName(null)).isNull();
        assertThat(XmlHelper.createValidNCName("")).isNull();

        // Already valid values are returned unchanged
        assertThat(XmlHelper.createValidNCName("report")).isEqualTo("report");
        assertThat(XmlHelper.createValidNCName("_report-1.0")).isEqualTo("_report-1.0");
        assertThat(XmlHelper.createValidNCName("Prüfbericht")).isEqualTo("Prüfbericht");

        assertThat(XmlHelper.createValidNCName("Report for eInvoice")).isEqualTo("Report_for_eInvoice");
        assertThat(XmlHelper.createValidNCName("xvrl:report")).isEqualTo("xvrl_report");
        assertThat(XmlHelper.createValidNCName(" ")).isEqualTo("_");

        // A digit, a '-' and a '.' are valid inside, but not at the start
        assertThat(XmlHelper.createValidNCName("1report")).isEqualTo("_1report");
        assertThat(XmlHelper.createValidNCName("-report")).isEqualTo("_-report");
        assertThat(XmlHelper.createValidNCName(".report")).isEqualTo("_.report");
    }

    @Test
    public void createValidNCNameIsAcceptedByXmlSchema() {
        for (final String value : new String[] { "report", "Report for eInvoice", "Report for eInvoice 2", "1report", "-report", ".report",
                "xvrl:report", "Prüfbericht", " ", "a\tb", "☃" }) {
            final String ncName = XmlHelper.createValidNCName(value);
            assertThat(ncName).isNotNull();
            assertThat(XmlHelper.isValidNCName(ncName)).isTrue();
            assertThat(isAcceptedByXmlSchema(ncName)).isTrue();
        }
    }

    @Test
    public void openJdkXmlImplementationIsAvailable() {
        assertThat(XmlHelper.isOpenJdkXmlImplementationAvailable()).isTrue();
        assertThatCode(XmlHelper::forceOpenJdkXmlImplementation).doesNotThrowAnyException();
    }

    @Test
    public void safeDocumentBuilderFactoryIsConfigured() {
        final DocumentBuilderFactory factory = XmlHelper.createSafeDocumentBuilderFactory();
        assertThat(factory.isNamespaceAware()).isTrue();
        assertThat(factory.isValidating()).isFalse();
        assertThat(factory.isCoalescing()).isTrue();
        assertThat(factory.isIgnoringComments()).isTrue();
        assertThat(factory.isExpandEntityReferences()).isTrue();
        assertThat(factory.isIgnoringElementContentWhitespace()).isFalse();
        assertThat(factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD)).isEqualTo("");
        assertThat(factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA)).isEqualTo("file");
    }

    @Test
    public void setFeatureIgnoresAnUnsupportedFeature() {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        assertThatCode(() -> XmlHelper.setFeature(factory, "http://example.org/features/unknown", true)).doesNotThrowAnyException();
    }

    @Test
    public void safeDocumentBuilderParsesPlainXml() throws Exception {
        final Document doc = XmlHelper.createSafeDocumentBuilder().parse(new InputSource(new StringReader(XML_WITH_CHILD)));
        assertThat(doc.getDocumentElement().getLocalName()).isEqualTo("doc");
        assertThat(doc.getDocumentElement().getFirstChild().getTextContent()).isEqualTo("text");
    }

    @Test
    public void safeDocumentBuilderRejectsADoctype() {
        final DocumentBuilder builder = XmlHelper.createSafeDocumentBuilder();
        assertThatThrownBy(() -> builder.parse(new InputSource(new StringReader(XML_WITH_DOCTYPE)))).isInstanceOf(SAXException.class)
                .hasMessageContaining("DOCTYPE");
    }

    @Test
    public void safeSchemaFactoryIsForXmlSchema() {
        final SchemaFactory factory = XmlHelper.createSafeSchemaFactory();
        assertThat(factory.isSchemaLanguageSupported(XMLConstants.W3C_XML_SCHEMA_NS_URI)).isTrue();
    }

    @Test
    public void setPropertyOnSchemaFactoryFailsForAnUnsupportedProperty() {
        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        assertThatThrownBy(() -> XmlHelper.setProperty(factory, "http://example.org/properties/unknown", "value"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("XML SchemaFactory");
    }

    @Test
    public void setPropertyOnXmlInputFactoryFailsForAnUnsupportedProperty() {
        final XMLInputFactory factory = XMLInputFactory.newFactory();
        assertThatThrownBy(() -> XmlHelper.setProperty(factory, "http://example.org/properties/unknown", "value"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("XML InputFactory");
    }

    private static String readCharacters(final XMLInputFactory factory, final String xml) throws XMLStreamException {
        final XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
        final StringBuilder ret = new StringBuilder();
        while (reader.hasNext())
            if (reader.next() == XMLStreamConstants.CHARACTERS)
                ret.append(reader.getText());
        return ret.toString();
    }

    @Test
    public void secureXmlInputFactoryDoesNotExpandEntities() throws XMLStreamException {
        final XMLInputFactory factory = XmlHelper.createSecureXmlInputFactory();
        assertThat(factory.getProperty(XMLInputFactory.SUPPORT_DTD)).isEqualTo(Boolean.FALSE);
        assertThat(factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES)).isEqualTo(Boolean.FALSE);
        assertThat(factory.getProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES)).isEqualTo(Boolean.FALSE);

        // the entity declared in the internal subset is reported as a reference instead of being expanded ...
        assertThat(readCharacters(factory, XML_WITH_ENTITY)).isEmpty();
        // ... whereas a default factory expands it
        assertThat(readCharacters(XMLInputFactory.newFactory(), XML_WITH_ENTITY)).isEqualTo("expanded");
    }

    @Test
    public void safeTransformerFactoryUsesSecureProcessing() throws Exception {
        final TransformerFactory factory = XmlHelper.createSafeTransformerFactory();
        assertThat(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING)).isTrue();
    }

    @Test
    public void setFeatureOnTransformerFactoryIgnoresAnUnsupportedFeature() {
        final TransformerFactory factory = TransformerFactory.newInstance();
        assertThatCode(() -> {
            XmlHelper.setFeature(factory, "http://example.org/features/unknown", true, true);
            XmlHelper.setFeature(factory, "http://example.org/features/unknown", true, false);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getXmlAsString() throws Exception {
        final Document doc = XmlHelper.createSafeDocumentBuilder().parse(new InputSource(new StringReader(XML_WITH_CHILD)));

        assertThat(XmlHelper.getXmlAsString(doc)).contains("<doc>").contains("<child>text</child>");
        // a single node can be serialized as well
        assertThat(XmlHelper.getXmlAsString(doc.getDocumentElement().getFirstChild())).contains("<child>text</child>");
    }
}
