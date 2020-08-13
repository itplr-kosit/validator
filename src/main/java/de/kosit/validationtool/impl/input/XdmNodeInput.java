package de.kosit.validationtool.impl.input;

import javax.xml.transform.Source;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import de.kosit.validationtool.api.Input;

import net.sf.saxon.s9api.XdmNode;

/**
 * An {@link Input} implementation holding saxon's {@link XdmNode} object.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Getter
public class XdmNodeInput implements Input {

    private final XdmNode node;

    private final String name;

    private final String digestAlgorithm;

    private final byte[] hashCode;

    @Override
    public Source getSource() {
        // usually not neccessary to be called.
        return this.node.getUnderlyingNode();
    }
}
