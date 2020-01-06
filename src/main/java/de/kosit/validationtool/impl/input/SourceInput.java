package de.kosit.validationtool.impl.input;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.NotImplementedException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A validator {@link de.kosit.validationtool.api.Input} based an on a {@link Source}.
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
        if (!isSupported()) {
            throw new IllegalStateException("Unsupported source. Only StreamSource supported yet");
        }
        if (((StreamSource) this.source).getInputStream() == null && !isHashcodeComputed()) {
            log.warn("No hashcode supplied, will wrap the reader using system default charset");
        }
    }

    @Override
    public Source getSource() throws IOException {
        if (!isSupported()) {
            throw new IllegalStateException("Unsupported source. Only InputStream-based StreamSource supported yet");
        }
        if (isWrappingRequired()) {
            return wrap();
        }
        if (isConsumed()) {
            throw new IllegalStateException("A SourceInput can only read once");
        }
        return this.source;
    }

    private boolean isSupported() {
        return isStreamSource();
    }

    private boolean isConsumed() throws IOException {
        if (!isStreamSource()) {
            throw new NotImplementedException("Supports only StreamSource yet");
        }
        final StreamSource ss = (StreamSource) this.source;
        try {
            return (ss.getInputStream() != null && ss.getInputStream().available() == 0)
                    || (ss.getReader() != null && !ss.getReader().ready());
        } catch (final IOException e) {
            return true;
        }
    }

    private boolean isStreamSource() {
        return this.source instanceof StreamSource;
    }

    private Source wrap() {
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
