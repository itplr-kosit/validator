package org.kosit.base.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.Test;

public class StreamHelperTest {

    private static final class FlushableCloseable implements Flushable, Closeable {

        private boolean flushed;

        private boolean closed;

        private final boolean fail;

        private FlushableCloseable(final boolean fail) {
            this.fail = fail;
        }

        public void flush() throws IOException {
            this.flushed = true;
            if (this.fail)
                throw new IOException("flush failed");
        }

        public void close() throws IOException {
            this.closed = true;
            if (this.fail)
                throw new IOException("close failed");
        }
    }

    @Test
    public void flushNullIsFalse() {
        assertThat(StreamHelper.flush(null)).isFalse();
    }

    @Test
    public void flushSuccess() {
        final FlushableCloseable obj = new FlushableCloseable(false);
        assertThat(StreamHelper.flush(obj)).isTrue();
        assertThat(obj.flushed).isTrue();
    }

    @Test
    public void flushSwallowsIoException() {
        final FlushableCloseable obj = new FlushableCloseable(true);
        assertThat(StreamHelper.flush(obj)).isFalse();
        assertThat(obj.flushed).isTrue();
    }

    @Test
    public void flushSwallowsNullPointerExceptionOfClosedFilterStream() {
        // a FilterOutputStream without an underlying stream throws a NullPointerException on flush
        assertThat(StreamHelper.flush(new FilterOutputStream((OutputStream) null))).isFalse();
    }

    @Test
    public void closeNullIsFalse() {
        assertThat(StreamHelper.close(null)).isFalse();
    }

    @Test
    public void closeFlushesBeforeClosing() {
        final FlushableCloseable obj = new FlushableCloseable(false);
        assertThat(StreamHelper.close(obj)).isTrue();
        assertThat(obj.flushed).isTrue();
        assertThat(obj.closed).isTrue();
    }

    @Test
    public void closeWithoutFlushable() {
        final ByteArrayInputStream is = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
        assertThat(StreamHelper.close(is)).isTrue();
    }

    @Test
    public void closeSwallowsException() {
        final FlushableCloseable obj = new FlushableCloseable(true);
        assertThat(StreamHelper.close(obj)).isFalse();
        assertThat(obj.closed).isTrue();
    }

    @Test
    public void closeIsRepeatable() {
        final FlushableCloseable obj = new FlushableCloseable(false);
        assertThat(StreamHelper.close(obj)).isTrue();
        assertThat(StreamHelper.close(obj)).isTrue();
    }
}
