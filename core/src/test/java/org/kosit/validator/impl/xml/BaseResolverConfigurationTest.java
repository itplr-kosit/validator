package org.kosit.validator.impl.xml;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.saxon.ProcessorProvider;
import org.kosit.validator.xml.resolve.StrictRelativeResolvingStrategy;
import org.kosit.validator.xvrl.XvrlSerializer;
import org.kosit.xvrl.model.XvrlDetection;
import org.kosit.xvrl.model.XvrlReport;
import org.kosit.xvrl.model.XvrlReports;
import org.kosit.xvrl.model.XvrlSupplemental;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import jakarta.xml.bind.JAXBException;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

/**
 * Tests the internal functions used to create a secure resolver
 * 
 * @author Andreas Penski
 */
public class BaseResolverConfigurationTest {

    public static final String NOT_EXISTING_SCHEME = "not-existing-scheme";

    public static void main(final String[] args) throws JAXBException, SaxonApiException {
        final XdmNode node = TestHelper.load(TestHelper.Simple.SIMPLE_VALID);
        final XvrlSupplemental s = XvrlSupplemental.builder().id("bla")
                .addContent(NodeOverNodeInfo.wrap(node.getUnderlyingNode()).getOwnerDocument().getDocumentElement()).build();
        final XvrlReports report = XvrlReports.builder()
                .addReport(XvrlReport.builder().addDetection(XvrlDetection.builder().addSupplemental(s))).build();
        final XvrlSerializer ser = new XvrlSerializer(ProcessorProvider.getProcessor());
        final XdmNode result = ser.marshalToXdmNode(report);
        final Serializer serialize = ProcessorProvider.getProcessor().newSerializer();
        final String string = serialize.serializeNodeToString(result);
        System.out.println(string);
    }

    @Test
    public void testIgnoreUnsupportedProperty() throws SAXNotRecognizedException, SAXNotSupportedException {
        final SchemaFactory sf = mock(SchemaFactory.class);
        final TestResolvingStrategy s = new TestResolvingStrategy();
        doThrow(new SAXNotRecognizedException("not supported")).when(sf).setProperty(any(), any());
        s.setInternalProperty(sf, true);
    }

    @Test
    public void testFailOnUnsupportedProperty() throws SAXNotRecognizedException, SAXNotSupportedException {
        final SchemaFactory sf = mock(SchemaFactory.class);
        final TestResolvingStrategy s = new TestResolvingStrategy();
        doThrow(new SAXNotRecognizedException("not supported")).when(sf).setProperty(any(), any());
        assertThrows(IllegalStateException.class, () -> s.setInternalProperty(sf, false));
    }

    @Test
    public void testSimpleSuccess() throws SAXNotRecognizedException, SAXNotSupportedException {
        final SchemaFactory sf = mock(SchemaFactory.class);
        final TestResolvingStrategy s = new TestResolvingStrategy();
        s.setInternalProperty(sf, true);
        s.setInternalProperty(sf, false);
        verify(sf, times(2)).setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, BaseResolverConfigurationTest.NOT_EXISTING_SCHEME);
    }

    private class TestResolvingStrategy extends StrictRelativeResolvingStrategy {

        void setInternalProperty(final SchemaFactory factory, final boolean lenient) {
            allowExternalSchema(factory, lenient, NOT_EXISTING_SCHEME);
        }

        public TestResolvingStrategy() {
        }
    }
}
