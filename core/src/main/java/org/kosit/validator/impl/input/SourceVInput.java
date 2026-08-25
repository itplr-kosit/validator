package org.kosit.validator.impl.input;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.util.JAXBSource;
import net.sf.saxon.om.TreeInfo;

/**
 * A validator {@link VInput} based on a {@link Source}.
 * <p>
 * Note: The various implementations of {@link Source} vary whether they can be read twice or not. This implementation
 * tries to handle this with respect document identification (hashcode).
 * 
 * This class is known to work with:
 * <ul>
 * <li>{@link StreamSource} - both {@link java.io.InputStream} based and {@link java.io.Reader} based</li>
 * <li>{@link javax.xml.transform.dom.DOMSource}</li>
 * <li>{@link jakarta.xml.bind.util.JAXBSource}</li>
 * <li>{@link TreeInfo}</li>
 * </ul>
 * 
 * Other {@link Source Sources} may work as well, please try and let us know.
 * 
 * @author Andreas Penski
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class SourceVInput extends AbstractVInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceVInput.class);

    private final Source source;

    private final String name;

    private final String digestAlgorithm;

    @Deprecated(since = "2.0.0", forRemoval = true)
    public SourceVInput(final StreamSource source, final String name, final String digestAlgorithm) {
        this(source, name, digestAlgorithm, null);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public SourceVInput(final Source source, final String name, final String digestAlgorithm, final byte[] hashCode) {
        this.source = source;
        this.name = name;
        this.digestAlgorithm = digestAlgorithm;
        setHashCode(hashCode);
        validate();
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public String getName() {
        return defaultIfBlank(this.name, this.source.getClass().getSimpleName());
    }

    private void validate() {
        if (!isHashcodeComputed() && !isHashcodeComputationSupported()) {
            throw new IllegalStateException("Unsupported source. Only StreamSource supported yet");
        }
        if (!isHashcodeComputed() && ((StreamSource) this.source).getInputStream() == null) {
            LOGGER.warn("No hashcode supplied, will wrap the reader using system default charset");
        }
        if (!(isTreeInfo() || isDomSource() || isStreamSource() || isJaxbSource())) {
            LOGGER.warn("No known to be working Source implementation provided.");
        }
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public Source getSource() throws IOException {
        if (isConsumed()) {
            throw new IllegalStateException("A SourceInput can only read once");
        }
        return isHashcodeComputed() ? this.source : wrappedSource();
    }

    private boolean isHashcodeComputationSupported() {
        return isStreamSource();
    }

    private boolean isConsumed() {
        if (isStreamSource()) {
            final StreamSource ss = (StreamSource) this.source;
            try {
                return (ss.getInputStream() != null && ss.getInputStream().available() == 0)
                        || (ss.getReader() != null && !ss.getReader().ready());
            } catch (final IOException e) {
                LOGGER.error("Error checking consumed state", e);
                return true;
            }
        }
        return false;
    }

    private boolean isStreamSource() {
        return this.source instanceof StreamSource;
    }

    private boolean isDomSource() {
        return this.source instanceof DOMSource;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public boolean isTreeInfo() {
        return this.source instanceof TreeInfo;
    }

    private boolean isJaxbSource() {
        return this.source instanceof JAXBSource;
    }

    private Source wrappedSource() {
        Source result = this.source;
        if (isStreamSource()) {
            final StreamSource ss = (StreamSource) this.source;
            if (ss.getInputStream() != null) {
                result = new StreamSource(wrap(ss.getInputStream()), this.source.getSystemId());
            } else if (ss.getReader() != null) {
                result = new StreamSource(wrap(new ReaderInputStream(ss.getReader(), Charset.defaultCharset())), this.source.getSystemId());
            }
        }
        return result;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public boolean supportsMultipleReads() {
        return isDomSource() || isTreeInfo();
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public String getDigestAlgorithm() {
        return this.digestAlgorithm;
    }
}
