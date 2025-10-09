/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kosit.validationtool.impl.xml;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.xml.XMLConstants;
import jakarta.xml.bind.JAXBException;
import javax.xml.validation.SchemaFactory;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import lombok.RequiredArgsConstructor;

import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.tasks.XvrlSerializer;
import de.kosit.validationtool.model.xvrl.Supplemental;
import de.kosit.validationtool.model.xvrl.XVRLDetection;
import de.kosit.validationtool.model.xvrl.XVRLReport;
import de.kosit.validationtool.model.xvrl.XVRLReportSummary;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

/**
 * 
 * Tests the internal functions used to create a secure resolver
 * 
 * @author Andreas Penski
 */
public class BaseResolverConfigurationTest {

    public static final String NOT_EXISTING_SCHEME = "not-existing-scheme";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public static void main(final String[] args) throws JAXBException, SaxonApiException {
        final XVRLReportSummary report = new XVRLReportSummary();
        final XVRLReport r = new XVRLReport();
        final XVRLDetection d = new XVRLDetection();
        final Supplemental s = new Supplemental();
        s.setId("bla");
        final XdmNode node = Helper.load(Helper.Simple.SIMPLE_VALID);
        s.getContent().add(NodeOverNodeInfo.wrap(node.getUnderlyingNode()).getOwnerDocument().getDocumentElement());
        d.getSupplementals().add(s);
        r.getDetection().add(d);
        report.getReports().add(r);

        final XvrlSerializer ser = new XvrlSerializer(new ConversionService(), ProcessorProvider.getProcessor());

        final XdmNode result = ser.serialize(report);
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
        verify(sf, times(2)).setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, BaseResolverConfigurationTest.NOT_EXISTING_SCHEME);
    }

    @RequiredArgsConstructor
    private class TestResolvingStrategy extends StrictRelativeResolvingStrategy {

        void setInternalProperty(final SchemaFactory factory, final boolean lenient) {
            allowExternalSchema(factory, lenient, NOT_EXISTING_SCHEME);
        }
    }

}
