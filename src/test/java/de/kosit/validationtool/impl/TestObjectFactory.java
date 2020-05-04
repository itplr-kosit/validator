package de.kosit.validationtool.impl;

import de.kosit.validationtool.impl.xml.StrictLocalResolvingStrategy;

import net.sf.saxon.s9api.Processor;

/**
 * @author Andreas Penski
 */
public class TestObjectFactory {

    private static Processor processor;

    public static Processor createProcessor() {
        if (processor == null) {
            processor = new StrictLocalResolvingStrategy().getProcessor();
        }
        return processor;
    }
}
