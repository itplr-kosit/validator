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

package de.kosit.validationtool.cmd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;

import lombok.Getter;
import lombok.Setter;

/**
 * Helferlein um Ausgaben auf der Kommandozeile zu testen.
 * 
 * @author Andreas Penski
 */
public class CommandLine {

    /**
     * Simpler Proxy für {@link OutputStream}, dessen target ausgetauscht werden kann.
     *
     * @param <O> Typ des eigentlichen {@link OutputStream}
     */
    private static class ReplaceableOutputStream<O extends OutputStream> extends OutputStream {

        @Getter
        @Setter
        private O out;

        @Override
        public void write(final int idx) throws IOException {
            if (this.out != null) {
                this.out.write(idx);
            }
        }

        @Override
        public void write(final byte[] bts) throws IOException {
            if (this.out != null) {
                this.out.write(bts);
            }
        }

        @Override
        public void write(final byte[] bts, final int st, final int end) throws IOException {
            if (this.out != null) {
                this.out.write(bts, st, end);
            }
        }

        @Override
        public void flush() throws IOException {

            if (this.out != null) {
                this.out.flush();
            }
        }

        @Override
        public void close() throws IOException {
            if (this.out != null) {
                this.out.close();
            }
        }
    }

    private static final ReplaceableOutputStream<ByteArrayOutputStream> out = new ReplaceableOutputStream<>();

    private static final ReplaceableOutputStream<ByteArrayOutputStream> error = new ReplaceableOutputStream<>();

    static {
        // Initialisierung muss vor SL4J's SimpleLogger erfolgen, sonst sind logs nicht erfasst.
        // deshalb darf diese Klasse kein Log haben
        System.setOut(new PrintStream(new TeeOutputStream(System.out, out)));
        System.setErr(new PrintStream(new TeeOutputStream(System.err, error)));
        setStandardInput(nullInputStream());
    }

    public static void setStandardInput(final InputStream in) {
        System.setIn(in);
    }

    public static InputStream nullInputStream() {
        return new InputStream() {

            @Override
            public int read() throws IOException {
                return 0;
            }
        };
    }

    public static String getOutput() {
        return new String(out.getOut().toByteArray());
    }

    public static String getErrorOutput() {
        return new String(error.getOut().toByteArray());
    }

    public List<String> getOutputLines() {
        return readLines(out.getOut().toByteArray());
    }

    public List<String> getErrorLines() {
        return readLines(error.getOut().toByteArray());
    }

    private List<String> readLines(final byte[] bytes) {
        try ( final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
              final Reader r = new InputStreamReader(in) ) {

            return IOUtils.readLines(r);
        } catch (final IOException e) {
            throw new IllegalStateException("Can not read input");
        }
    }

    public static void activate() {
        out.setOut(new ByteArrayOutputStream());
        error.setOut(new ByteArrayOutputStream());

    }

    public static void deactivate() {
        out.setOut(null);
        error.setOut(null);
        setStandardInput(nullInputStream());
    }

}
