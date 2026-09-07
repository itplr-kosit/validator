package org.kost.validator.api.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.conformatron.api.model.source.CTReadResource;
import org.conformatron.api.model.source.CTResource;
import org.junit.jupiter.api.Test;
import org.kosit.conformatron.detection.DetectionList;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.w3c.dom.Document;

import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

public class XmlParserTest {

    /** A resource that was read once but cannot be read again. */
    private static final class UnreadableResource implements CTReadResource {

        private final CTResource source;

        private UnreadableResource(final String name) {
            this.source = Resource.utf8(name, "<root/>");
        }

        public CTResource getSource() {
            return this.source;
        }

        public InputStream getSourceStream() throws IOException {
            throw new IOException("Stream is gone");
        }

        public String getHashAlgorithmName() {
            return ReadResource.HASH_ALGORITHM_NAME;
        }

        public byte[] getHashBytes() {
            return new byte[0];
        }
    }

    private static final String WELLFORMED = "<?xml version=\"1.0\"?><doc><child>content</child></doc>";

    private static final String NOT_WELLFORMED = "<?xml version=\"1.0\"?><doc><child>content</doc>";

    private static CTReadResource read(final String name, final String content) throws IOException {
        return ReadResource.inMemory(Resource.utf8(name, content));
    }

    @Test
    public void parseXmlDomWellformed() throws IOException {
        final AtomicReference<Document> doc = new AtomicReference<>();
        final DetectionList detections = XmlParser.parseXmlDom(read("test.xml", WELLFORMED), doc::set);

        assertThat(detections.isEmpty()).isTrue();
        assertThat(doc.get()).isNotNull();
        assertThat(doc.get().getDocumentElement().getLocalName()).isEqualTo("doc");
    }

    @Test
    public void parseXmlDomNotWellformed() throws IOException {
        final AtomicReference<Document> doc = new AtomicReference<>();
        final DetectionList detections = XmlParser.parseXmlDom(read("broken.xml", NOT_WELLFORMED), doc::set);

        // the consumer is only called on success
        assertThat(doc.get()).isNull();
        assertThat(detections.containsAtLeastOneError()).isTrue();
        // the exception thrown out of the error handler is not collected a second time
        assertThat(detections.getCount()).isEqualTo(1);
        assertThat(detections.getAll()).allSatisfy(detection -> {
            assertThat(detection.getCode()).isEqualTo(XmlDetection.CODE_NOT_WELLFORMED);
            assertThat(detection.getLocation().getResourceId()).isEqualTo("broken.xml");
            assertThat(detection.getLocation().hasLineNumber()).isTrue();
        });
    }

    @Test
    public void parseXmlDomReadError() {
        final AtomicReference<Document> doc = new AtomicReference<>();
        final DetectionList detections = XmlParser.parseXmlDom(new UnreadableResource("unreadable.xml"), doc::set);

        assertThat(doc.get()).isNull();
        assertThat(detections.getCount()).isEqualTo(1);
        assertThat(detections.getAll().get(0).getCode()).isEqualTo(XmlDetection.CODE_SOURCE_READ_ERROR);
        assertThat(detections.getAll().get(0).getLocation().getResourceId()).isEqualTo("unreadable.xml");
        assertThat(detections.getAll().get(0).getLinkedException()).isInstanceOf(IOException.class);
    }

    @Test
    public void parseXmlDomNullParameters() throws IOException {
        final CTReadResource input = read("test.xml", WELLFORMED);
        assertThatThrownBy(() -> XmlParser.parseXmlDom(null, _ -> {
        })).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> XmlParser.parseXmlDom(input, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void parseXdmNodeWellformed() throws IOException {
        final AtomicReference<XdmNode> node = new AtomicReference<>();
        final DetectionList detections = XmlParser.parseXdmNode(read("test.xml", WELLFORMED), node::set);

        assertThat(detections.isEmpty()).isTrue();
        assertThat(node.get()).isNotNull();
        assertThat(node.get().getNodeKind()).isEqualTo(XdmNodeKind.DOCUMENT);
        assertThat(node.get().getOutermostElement().getNodeName().getLocalName()).isEqualTo("doc");
    }

    @Test
    public void parseXdmNodeNotWellformed() throws IOException {
        final AtomicReference<XdmNode> node = new AtomicReference<>();
        final DetectionList detections = XmlParser.parseXdmNode(read("broken.xml", NOT_WELLFORMED), node::set);

        assertThat(node.get()).isNull();
        assertThat(detections.containsAtLeastOneError()).isTrue();
        assertThat(detections.getAll()).anySatisfy(detection -> {
            assertThat(detection.getCode()).isEqualTo(XmlDetection.CODE_NOT_WELLFORMED);
            assertThat(detection.getLocation().getResourceId()).isEqualTo("broken.xml");
            assertThat(detection.getLocation().hasLineNumber()).isTrue();
        });
    }

    @Test
    public void parseXdmNodeReadError() {
        final AtomicReference<XdmNode> node = new AtomicReference<>();
        final DetectionList detections = XmlParser.parseXdmNode(new UnreadableResource("unreadable.xml"), node::set);

        assertThat(node.get()).isNull();
        assertThat(detections.containsAtLeastOneError()).isTrue();
        assertThat(detections.getAll()).anySatisfy(detection -> {
            assertThat(detection.getCode()).isEqualTo(XmlDetection.CODE_SOURCE_READ_ERROR);
            assertThat(detection.getLocation().getResourceId()).isEqualTo("unreadable.xml");
        });
    }

    @Test
    public void parseXdmNodeNullParameters() throws IOException {
        final CTReadResource input = read("test.xml", WELLFORMED);
        assertThatThrownBy(() -> XmlParser.parseXdmNode(null, _ -> {
        })).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> XmlParser.parseXdmNode(input, null)).isInstanceOf(NullPointerException.class);
    }
}
