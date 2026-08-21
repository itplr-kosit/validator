package org.kosit.validator.impl.input;

import javax.xml.transform.Source;

import org.kosit.validator.api.VInput;

import net.sf.saxon.s9api.XdmNode;

/**
 * An {@link VInput} implementation holding saxon's {@link XdmNode} object.
 * 
 * @author Andreas Penski
 */
public class XdmNodeVInput implements VInput {

    private final XdmNode node;

    private final String name;

    private final String digestAlgorithm;

    private final byte[] hashCode;

    @Override
    public Source getSource() {
        // usually not necessary to be called.
        return this.node.getUnderlyingNode();
    }

    public XdmNodeVInput(final XdmNode node, final String name, final String digestAlgorithm, final byte[] hashCode) {
        this.node = node;
        this.name = name;
        this.digestAlgorithm = digestAlgorithm;
        this.hashCode = hashCode;
    }

    public XdmNode getNode() {
        return this.node;
    }

    public String getName() {
        return this.name;
    }

    public String getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    public byte[] getHashCode() {
        return this.hashCode;
    }
}
