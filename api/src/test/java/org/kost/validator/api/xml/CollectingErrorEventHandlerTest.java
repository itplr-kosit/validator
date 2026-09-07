package org.kost.validator.api.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;
import org.kosit.base.error.SimpleError;
import org.kost.validator.api.saxon.ProcessorProvider;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventLocator;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.s9api.Message;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class CollectingErrorEventHandlerTest {

    private record TestLocator(URL url, int lineNumber, int columnNumber) implements ValidationEventLocator {

        public URL getURL() {
            return this.url;
        }

        public int getOffset() {
            return -1;
        }

        public int getLineNumber() {
            return this.lineNumber;
        }

        public int getColumnNumber() {
            return this.columnNumber;
        }

        public Object getObject() {
            return null;
        }

        public Node getNode() {
            return null;
        }
    }

    private record TestEvent(int severity, String message, Throwable linkedException,
            ValidationEventLocator locator) implements ValidationEvent {

        public int getSeverity() {
            return this.severity;
        }

        public String getMessage() {
            return this.message;
        }

        public Throwable getLinkedException() {
            return this.linkedException;
        }

        public ValidationEventLocator getLocator() {
            return this.locator;
        }
    }

    private record TestSourceLocator(String systemId, int lineNumber, int columnNumber) implements SourceLocator {

        public String getPublicId() {
            return null;
        }

        public String getSystemId() {
            return this.systemId;
        }

        public int getLineNumber() {
            return this.lineNumber;
        }

        public int getColumnNumber() {
            return this.columnNumber;
        }
    }

    private static ValidationEvent createEvent(final int severity) throws Exception {
        return new TestEvent(severity, "Invalid content", new IOException("cause"),
                new TestLocator(URI.create("file:/tmp/test.xml").toURL(), 5, 9));
    }

    private static SAXParseException createSaxException() {
        return new SAXParseException("Element type must be terminated", null, "test.xml", 4, 17);
    }

    @Test
    public void emptyHandler() {
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();
        assertThat(handler.getErrors()).isEmpty();
        assertThat(handler.hasEvents()).isFalse();
        assertThat(handler.hasErrors()).isFalse();
        assertThat(handler.getErrorDescription()).isEmpty();
    }

    @Test
    public void saxEvents() throws SAXException {
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();
        handler.warning(createSaxException());
        handler.error(createSaxException());
        handler.fatalError(createSaxException());

        assertThat(handler.getErrors()).hasSize(3);
        assertThat(handler.getErrors()).extracting(SimpleError::getSeverity).containsExactly(CTStandardSeverity.WARNING,
                CTStandardSeverity.ERROR, CTStandardSeverity.ERROR);
        final SimpleError error = handler.getErrors().get(1);
        assertThat(error.getSystemID()).isEqualTo("test.xml");
        assertThat(error.getLineNumber()).isEqualTo(4);
        assertThat(error.getColumnNumber()).isEqualTo(17);
        assertThat(error.getMessage()).isEqualTo("Element type must be terminated");
        assertThat(error.hasLinkedException()).isTrue();
        assertThat(handler.hasEvents()).isTrue();
        assertThat(handler.hasErrors()).isTrue();
    }

    @Test
    public void onlyWarningsAreNoErrors() throws SAXException {
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();
        handler.warning(createSaxException());

        assertThat(handler.hasEvents()).isTrue();
        assertThat(handler.hasErrors()).isFalse();
        assertThat(handler.getErrorDescription()).isEqualTo("[WARN] Element type must be terminated at line 4 at pos 17");
    }

    @Test
    public void errorDescriptionContainsAllEvents() throws SAXException {
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();
        handler.error(createSaxException());
        handler.warning(createSaxException());

        assertThat(handler.getErrorDescription()).isEqualTo("[ERROR] Element type must be terminated at line 4 at pos 17\n"
                + "[WARN] Element type must be terminated at line 4 at pos 17");
    }

    @Test
    public void transformerEvents() throws TransformerException {
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();
        final SourceLocator locator = new TestSourceLocator("style.xsl", 8, 2);
        handler.warning(new TransformerException("Warning message", locator));
        handler.error(new TransformerException("Error message", locator));
        handler.fatalError(new TransformerException("Fatal message", locator));

        assertThat(handler.getErrors()).hasSize(3);
        assertThat(handler.getErrors().get(0).getSeverity()).isEqualTo(CTStandardSeverity.WARNING);
        assertThat(handler.getErrors().get(1).getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(handler.getErrors().get(2).getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(handler.getErrors().get(1).getSystemID()).isEqualTo("style.xsl");
        assertThat(handler.getErrors().get(1).getLineNumber()).isEqualTo(8);
        assertThat(handler.getErrors().get(1).getColumnNumber()).isEqualTo(2);
    }

    @Test
    public void transformerEventWithoutLocator() throws TransformerException {
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();
        handler.error(new TransformerException("No location available"));

        assertThat(handler.getErrors()).hasSize(1);
        assertThat(handler.getErrors().get(0).getSystemID()).isNull();
        assertThat(handler.getErrors().get(0).hasLineOrColumnNumber()).isFalse();
    }

    @Test
    public void jaxbEvents() throws Exception {
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();

        assertThat(handler.handleEvent(createEvent(ValidationEvent.WARNING))).isTrue();
        assertThat(handler.handleEvent(createEvent(ValidationEvent.ERROR))).isTrue();
        assertThat(handler.handleEvent(createEvent(ValidationEvent.FATAL_ERROR))).isTrue();

        assertThat(handler.getErrors()).hasSize(3);
        assertThat(handler.getErrors()).extracting(SimpleError::getSeverity).containsExactly(CTStandardSeverity.WARNING,
                CTStandardSeverity.ERROR, CTStandardSeverity.ERROR);
        final SimpleError error = handler.getErrors().get(0);
        assertThat(error.getSystemID()).isEqualTo("file:/tmp/test.xml");
        assertThat(error.getLineNumber()).isEqualTo(5);
        assertThat(error.getColumnNumber()).isEqualTo(9);
        assertThat(error.getMessage()).isEqualTo("Invalid content");
        assertThat(error.getLinkedException()).isInstanceOf(IOException.class);
    }

    @Test
    public void jaxbEventWithUnknownSeverity() {
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();
        assertThatThrownBy(() -> handler.handleEvent(new TestEvent(4711, "Unknown", null, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("4711");
    }

    @Test
    public void jaxbProcessingStopsAfter50Events() throws Exception {
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();
        for (int i = 1; i < 50; i++) {
            assertThat(handler.handleEvent(createEvent(ValidationEvent.ERROR))).as("Event " + i).isTrue();
        }
        // the 50th event stops the processing
        assertThat(handler.handleEvent(createEvent(ValidationEvent.ERROR))).isFalse();
        assertThat(handler.getErrors()).hasSize(50);
    }

    @Test
    public void saxonMessages() throws SaxonApiException {
        final XdmNode content = ProcessorProvider.getProcessor().newDocumentBuilder()
                .build(new StreamSource(new StringReader("<msg>Attention</msg>")));
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();
        handler.accept(new Message(content, new QName("terminated"), true, new Loc("style.xsl", 3, 7)));
        handler.accept(new Message(content, new QName("info"), false, new Loc("style.xsl", 4, 1)));

        assertThat(handler.getErrors()).hasSize(2);
        final SimpleError error = handler.getErrors().get(0);
        assertThat(error.getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(error.getMessage()).isEqualTo("Error processing Attention");
        assertThat(error.getSystemID()).isEqualTo("style.xsl");
        assertThat(error.getLineNumber()).isEqualTo(3);
        assertThat(error.getColumnNumber()).isEqualTo(7);
        assertThat(handler.getErrors().get(1).getSeverity()).isEqualTo(CTStandardSeverity.WARNING);
    }
}
