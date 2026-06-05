package org.kosit.validator.impl.input;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * @author Andreas Penski
 */
public class StreamHelperTest {

    /**
     * Simulates a stream that is return 0 for {@link InputStream#available()} even though content is supplied.
     */
    private static class MyLazyStream extends FilterInputStream {

        protected MyLazyStream(final InputStream is) {
            super(is);
        }

        @Override
        public int available() throws IOException {
            return 0;
        }
    }

    @Test
    public void testLazyStream() throws IOException {
        final String myContent = "SomeBytes";
        try ( final InputStream in = new MyLazyStream(new ByteArrayInputStream(myContent.getBytes())) ) {
            final BufferedInputStream peekable = StreamHelper.wrapPeekable(in);
            assertThat(peekable.available()).isPositive();
            final String read = IOUtils.toString(peekable, Charset.defaultCharset());
            assertThat(read).isEqualTo(myContent);
        }
    }
}
