package org.kosit.validator.impl.xvrl;

import org.kosit.xvrl.model.XVRLSupplementalType;
import org.w3c.dom.Node;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.XdmNode;

public class XvrlSupplementalBuilder {

    private final XVRLSupplementalType sup = new XVRLSupplementalType();

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

    public XVRLSupplementalType build() {
        return this.sup;
    }
}