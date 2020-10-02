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

package de.kosit.validationtool.daemon;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import com.sun.net.httpserver.HttpExchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class GuiHandler extends BaseHandler {

    private static final URL INDEX_HTML = GuiHandler.class.getClassLoader().getResource("gui/index.html");

    public GuiHandler() {
        if (INDEX_HTML == null) {
            throw new IllegalStateException("No html found");
        }
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        assert INDEX_HTML != null;
        final String path = exchange.getRequestURI().toASCIIString();
        if (path.equals("/")) {
            write(exchange, IOUtils.toString(INDEX_HTML, Charset.defaultCharset()).getBytes(), "text/html");
        } else {
            final URL resource = GuiHandler.class.getClassLoader().getResource("gui" + path);
            if (resource != null) {
                write(exchange, IOUtils.toString(resource, Charset.defaultCharset()).getBytes(),
                        Mediatype.resolveBySuffix(resource.getPath()).getMimeType());
            } else {
                error(exchange, 404, "not found");
            }
        }
    }

    @RequiredArgsConstructor
    @Getter
    protected enum Mediatype {

        JS("application/javascript"), MD("text/markdown"), CSS("text/css");

        private final String mimeType;

        static Mediatype resolveBySuffix(final String path) {
            return Arrays.stream(values()).filter(e -> path.toUpperCase().endsWith("." + e.name())).findFirst().orElse(Mediatype.MD);
        }
    }
}
