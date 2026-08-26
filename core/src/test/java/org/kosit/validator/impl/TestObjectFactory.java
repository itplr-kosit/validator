package org.kosit.validator.impl;

import net.sf.saxon.s9api.Processor;

/**
 * @author Andreas Penski
 */
public class TestObjectFactory {

    private static Processor processor;

    public static Processor getProcessor() {
        if (processor == null) {
            processor = TestHelper.getTestProcessor();
        }
        return processor;
    }
}
