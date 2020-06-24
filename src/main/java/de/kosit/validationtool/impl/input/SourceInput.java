package de.kosit.validationtool.impl.input;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.input.ReaderInputStream;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A validator {@link de.kosit.validationtool.api.Input} based on a {@link Source}. <br/>
 * <p>
 * Note: The various implementations of {@link Source} varies wether the can be read twice or no. This implementation
 * tries to handle this with respect document identification (hashcode).
 * 
 * This class is know to work with:
 * <ul>
 * <li>{@link StreamSource} - both {@link java.io.InputStream} based and {@link java.io.Reader} based</li>
 * <li>{@link javax.xml.transform.dom.DOMSource}</li>
 * <li>{@link javax.xml.bind.util.JAXBSource}</li>
 * </ul>
 * 
 * Other {@link Source Sources} may work as well, please try and let us know.
 * </p>
 * 
 * @author Andreas Penski
 */
@Getter
@Slf4j
public class SourceInput extends AbstractInput {

    private final Source source;

    private final String name;

    private final String digestAlgorithm;

    public SourceInput(final StreamSource source, final String name, final String digestAlgorithm) {
        this(source, name, digestAlgorithm, null);
    }

    public SourceInput(final Source source, final String name, final String digestAlgorithm, final byte[] hashCode) {
        this.source = source;
        this.name = name;
        this.digestAlgorithm = digestAlgorithm;
        setHashCode(hashCode);
        validate();
    }

    private void validate() {
        if (!isHashcodeComputed() && !isSupported()) {
            throw new IllegalStateException("Unsupported source. Only StreamSource supported yet");
        }
        if (!isHashcodeComputed() && ((StreamSource) this.source).getInputStream() == null) {
            log.warn("No hashcode supplied, will wrap the reader using system default charset");
        }
    }

    @Override
    public Source getSource() throws IOException {
        if (!isHashcodeComputed() && !isSupported()) {
            throw new IllegalStateException("Unsupported source. Only InputStream-based StreamSource supported yet");
        }
        if (isConsumed()) {
            throw new IllegalStateException("A SourceInput can only read once");
        }
        return isHashcodeComputed() ? this.source : wrappedSource();
    }

    private boolean isSupported() {
        return isStreamSource();
    }

    private boolean isConsumed() throws IOException {
        if (isStreamSource()) {

            final StreamSource ss = (StreamSource) this.source;
            try {
                return (ss.getInputStream() != null && ss.getInputStream().available() == 0)
                        || (ss.getReader() != null && !ss.getReader().ready());
            } catch (final IOException e) {
                log.error("Error checking consumed state", e);
                return true;
            }
        }
        return false;
    }

    private boolean isStreamSource() {
        return this.source instanceof StreamSource;
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

    private boolean isWrappingRequired() {
        return !isHashcodeComputed();
    }

    @Override
    public boolean supportsMultipleReads() {
        return false;
    }

}
