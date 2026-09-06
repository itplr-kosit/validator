package org.kosit.cvr.action;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;

import org.conformatron.api.model.source.CTReadResource;
import org.jspecify.annotations.NonNull;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.schematron.ContentRepository;
import org.kosit.schematron.resolve.ResolvingConfigurationStrategy;
import org.kosit.schematron.resolve.StrictRelativeResolvingStrategy;
import org.kosit.validator.testdata.TestData;
import org.kosit.validator.testdata.TestResources;
import org.kost.validator.api.saxon.ProcessorProvider;

import net.sf.saxon.s9api.Processor;

public final class TestHelper {

    public static ContentRepository createContentRepository() {
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

    public static @NonNull CTReadResource read(final @NonNull URI u) {
        try {
            return ReadResource.inMemory(Resource.of(u));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static @NonNull CTReadResource read(final @NonNull File f) {
        try {
            return ReadResource.inMemory(Resource.of(f));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TestHelper() {
    }
}
