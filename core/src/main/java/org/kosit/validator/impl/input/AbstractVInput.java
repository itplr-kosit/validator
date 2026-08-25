package org.kosit.validator.impl.input;

import static org.kosit.validator.impl.input.StreamHelper.drain;

import java.io.IOException;
import java.io.InputStream;

import org.kosit.validator.api.VInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all {@link VInput Inputs}.
 * 
 * @author Andreas Penski
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public abstract class AbstractVInput implements VInput, LazyReadInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractVInput.class);

    private byte[] hashCode;

    private long length;

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public byte[] getHashCode() {
        if (this.hashCode == null) {
            LOGGER.warn("Extra calculating hashcode. This is in-efficient in most cases");
            computeHashcode();
        }
        return this.hashCode;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    protected void computeHashcode() {
        try {
            drain(this);
        } catch (final IOException e) {
            LOGGER.error("Error extra computing hashcode", e);
        }
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    protected InputStream wrap(final InputStream stream) {
        InputStream result = stream;
        if (!isHashcodeComputed()) {
            result = StreamHelper.wrapDigesting(this, result, getDigestAlgorithm());
        }
        if (getLength() == 0) {
            result = StreamHelper.wrapCount(this, result);
        }
        return result;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public boolean isHashcodeComputed() {
        return this.hashCode != null;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public void setHashCode(final byte[] digest) {
        this.hashCode = digest;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public boolean supportsMultipleReads() {
        return true;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public long getLength() {
        return this.length;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setLength(final long length) {
        this.length = length;
    }
}
