package org.kosit.validator.impl.input;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

/**
 * Classical in-memory {@link VInput}. It is not memory efficient to read the whole file into memory prio validating.
 * Consider using the {@link ResourceVInput}.
 * 
 * @author Andreas Penski
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class ByteArrayVInput extends AbstractVInput {

    private final byte[] content;

    private final String name;

    private final String digestAlgorithm;

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public long getLength() {
        return this.content != null ? this.content.length : 0;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public StreamSource getSource() {
        final InputStream stream = wrap(new ByteArrayInputStream(this.content));
        return new StreamSource(stream, getName());
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public byte[] getContent() {
        return this.content;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public String getName() {
        return this.name;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public String getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public ByteArrayVInput(final byte[] content, final String name, final String digestAlgorithm) {
        this.content = content;
        this.name = name;
        this.digestAlgorithm = digestAlgorithm;
    }
}
