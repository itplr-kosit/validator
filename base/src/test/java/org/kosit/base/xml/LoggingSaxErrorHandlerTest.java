package org.kosit.base.xml;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

public class LoggingSaxErrorHandlerTest {

    private static final String INVALID_XSD = """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:element name="e" type="xs:doesNotExist"/>
            </xs:schema>""";

    private static SAXParseException createException(final String message) {
        return new SAXParseException(message, null, "test.xml", 4, 17);
    }

    @Test
    public void allEventsAreSwallowed() {
        final LoggingSaxErrorHandler handler = new LoggingSaxErrorHandler();
        assertThatCode(() -> {
            handler.warning(createException("a warning"));
            handler.error(createException("an error"));
            handler.fatalError(createException("a fatal error"));
        }).doesNotThrowAnyException();
    }

    @Test
    public void anExceptionWithoutAMessageIsHandled() {
        final LoggingSaxErrorHandler handler = new LoggingSaxErrorHandler();
        assertThatCode(() -> handler.error(new SAXParseException(null, null))).doesNotThrowAnyException();
    }

    @Test
    public void schemaErrorsDoNotAbortTheSchemaFactory() {
        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setErrorHandler(new LoggingSaxErrorHandler());

        // the handler only logs, so a schema with an unresolvable type is reported but does not throw
        assertThatCode(() -> factory.newSchema(new StreamSource(new StringReader(INVALID_XSD)))).doesNotThrowAnyException();
    }
}
