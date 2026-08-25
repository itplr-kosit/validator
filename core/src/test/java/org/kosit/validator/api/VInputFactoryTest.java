package org.kosit.validator.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kosit.validator.impl.TestHelper.Simple.SIMPLE_VALID;
import static org.kosit.validator.impl.input.StreamHelper.drain;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.TestObjectFactory;
import org.kosit.validator.impl.conformatron.source.ReadResource;
import org.kosit.validator.impl.input.SourceVInput;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.model.XMLSyntaxError;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.BuildingContentHandler;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

/**
 * Tests the hashcode service.
 *
 * @author Andreas Penski
 */
public class VInputFactoryTest {

    public static final String SOME_VALUE = "some value";

    @Test
    public void testDefaultDigestAlgorithm() {
        assertThat(new VInputFactory().getAlgorithm()).isEqualTo(VInputFactory.DEFAULT_ALGORITHM);
        assertThat(new VInputFactory("").getAlgorithm()).isEqualTo(VInputFactory.DEFAULT_ALGORITHM);
    }

    @Test
    public void testHashCodeGeneration() throws IOException {
        final byte[] s1 = drain(VInputFactory.read(Simple.SIMPLE_VALID.toURL())).getHashCode();
        final byte[] s2 = drain(VInputFactory.read(Simple.SIMPLE_VALID.toURL())).getHashCode();
        final byte[] s3 = drain(VInputFactory.read(Simple.SCHEMA_INVALID.toURL())).getHashCode();
        assertThat(s1).isNotEmpty().isEqualTo(s2);
        assertThat(s3).isNotEmpty();
        assertThat(s1).isNotEqualTo(s3);
    }

    @Test
    public void testWrongAlgorithm() {
        assertThrows(IllegalArgumentException.class, () -> new VInputFactory("unknown"));
    }

    @Test
    public void testNullInputURL() {
        assertThrows(NullPointerException.class, () -> VInputFactory.read((URL) null));
    }

    @Test
    public void testInputByte() {
        final VInput input = VInputFactory.read(SOME_VALUE.getBytes(StandardCharsets.UTF_8), SOME_VALUE);
        assertThat(input).isNotNull();
    }

    @Test
    public void testInputStream() {
        final VInput input = VInputFactory.read(new ByteArrayInputStream(SOME_VALUE.getBytes(StandardCharsets.UTF_8)), SOME_VALUE);
        assertThat(input).isNotNull();
    }

    @Test
    public void testNullStream() {
        assertThrows(NullPointerException.class, () -> VInputFactory.read((InputStream) null, SOME_VALUE));
    }

    @Test
    public void testInputFile() {
        final VInput input = VInputFactory.read(new File(Simple.SIMPLE_VALID));
        assertThat(input).isNotNull();
    }

    @Test
    public void testInputPath() {
        final VInput input = VInputFactory.read(Paths.get(Simple.SIMPLE_VALID));
        assertThat(input).isNotNull();
    }

    @Test
    public void testNullInput() {
        assertThrows(NullPointerException.class, () -> VInputFactory.read((byte[]) null, SOME_VALUE));
    }

    @Test
    public void testNullInputName() {
        assertThrows(IllegalArgumentException.class, () -> VInputFactory.read(SOME_VALUE.getBytes(StandardCharsets.UTF_8), null));
    }

    @Test
    public void testEmptyInputName() {
        assertThrows(IllegalArgumentException.class, () -> {
            final VInput input = VInputFactory.read(SOME_VALUE.getBytes(StandardCharsets.UTF_8), "");
            drain(input);
        });
    }

    @Test
    public void testSourceInput() throws IOException {
        try ( final InputStream s = Simple.SIMPLE_VALID.toURL().openStream() ) {
            final SourceVInput input = VInputFactory.read(new StreamSource(s));
            assertThat(input.getSource()).isNotNull();
            drain(input);
            assertThat(input.getHashCode()).isNotNull();
            assertThat(input.getLength()).isPositive();
            assertThrows(IllegalStateException.class, () -> input.getSource());
        }
    }

    @Test
    public void testSourceInputReader() throws IOException {
        try ( final InputStream s = Simple.SIMPLE_VALID.toURL().openStream();
              final InputStreamReader reader = new InputStreamReader(s) ) {
            final SourceVInput input = VInputFactory.read(new StreamSource(reader));
            assertThat(input.getSource()).isNotNull();
            drain(input);
            assertThat(input.getHashCode()).isNotNull();
            assertThat(input.getLength()).isPositive();
            assertThrows(IllegalStateException.class, () -> input.getSource());
        }
    }

    @Test
    public void testUnexistingInput() {
        assertThrows(IllegalArgumentException.class, () -> VInputFactory.read(Simple.NOT_EXISTING));
    }

    @Test
    @Disabled("DOMSource currently not supported for v2")
    public void testDomSource() throws SaxonApiException, SAXException, IOException {
        final DocumentBuilder builder = TestObjectFactory.createProcessor().newDocumentBuilder();

        final BuildingContentHandler handler = builder.newBuildingContentHandler();
        handler.startDocument();
        handler.startElement("http://some.ns", "mynode", "mynode", new AttributesImpl());
        final Document dom = NodeOverNodeInfo.wrap(handler.getDocumentNode().getUnderlyingNode()).getOwnerDocument();
        final VInput domVInput = VInputFactory.read(new DOMSource(dom), "MD5", "id".getBytes(StandardCharsets.UTF_8));
        assertThat(domVInput).isNotNull();
        assertThat(domVInput.getSource()).isNotNull();
        final Result<XdmNode, XMLSyntaxError> parsed = TestHelper.parseDocument(ReadResource.of(domVInput));
        assertThat(parsed.isValid()).isTrue();

        // read twice
        assertThat(TestHelper.parseDocument(ReadResource.of(domVInput)).getObject()).isNotNull();
    }

    @Test
    @Disabled("TinyDocumentImpl currently not supported for v2")
    public void testXdmNode() throws Exception {
        final XdmNode node = TestObjectFactory.createProcessor().newDocumentBuilder().build(new StreamSource(SIMPLE_VALID.toASCIIString()));
        final VInput nodeVInput = VInputFactory.read(node, "node test");
        assertThat(nodeVInput).isNotNull();
        assertThat(nodeVInput.getSource()).isNotNull();
        final Result<XdmNode, XMLSyntaxError> parsed = TestHelper.parseDocument(ReadResource.of(nodeVInput));
        assertThat(parsed.isValid()).isTrue();

        // read twice
        assertThat(TestHelper.parseDocument(ReadResource.of(nodeVInput)).getObject()).isNotNull();
    }

}
