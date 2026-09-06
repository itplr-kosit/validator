package org.kosit.validator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.xml.transform.stream.StreamSource;

import org.conformatron.api.model.source.CTReadResource;
import org.jspecify.annotations.NonNull;
import org.kosit.cvr.source.ReadResource;
import org.kosit.cvr.source.Resource;
import org.kosit.schematron.ContentRepository;
import org.kosit.schematron.resolve.ResolvingConfigurationStrategy;
import org.kosit.schematron.resolve.StrictRelativeResolvingStrategy;
import org.kosit.schematron.saxon.ProcessorProvider;
import org.kosit.validator.testdata.TestData;
import org.kosit.validator.testdata.TestResources;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

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

    public static XdmNode load(final URI url) {
        try {
            return load(url.toURL());
        } catch (final MalformedURLException e) {
            throw new IllegalStateException("Error loading the XML file", e);
        }
    }

    /**
     * Loads an XML document from the given URL.
     *
     * @param url the url to load
     * @return a result object containing the document
     */
    public static XdmNode load(final URL url) {
        try ( final InputStream input = url.openStream() ) {
            return getTestProcessor().newDocumentBuilder().build(new StreamSource(input));
        } catch (final SaxonApiException | IOException e) {
            throw new IllegalStateException("Error loading the XML file", e);
        }
    }

}
