package org.kosit.base.io;

import java.io.Flushable;
import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for stream handling.
 * 
 * @author Philip Helger
 */
public class StreamHelper {

    /**
     * Flush the passed object encapsulating the declared {@link IOException}.
     *
     * @param aFlushable The flushable to be flushed. May be <code>null</code>.
     * @return <code>true</code> if the object was successfully flushed.
     */
    @NonNull
    public static boolean flush(@Nullable final Flushable aFlushable) {
        if (aFlushable != null)
            try {
                aFlushable.flush();
                return true;
            } catch (final NullPointerException ex) {
                // Happens if a java.io.FilterOutputStream is already closed!
            } catch (final IOException ex) {
                LOGGER.error("Failed to flush object " + aFlushable.getClass().getName(), ex);
            }
        return false;
    }

    /**
     * Close the passed stream by encapsulating the declared {@link IOException}. If the passed object also implements
     * the {@link Flushable} interface, it is tried to be flushed before it is closed.
     *
     * @param aCloseable The object to be closed. May be <code>null</code>.
     * @return <code>true</code> if the object was successfully closed.
     */
    public static boolean close(@Nullable final AutoCloseable aCloseable) {
        if (aCloseable != null) {
            try {
                // flush object (if available)
                if (aCloseable instanceof final Flushable aFlushable)
                    flush(aFlushable);

                // close object
                aCloseable.close();
                return true;
            } catch (final NullPointerException ex) {
                // Happens if a java.io.FilterInputStream or java.io.FilterOutputStream
                // has no underlying stream!
            } catch (final Exception ex) {
                LOGGER.error("Failed to close object " + aCloseable.getClass().getName(), ex);
            }
        }

        return false;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamHelper.class);

    private StreamHelper() {
        // empty
    }
}
