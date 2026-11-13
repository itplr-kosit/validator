package org.kosit.cvr.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.kosit.base.xml.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The CVR namespace is written down in three places - {@link CvrProfile#NS_CVR}, the schema and the profile Schematron
 * - and a report is only valid if all three agree. Changing it (the URN is still a draft) must therefore fail here
 * until every place is changed, instead of producing reports that the profile rejects.
 */
public class CvrNamespaceTest {

    private static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";

    private static final String NS_SCH = "http://purl.oclc.org/dsdl/schematron";

    private static Document parse(final String path) throws IOException, SAXException {
        try ( final InputStream input = CvrProfile.class.getResourceAsStream(path) ) {
            assertThat(input).as(path + " on the classpath").isNotNull();
            return XmlHelper.createSafeDocumentBuilder().parse(input);
        }
    }

    private static String text(final String path) throws IOException {
        try ( final InputStream input = CvrProfile.class.getResourceAsStream(path) ) {
            assertThat(input).as(path + " on the classpath").isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    public void testTheSchemaDefinesTheProfileNamespace() throws Exception {
        final Element schema = parse(CvrProfile.CVR_XSD_PATH).getDocumentElement();

        assertThat(schema.getNamespaceURI()).isEqualTo(NS_XSD);
        assertThat(schema.getAttribute("targetNamespace")).isEqualTo(CvrProfile.NS_CVR);
        // the prefix the schema refers to its own components with
        assertThat(schema.getAttribute("xmlns:cvr")).isEqualTo(CvrProfile.NS_CVR);
    }

    @Test
    public void testTheSchematronBindsTheProfileNamespace() throws Exception {
        final NodeList bindings = parse(CvrProfile.CVR_SCH_PATH).getElementsByTagNameNS(NS_SCH, "ns");
        String cvr = null;
        for (int i = 0; i < bindings.getLength(); ++i) {
            final Element binding = (Element) bindings.item(i);
            if ("cvr".equals(binding.getAttribute("prefix"))) {
                cvr = binding.getAttribute("uri");
            }
        }

        assertThat(cvr).as("the cvr prefix the rules are written with").isEqualTo(CvrProfile.NS_CVR);
    }

    @Test
    public void testTheSchematronComparesAgainstTheProfileNamespaceOnly() throws Exception {
        final Matcher literal = Pattern.compile("namespace-uri\\(\\)\\s*=\\s*'([^']*)'").matcher(text(CvrProfile.CVR_SCH_PATH));
        final List<String> compared = new ArrayList<>();
        while (literal.find()) {
            compared.add(literal.group(1));
        }

        assertThat(compared).isNotEmpty().containsOnly(CvrProfile.NS_CVR);
    }
}
