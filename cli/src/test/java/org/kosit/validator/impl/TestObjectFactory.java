package org.kosit.validator.impl;

import org.kosit.xvrl.impl.XvrlConversionService;

import net.sf.saxon.s9api.Processor;

/**
 * @author Andreas Penski
 */
public class TestObjectFactory {

    private static Processor processor;

    private static XvrlConversionService conversionService;

    public static Processor createProcessor() {
        if (processor == null) {
            processor = TestHelper.getTestProcessor();
        }
        return processor;
    }

    public static XvrlConversionService createXvrlConversionService() {
        if (conversionService == null) {
            conversionService = new XvrlConversionService();
        }
        return conversionService;
    }
}
