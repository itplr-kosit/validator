/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kosit.validationtool.impl.input;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @author Andreas Penski
 */
public class StreamHelperTest {

    /**
     * Simulates a stream that is return 0 for {@link InputStream#available()} even though content is supplied.
     */
    private static class MyLazyStream extends FilterInputStream {

        protected MyLazyStream(final InputStream in) {
            super(in);
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
            assertThat(peekable.available()).isGreaterThan(0);
            final String read = IOUtils.toString(peekable, Charset.defaultCharset());
            assertThat(read).isEqualTo(myContent);
        }
    }
}
