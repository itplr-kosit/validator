package org.conformatron.api.model.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import javax.xml.transform.stream.StreamSource;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Defines a single resource that was once completely read and can be read again. This is a repeatable representation of
 * a {@link CTResource} which also includes a hash digest.
 * 
 * @author Philip Helger
 */
public interface CTReadResource {

    /**
     * 
     * @return The underlying source resource description.
     */
    @NonNull
    CTResource getSource();

    /**
     * 
     * @return The underlying source resource name.
     */
    @NonNull
    default String getName() {
        return getSource().getName();
    }

    /**
     * @return Get a repeatable input stream on the whole document - either backed by a byte array of if large enough
     *         (and possible) via a temporary file.
     * @throws IOException if reading from an underlying source failed
     */
    @NonNull
    InputStream getSourceStream() throws IOException;

    /**
     * Perform something with the {@link InputStream} of this object and making sure, that it gets closed correctly
     * afterwards.
     *
     * @param <T> The result type of handling the {@link InputStream}
     * @param aFunc The function to be invoked to read from the {@link InputStream}. This function needs to be able to
     *            deal with a <code>null</code>-parameter.
     * @return The result of the function. May be <code>null</code>.
     * @throws IOException In case reading from the InputStream fails
     */
    @Nullable
    default <T> T withSourceStreamDo(@NonNull final Function<? super InputStream, T> aFunc) throws IOException {
        try ( final InputStream aIS = getSourceStream() ) {
            return aFunc.apply(aIS);
        }
    }

    /**
     * @return A new {@link StreamSource} based on the {@link InputStream} of this object.
     * @throws IOException if opening the stream fails
     */
    default @NonNull StreamSource getAsSource() throws IOException {
        return getAsSource(getName());
    }

    /**
     * @param systemId The systemID to be used for resolving to or from this source. May be <code>null</code>.
     * @return A new {@link StreamSource} based on the {@link InputStream} of this object.
     * @throws IOException if opening the stream fails
     */
    default @NonNull StreamSource getAsSource(final @Nullable String systemId) throws IOException {
        if (systemId == null)
            return new StreamSource(getSourceStream());
        return new StreamSource(getSourceStream(), getName());
    }

    /**
     * 
     * @return The name of the used hash algorithm.
     */
    @NonNull
    @Nonempty
    String getHashAlgorithmName();

    /**
     * @return Hash bytes of {@link #getSourceStream()} using the algorithm defined in {@link #getHashAlgorithmName()}.
     *         Used for integrity verification and report audit metadata.
     */
    byte @NonNull [] getHashBytes();
}
