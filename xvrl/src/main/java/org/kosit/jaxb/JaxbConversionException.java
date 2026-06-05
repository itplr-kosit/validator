package org.kosit.jaxb;

import org.jspecify.annotations.Nullable;

/**
 * Thrown when {@link JaxbConversionService} fails to marshal or unmarshal a JAXB object.
 */
public class JaxbConversionException extends RuntimeException {

    /**
     * Creates a new exception with the given message and no cause.
     *
     * @param message the detail message
     */
    public JaxbConversionException(final String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message and underlying cause.
     *
     * @param message the detail message
     * @param cause the cause, or {@code null} if none
     */
    public JaxbConversionException(final String message, final @Nullable Throwable cause) {
        super(message, cause);
    }
}
