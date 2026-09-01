package org.kosit.base.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.jupiter.api.Test;

public class XmlHelperTest {

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
}
