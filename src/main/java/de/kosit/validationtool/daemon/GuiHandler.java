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
        } else{
            final URL resource = GuiHandler.class.getClassLoader().getResource("gui" + path);
            if (resource != null) {
                write(exchange, IOUtils.toString(resource, Charset.defaultCharset()).getBytes(),
                        Mediatype.resolveBySuffix(resource.getPath()).getMimeType());
            }else {
                error(exchange,404,"not found");
            }
        }
    }


    @RequiredArgsConstructor
    @Getter
    protected enum Mediatype {
        JS("application/javascript"),
        MD("text/markdown"),
        CSS("text/css");
        private final String mimeType;

        static Mediatype resolveBySuffix(final String path) {
            return Arrays.stream(values()).filter(e->path.toUpperCase().endsWith("."+e.name())).findFirst().orElse(Mediatype.MD);
        }
    }
}
