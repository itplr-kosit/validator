package org.kosit.validator.impl.saxon;

import org.w3c.dom.Node;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.XdmNode;

public class SaxonHelper {

    public static Node toNode(final XdmNode node) {
        return NodeOverNodeInfo.wrap(node.getUnderlyingNode()).getOwnerDocument().getDocumentElement();
    }

    private SaxonHelper() {
    }
}
