package org.kosit.validator.xvrl;

import org.kosit.xvrl.model.XvrlSupplemental;
import org.w3c.dom.Node;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.XdmNode;

public class XvrlSupplementalBuilder {

    private final XvrlSupplemental.Builder sup = XvrlSupplemental.builder();

    public static XvrlSupplementalBuilder builder() {
        return new XvrlSupplementalBuilder();
    }

    private XvrlSupplementalBuilder() {
    }

    public XvrlSupplementalBuilder id(final String id) {
        this.sup.id(id);
        return this;
    }

    public XvrlSupplementalBuilder addContent(final XdmNode node) {
        if (node != null) {
            addContent(NodeOverNodeInfo.wrap(node.getUnderlyingNode()).getOwnerDocument().getDocumentElement());
        }
        return this;
    }

    public XvrlSupplementalBuilder addContent(final Node node) {
        this.sup.addContent(node);
        return this;
    }

    public XvrlSupplemental build() {
        return this.sup.build();
    }
}
