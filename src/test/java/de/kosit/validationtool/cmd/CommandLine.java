/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.cmd;

import java.io.*;
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

    private static ReplaceableOutputStream<ByteArrayOutputStream> out = new ReplaceableOutputStream<>();

    private static ReplaceableOutputStream<ByteArrayOutputStream> error = new ReplaceableOutputStream<>();

    static {
        // Initialisierung muss vor SL4J's SimpleLogger erfolgen, sonst sind logs nicht erfasst.
        // deshalb darf diese Klasse kein Log haben
        System.setOut(new PrintStream(new TeeOutputStream(System.out, out)));
        System.setErr(new PrintStream(new TeeOutputStream(System.err, error)));
    }

    public String getOutput() {
        return new String(out.getOut().toByteArray());
    }


    public String getErrorOutput() {
        return new String(error.getOut().toByteArray());
    }

    public List<String> getOutputLines() {
        return readLines(out.getOut().toByteArray());
    }

    public List<String> getErrorLines() {
        return readLines(error.getOut().toByteArray());
    }

    private List<String> readLines(byte[] bytes) {
        try ( ByteArrayInputStream in = new ByteArrayInputStream(bytes);
              Reader r = new InputStreamReader(in) ) {

            return IOUtils.readLines(r);
        } catch (IOException e) {
            throw new IllegalStateException("Can not read input");
        }
    }

    public void activate() {
        out.setOut(new ByteArrayOutputStream());
        error.setOut(new ByteArrayOutputStream());

    }

    public void deactivate() {
        out.setOut(null);
        error.setOut(null);
    }

    /**
     * Simpler Proxy für {@link OutputStream}, dessen target ausgetauscht werden kann.
     * 
     * @param <O> Typ des eigentlichen {@link OutputStream}
     */
    private static class ReplaceableOutputStream<O extends OutputStream> extends OutputStream {

        @Getter
        @Setter
        private O out;

        public void write(int idx) throws IOException {
            if (out != null) {
                this.out.write(idx);
            }
        }

        public void write(byte[] bts) throws IOException {
            if (out != null) {
                this.out.write(bts);
            }
        }

        public void write(byte[] bts, int st, int end) throws IOException {
            if (out != null) {
                this.out.write(bts, st, end);
            }
        }

        public void flush() throws IOException {

            if (out != null) {
                this.out.flush();
            }
        }

        public void close() throws IOException {
            if (out != null) {
                this.out.close();
            }
        }
    }

}
