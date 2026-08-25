package org.kosit.validator.api;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.kosit.validator.impl.input.ByteArrayVInput;
import org.kosit.validator.impl.input.ResourceVInput;
import org.kosit.validator.impl.input.SourceVInput;
import org.kosit.validator.impl.input.StreamHelper;
import org.kosit.validator.impl.input.XdmNodeVInput;

import net.sf.saxon.s9api.XdmNode;

/**
 * Service for reading the test object into memory. While reading a checksum is computed and carried along with the
 * result.
 *
 * @author Andreas Penski
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class VInputFactory {

    @Deprecated(since = "2.0.0", forRemoval = true)
    static final String DEFAULT_ALGORITHM = "SHA-256";

    /**
     * Pseudo hashcode algorithm name, which indicates, that the hashcode of the {@link VInput} is actually the name.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    static final String PSEUDO_NAME_ALGORITHM = "NAME";

    private static final String MESSAGE_OPEN_STREAM_ERROR = "Can not open stream from";

    private final String algorithm;

    /**
     * Reads a test document from the given path. The default checksum algorithm is used to compute the checksum.
     *
     * @param path the test document
     * @return a test input object
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ResourceVInput read(final Path path) {
        return read(path, DEFAULT_ALGORITHM);
    }

    /**
     * Reads a test document from the given URL. A defined algorithm is used to compute the checksum.
     *
     * @param path the test document
     * @param digestAlgorithm the checksum algorithm
     * @return a test input object
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ResourceVInput read(final Path path, final String digestAlgorithm) {
        Objects.requireNonNull(path);
        return read(path.toUri(), digestAlgorithm);
    }

    /**
     * Reads a test document from the given file. The default checksum algorithm is used to compute the checksum.
     *
     * @param file the test document
     * @return a test input object
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ResourceVInput read(final File file) {
        return read(file, DEFAULT_ALGORITHM);
    }

    /**
     * Reads a test document from the given URI. The default checksum algorithm is used to compute the checksum.
     *
     * @param uri URI of the test document
     * @return a test input object
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ResourceVInput read(final URI uri) {
        return read(uri, DEFAULT_ALGORITHM);
    }

    /**
     * Reads a test document from the given URL. A defined algorithm is used to compute the checksum.
     *
     * @param uri URI of the test document
     * @param digestAlgorithm the checksum algorithm
     * @return a test input object
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ResourceVInput read(final URI uri, final String digestAlgorithm) {
        try {
            return read(uri.toURL(), digestAlgorithm);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("URL invalid or protocol not supported: " + uri, e);
        }
    }

    /**
     * Reads a test document from the given URL. The default checksum algorithm is used to compute the checksum.
     *
     * @param url URL of the test document
     * @return a test input object
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ResourceVInput read(final URL url) {
        return read(url, DEFAULT_ALGORITHM);
    }

    /**
     * Reads a test document from the given URL. A defined algorithm is used to compute the checksum.
     *
     * @param url URL of the test document
     * @param digestAlgorithm the checksum algorithm
     * @return a test input object
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ResourceVInput read(final URL url, final String digestAlgorithm) {
        Objects.requireNonNull(url);
        checkNotEmpty(url.getFile());
        try {
            final URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
        } catch (final IOException e) {
            throw new IllegalArgumentException(MESSAGE_OPEN_STREAM_ERROR + url, e);
        }
        return new ResourceVInput(url, url.getFile(), digestAlgorithm);
    }

    /**
     * Reads a test document from a {@link Source}. Note: computing the hashcode is only supported for
     * {@link StreamSource}. You can not directly use other {@link Source Sources}. You need to supply the hashcode for
     * identification then.
     * 
     * @param source source
     * @return an {@link SourceVInput}
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static SourceVInput read(final Source source) {
        if (source instanceof StreamSource) {
            return read(source, source.getSystemId(), DEFAULT_ALGORITHM);
        }
        final String name = UUID.randomUUID().toString();
        return read(source, name, PSEUDO_NAME_ALGORITHM, name.getBytes());
    }

    /**
     * Reads a test document from a {@link Source} using a specified digest algorithm.
     * 
     * Note: computing the hashcode is only supported for {@link StreamSource}. You can not directly use other
     * {@link Source Sources}. You need to supply the hashcode for identification then.
     *
     * @param source source
     * @param name the digest algorithm
     * @return an {@link VInput}
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static SourceVInput read(final Source source, final String name) {
        checkNotEmpty(name);
        return read(source, name, PSEUDO_NAME_ALGORITHM, name.getBytes());
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public static SourceVInput read(final Source source, final String name, final String digestAlgorithm) {
        return read(source, name, digestAlgorithm, null);
    }

    /**
     * Reads a test document from a {@link Source} using a specified digest algorithm.
     *
     * @param source source
     * @param digestAlgorithm the digest algorithm
     * @return an {@link VInput}
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static SourceVInput read(final Source source, final String digestAlgorithm, final byte[] hashcode) {
        Objects.requireNonNull(source);
        return read(source, source.getSystemId(), digestAlgorithm, hashcode);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public static SourceVInput read(final Source source, final String name, final String digestAlgorithm, final byte[] hashcode) {
        Objects.requireNonNull(source);
        return new SourceVInput(source, name, digestAlgorithm, hashcode);
    }

    /**
     * Reads a test document from the given URL. A defined algorithm is used to compute the checksum.
     *
     * @param file the test document
     * @param digestAlgorithm the checksum algorithm
     * @return a test input object
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ResourceVInput read(final File file, final String digestAlgorithm) {
        Objects.requireNonNull(file);
        try {
            return read(file.toURI().toURL(), digestAlgorithm);
        } catch (final IOException e) {
            throw new IllegalArgumentException(MESSAGE_OPEN_STREAM_ERROR + file, e);
        }
    }

    /**
     * Reads a test document from the given byte sequence. A defined algorithm is used to compute the checksum.
     *
     * @param input URL of the test document
     * @return a test input object
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ByteArrayVInput read(final byte[] input, final String name) {
        Objects.requireNonNull(input);
        return read(input, name, DEFAULT_ALGORITHM);
    }

    /**
     * Reads a test document from the given byte sequence. A defined algorithm is used to compute the checksum.
     *
     * @param input URL of the test document
     * @param digestAlgorithm the checksum algorithm
     * @return a test input object
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ByteArrayVInput read(final byte[] input, final String name, final String digestAlgorithm) {
        Objects.requireNonNull(input);
        checkNotEmpty(name);
        return new ByteArrayVInput(input, name, digestAlgorithm);
    }

    private static void checkNotEmpty(final String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Input name can not be null");
        }
    }

    /**
     * Reads a test document from the given {@link InputStream}.
     *
     * @param inputStream the {@link InputStream}
     * @param name the name/identifier of the test document
     * @return a test document in the read form
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static SourceVInput read(final InputStream inputStream, final String name) {
        return read(inputStream, name, DEFAULT_ALGORITHM);
    }

    /**
     * Reads a test document from the given {@link InputStream}.
     *
     * @param inputStream the {@link InputStream}
     * @param name the name/identifier of the test document
     * @param digestAlgorithm the checksum algorithm
     * @return a test document in the read form
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static SourceVInput read(final InputStream inputStream, final String name, final String digestAlgorithm) {
        Objects.requireNonNull(inputStream);
        return read(new StreamSource(inputStream, name), name, digestAlgorithm);
    }

    /**
     * Reads a saxon {@link XdmNode} with a given name. Hashcode identification is based on the name of the supplied
     * input. Now real hashcode is computed.
     * 
     * @param node the node to read
     * @param name the name of the {@link VInput}
     * @return an {@link XdmNodeVInput} to validate
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static XdmNodeVInput read(final XdmNode node, final String name) {
        Objects.requireNonNull(node);
        return new XdmNodeVInput(node, name, PSEUDO_NAME_ALGORITHM, name.getBytes());
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    VInputFactory() {
        this(null);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    VInputFactory(final String specifiedAlgorithm) {
        this.algorithm = isNotEmpty(specifiedAlgorithm) ? specifiedAlgorithm : DEFAULT_ALGORITHM;
        // check validity
        StreamHelper.createDigest(this.algorithm);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public String getAlgorithm() {
        return this.algorithm;
    }
}
