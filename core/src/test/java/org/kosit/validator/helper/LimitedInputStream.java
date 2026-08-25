package org.kosit.validator.helper;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jspecify.annotations.NonNull;

public final class LimitedInputStream extends FilterInputStream {

    private long m_nRemaining;

    public LimitedInputStream(@NonNull final InputStream aIS, final long nLimit) {
        super(aIS);
        m_nRemaining = nLimit;
    }

    @Override
    public int read() throws IOException {
        if (m_nRemaining <= 0)
            return -1;
        final int b = super.read();
        if (b >= 0)
            m_nRemaining--;
        return b;
    }

    @Override
    public int read(final byte[] aBuf, final int nOfs, final int nLen) throws IOException {
        if (m_nRemaining <= 0)
            return -1;
        final int n = super.read(aBuf, nOfs, (int) Math.min(nLen, m_nRemaining));
        if (n > 0)
            m_nRemaining -= n;
        return n;
    }

    @Override
    public long skip(final long nCount) throws IOException {
        final long nSkipped = super.skip(Math.min(nCount, m_nRemaining));
        m_nRemaining -= nSkipped;
        return nSkipped;
    }

    public long getRemaining() {
        return m_nRemaining;
    }
}