package de.kosit.validationtool.daemon;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Simple base implemenation for http handlers. Doing I/O stuff.
 * 
 * @author Andreas Penski
 */
public abstract class BaseHandler implements HttpHandler {

    protected static final String APPLICATION_XML = "application/xml";

    protected static void write(final HttpExchange exchange, final byte[] content, final String contentType) throws IOException {
        final OutputStream os = exchange.getResponseBody();
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, content.length);
        os.write(content);
        os.close();
    }

    protected static void error(final HttpExchange httpExchange, final int statusCode, final String message) throws IOException {
        final byte[] bytes = message.getBytes();
        httpExchange.sendResponseHeaders(statusCode, bytes.length);
        final OutputStream os = httpExchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

}
