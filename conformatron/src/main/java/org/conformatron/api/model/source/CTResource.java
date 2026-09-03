package org.conformatron.api.model.source;

import java.io.IOException;
import java.io.InputStream;

import org.conformatron.api.annotation.CheckForSigned;
import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;

/**
 * Represents the raw input resource entering the validation pipeline at step 1.
 * 
 * @author Philip Helger
 */
public interface CTResource {

    /**
     * @return A human-readable name / URI for the input resource (e.g. filename). Used e.g. as the System ID for XML
     *         parsing, in report metadata and log messages.
     */
    @NonNull
    @Nonempty
    String getName();

    /**
     * @return May be -1 if the length is unknown or cannot be determined upfront.
     */
    @CheckForSigned
    long getLength();

    /**
     * Get the input stream to read from the object. Each time this method is called, a new {@link InputStream} needs to
     * be created. The caller is responsible for closing the stream.
     *
     * @return The input stream
     * @throws IOException if opening fails
     */
    @NonNull
    InputStream getInputStream() throws IOException;
}
