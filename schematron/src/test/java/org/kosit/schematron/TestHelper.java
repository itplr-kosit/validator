package org.kosit.schematron;

import org.kosit.schematron.resolve.ResolvingConfigurationStrategy;
import org.kosit.schematron.resolve.StrictRelativeResolvingStrategy;
import org.kosit.schematron.saxon.ProcessorProvider;
import org.kosit.validator.testdata.TestData;
import org.kosit.validator.testdata.TestResources;

import net.sf.saxon.s9api.Processor;

/**
 * Helper for test artifacts.
 *
 * @author Andreas Penski
 */

public class TestHelper {

    public static final ContentRepository createContentRepository() {
        return new ContentRepository(TestHelper.getTestProcessor(), getTestResolvingStrategy(), TestResources.Simple.REPOSITORY_URI);
    }

    /**
     * Part of the shared test data lives inside an archive, either because the build packaged this module or because
     * {@link TestData#inArchive(String)} did. Resolving into an archive is off by default, and the tests are the ones
     * that explicitly allow it.
     *
     * @return the resolving strategy of the tests, never {@code null}
     */
    public static ResolvingConfigurationStrategy getTestResolvingStrategy() {
        return new StrictRelativeResolvingStrategy(true);
    }

    public static Processor getTestProcessor() {
        // is always the same at the moment
        return ProcessorProvider.getProcessor();
    }
}
