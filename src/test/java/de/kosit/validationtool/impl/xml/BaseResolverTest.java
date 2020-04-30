package de.kosit.validationtool.impl.xml;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import lombok.RequiredArgsConstructor;

/**
 * 
 * Tests the internal functions used to create a secure resolver
 * 
 * @author Andreas Penski
 */
public class BaseResolverTest {

    @RequiredArgsConstructor
    private class TestResolvingStrategy extends StrictRelativeResolvingStrategy {

        void setInternalProperty(final SchemaFactory factory, final boolean lenient) {
            allowExternalSchema(factory, lenient, "quatsch");
        }
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testIgnoreUnsupportedProperty() throws SAXNotRecognizedException, SAXNotSupportedException {
        final SchemaFactory sf = mock(SchemaFactory.class);
        final TestResolvingStrategy s = new TestResolvingStrategy();
        doThrow(new SAXNotRecognizedException("not supported")).when(sf).setProperty(any(), any());
        s.setInternalProperty(sf, true);
    }

    @Test
    public void testFailOnUnsupportedProperty() throws SAXNotRecognizedException, SAXNotSupportedException {
        this.expectedException.expect(IllegalStateException.class);
        final SchemaFactory sf = mock(SchemaFactory.class);
        final TestResolvingStrategy s = new TestResolvingStrategy();
        doThrow(new SAXNotRecognizedException("not supported")).when(sf).setProperty(any(), any());
        s.setInternalProperty(sf, false);
    }

    @Test
    public void testSimpleSuccess() throws SAXNotRecognizedException, SAXNotSupportedException {
        final SchemaFactory sf = mock(SchemaFactory.class);
        final TestResolvingStrategy s = new TestResolvingStrategy();
        s.setInternalProperty(sf, true);
        s.setInternalProperty(sf, false);
        verify(sf, times(2)).setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "quatsch");
    }

}
