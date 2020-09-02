package de.kosit.validationtool.daemon;

/**
 * Status codes for the HTTP daemon.
 * 
 * @author Andreas Penski
 */
public interface HttpStatus {

    // --- 2xx Success ---

    /** {@code 200 OK} (HTTP/1.0 - RFC 1945) */
    int SC_OK = 200;

    // --- 4xx Client Error ---

    /** {@code 400 Bad Request} (HTTP/1.1 - RFC 2616) */
    int SC_BAD_REQUEST = 400;

    /** {@code 405 Method Not Allowed} (HTTP/1.1 - RFC 2616) */
    int SC_METHOD_NOT_ALLOWED = 405;

    /** {@code 406 Not Acceptable} (HTTP/1.1 - RFC 2616) */
    int SC_NOT_ACCEPTABLE = 406;

    /** {@code 422 Unprocessable Entity} (WebDAV - RFC 2518) */
    public static final int SC_UNPROCESSABLE_ENTITY = 422;

    /** {@code 500 Server Error} (HTTP/1.0 - RFC 1945) */
    int SC_INTERNAL_SERVER_ERROR = 500;

}