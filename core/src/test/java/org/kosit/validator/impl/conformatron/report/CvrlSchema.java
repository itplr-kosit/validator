package org.kosit.validator.impl.conformatron.report;

import static org.assertj.core.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Validates a generated report against the XVRL schema. CVRL is a profile of XVRL, so a report that does not validate
 * is not a CVRL report — and the ways to get this wrong are quiet ones: a required attribute we never knew about, an
 * element in the wrong order, an invented attribute where the schema has a real one. Reading the schema by hand finds
 * those late; this finds them on every test run.
 * <p>
 * XVRL allows arbitrary foreign attributes ({@code anyAttribute processContents="skip"}), so the CVRL extension
 * attributes pass; what is checked is everything XVRL does define.
 * </p>
 */
final class CvrlSchema {

    private static final String XSD_PATH = "/xsd/xvrl-1.0.xsd";

    private static final Schema SCHEMA = load();

    private CvrlSchema() {
        // static utility
    }

    private static Schema load() {
        final URL xsd = CvrlSchema.class.getResource(XSD_PATH);
        if (xsd == null) {
            throw new IllegalStateException("XVRL schema not on the test classpath: " + XSD_PATH);
        }
        try {
            return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(xsd);
        } catch (final Exception e) {
            throw new IllegalStateException("Can not read the XVRL schema " + XSD_PATH, e);
        }
    }

    /**
     * Fails the calling test when the report violates the XVRL schema, listing every violation rather than only the
     * first — one structural mistake usually produces several, and seeing them together is what makes them fixable.
     *
     * @param cvrl the serialized report
     */
    static void assertValid(final byte[] cvrl) {
        final List<String> violations = new ArrayList<>();
        final Validator validator = SCHEMA.newValidator();
        validator.setErrorHandler(new ErrorHandler() {

            @Override
            public void warning(final SAXParseException e) {
                // warnings do not make a document invalid
            }

            @Override
            public void error(final SAXParseException e) {
                violations.add("line " + e.getLineNumber() + ": " + e.getMessage());
            }

            @Override
            public void fatalError(final SAXParseException e) {
                violations.add("line " + e.getLineNumber() + " (fatal): " + e.getMessage());
            }
        });
        try {
            validator.validate(new StreamSource(new ByteArrayInputStream(cvrl)));
        } catch (final Exception e) {
            violations.add("validation aborted: " + e.getMessage());
        }
        if (!violations.isEmpty()) {
            fail("The report does not validate against XVRL:%n  %s", String.join(String.format("%n  "), violations));
        }
    }
}
