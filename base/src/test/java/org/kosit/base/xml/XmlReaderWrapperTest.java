package org.kosit.base.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XmlReaderWrapperTest {

    private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";

    private static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";

    private static final String UNKNOWN_FEATURE = "http://example.org/features/unknown";

    private XMLReader delegate;

    private XmlReaderWrapper wrapper;

    @BeforeEach
    public void setup() throws SAXException, ParserConfigurationException {
        this.delegate = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        this.wrapper = new XmlReaderWrapper(this.delegate);
    }

    @Test
    public void getFeatureAnswersTheSaxonExpectations() {
        assertThat(this.wrapper.getFeature(NAMESPACES_FEATURE)).isTrue();
        assertThat(this.wrapper.getFeature(NAMESPACE_PREFIXES_FEATURE)).isFalse();
        assertThat(this.wrapper.getFeature(UNKNOWN_FEATURE)).isFalse();
    }

    @Test
    public void setFeatureAcceptsTheSupportedCombinations() {
        assertThatCode(() -> {
            this.wrapper.setFeature(NAMESPACES_FEATURE, true);
            this.wrapper.setFeature(NAMESPACE_PREFIXES_FEATURE, false);
            // unknown features are silently dropped instead of failing the parse
            this.wrapper.setFeature(UNKNOWN_FEATURE, true);
            this.wrapper.setFeature(UNKNOWN_FEATURE, false);
        }).doesNotThrowAnyException();
    }

    @Test
    public void setFeatureRejectsTheUnsupportedCombinations() {
        assertThatThrownBy(() -> this.wrapper.setFeature(NAMESPACES_FEATURE, false)).isInstanceOf(SAXNotRecognizedException.class)
                .hasMessageContaining(NAMESPACES_FEATURE);
        assertThatThrownBy(() -> this.wrapper.setFeature(NAMESPACE_PREFIXES_FEATURE, true)).isInstanceOf(SAXNotRecognizedException.class)
                .hasMessageContaining(NAMESPACE_PREFIXES_FEATURE);
    }

    @Test
    public void propertiesAreDelegated() throws SAXException {
        assertThat(this.wrapper.getProperty("http://xml.org/sax/properties/lexical-handler")).isNull();
        assertThatThrownBy(() -> this.wrapper.getProperty("http://example.org/properties/unknown"))
                .isInstanceOf(SAXNotRecognizedException.class);
        assertThatThrownBy(() -> this.wrapper.setProperty("http://example.org/properties/unknown", "value"))
                .isInstanceOf(SAXNotRecognizedException.class);
    }

    @Test
    public void handlersAreDelegated() {
        final EntityResolver entityResolver = (publicId, systemId) -> null;
        final ErrorHandler errorHandler = new LoggingSaxErrorHandler();
        final DefaultHandler contentHandler = new DefaultHandler();

        this.wrapper.setEntityResolver(entityResolver);
        this.wrapper.setErrorHandler(errorHandler);
        this.wrapper.setContentHandler(contentHandler);
        this.wrapper.setDTDHandler(contentHandler);

        assertThat(this.wrapper.getEntityResolver()).isSameAs(entityResolver);
        assertThat(this.wrapper.getErrorHandler()).isSameAs(errorHandler);
        assertThat(this.wrapper.getContentHandler()).isSameAs(contentHandler);
        assertThat(this.wrapper.getDTDHandler()).isSameAs(contentHandler);
        // the wrapper holds no state of its own
        assertThat(this.delegate.getEntityResolver()).isSameAs(entityResolver);
        assertThat(this.delegate.getErrorHandler()).isSameAs(errorHandler);
        assertThat(this.delegate.getContentHandler()).isSameAs(contentHandler);
        assertThat(this.delegate.getDTDHandler()).isSameAs(contentHandler);
    }

    @Test
    public void parseIsDelegated() throws Exception {
        final List<String> elements = new ArrayList<>();
        this.wrapper.setContentHandler(new DefaultHandler() {

            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
                elements.add(qName);
            }
        });

        this.wrapper.parse(new InputSource(new StringReader("<doc><child/></doc>")));
        assertThat(elements).containsExactly("doc", "child");
    }
}
