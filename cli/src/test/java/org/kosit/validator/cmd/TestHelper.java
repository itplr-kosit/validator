package org.kosit.validator.cmd;

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
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kost.validator.api.saxon.ProcessorProvider;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class TestHelper {

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
