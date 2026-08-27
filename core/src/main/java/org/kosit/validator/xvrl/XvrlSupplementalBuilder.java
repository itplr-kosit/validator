package org.kosit.validator.xvrl;

import org.kosit.xvrl.model.XvrlSupplementalType;
import org.w3c.dom.Node;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.XdmNode;

public class XvrlSupplementalBuilder {

    private final XvrlSupplementalType sup = new XvrlSupplementalType();

    public static XvrlSupplementalBuilder builder() {
        return new XvrlSupplementalBuilder();
    }

    private XvrlSupplementalBuilder() {
    }

    public XvrlSupplementalBuilder id(final String id) {
        this.sup.setId(id);
        return this;
    }

    public XvrlSupplementalBuilder addContent(final XdmNode node) {
        if (node != null) {
            addContent(NodeOverNodeInfo.wrap(node.getUnderlyingNode()).getOwnerDocument().getDocumentElement());
        }
        return this;
    }

    public XvrlSupplementalBuilder addContent(final Node node) {
        if (node != null) {
            this.sup.getContent().add(node);
        }
        return this;
    }

    public XvrlSupplementalType build() {
        return this.sup;
    }
}