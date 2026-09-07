package org.kost.validator.api.saxon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

import org.conformatron.api.model.source.CTValidationSource;
import org.conformatron.api.model.validation.CTSyntax;
import org.junit.jupiter.api.Test;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.conformatron.source.ValidationSource;
import org.w3c.dom.Document;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class XdmNodeValidationSourceTest {

    private static final String XML = "<doc><child>content</child></doc>";

    private static CTValidationSource createSource() throws IOException {
        return ValidationSource.completeXml(ReadResource.inMemory(Resource.utf8("test.xml", XML)));
    }

    private static XdmNode createNode() throws SaxonApiException {
        return ProcessorProvider.getProcessor().newDocumentBuilder().build(new StreamSource(new StringReader(XML)));
    }

    @Test
    public void createFromDocumentNode() throws Exception {
        final CTValidationSource source = createSource();
        final XdmNode node = createNode();
        final XdmNodeValidationSource vs = new XdmNodeValidationSource(source, node);

        assertThat(vs.getSource()).isSameAs(source);
        assertThat(vs.getSource().getName()).isEqualTo("test.xml");
        assertThat(vs.getSource().getDetectedSyntax()).isEqualTo(CTSyntax.XML);
        assertThat(vs.isParsed()).isTrue();
    }

    @Test
    public void getParsedContentIsTheSaxonNode() throws Exception {
        final XdmNode node = createNode();
        final XdmNodeValidationSource vs = new XdmNodeValidationSource(createSource(), node);

        assertThat(vs.getParsedContent()).isSameAs(node);
    }

    @Test
    public void getAsDomIsAViewOnTheSaxonTree() throws Exception {
        final XdmNodeValidationSource vs = new XdmNodeValidationSource(createSource(), createNode());

        final Document dom = vs.getAsDom();
        assertThat(dom).isNotNull();
        assertThat(dom.getDocumentElement().getLocalName()).isEqualTo("doc");
        assertThat(dom.getDocumentElement().getFirstChild().getLocalName()).isEqualTo("child");
        assertThat(dom.getDocumentElement().getTextContent()).isEqualTo("content");
    }

    @Test
    public void getAsDomStartsAtTheRootNode() throws Exception {
        // an element within the document leads to the same DOM view, because the root node is used
        final XdmNode element = createNode().getOutermostElement();
        final XdmNodeValidationSource vs = new XdmNodeValidationSource(createSource(), element);

        assertThat(vs.getAsDom().getDocumentElement().getLocalName()).isEqualTo("doc");
    }

    @Test
    public void getAsDomFailsWithoutDocumentRoot() throws Exception {
        // an element constructed in XQuery is the root of its own tree - there is no document node
        final XdmNode parentless = (XdmNode) ProcessorProvider.getProcessor().newXQueryCompiler().compile("<constructed/>").load()
                .evaluateSingle();
        final XdmNodeValidationSource vs = new XdmNodeValidationSource(createSource(), parentless);

        assertThatThrownBy(vs::getAsDom).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Underlying Saxon tree has no document root node");
    }

    @Test
    public void nullParameters() throws Exception {
        final CTValidationSource source = createSource();
        final XdmNode node = createNode();
        assertThatThrownBy(() -> new XdmNodeValidationSource(null, node)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new XdmNodeValidationSource(source, null)).isInstanceOf(NullPointerException.class);
    }
}
