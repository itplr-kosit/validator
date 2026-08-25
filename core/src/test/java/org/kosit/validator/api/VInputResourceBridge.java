package org.kosit.validator.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.xml.transform.stream.StreamSource;

import org.conformatron.api.annotation.CheckForSigned;
import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.model.source.CTResource;
import org.jspecify.annotations.NonNull;
import org.kosit.validator.impl.conformatron.source.ReadResource;

@Deprecated(forRemoval = true)
public final class VInputResourceBridge {

    /**
     * Wraps a legacy {@link VInput} as a complete XML source. The validator currently only feeds XML documents into the
     * pipeline, so the detected syntax is fixed until DETECT_SYNTAX is implemented as its own action.
     *
     * @param input the legacy input
     * @return a new source facade
     */
    @Deprecated(forRemoval = true)
    public static @NonNull CTResource resource(final @NonNull VInput input) {
        Objects.requireNonNull(input);
        return new CTResource() {

            public @NonNull @Nonempty String getName() {
                return input.getName();
            }

            public @CheckForSigned long getLength() {
                return -1;
            }

            public InputStream getInputStream() throws IOException {
                if (input.getSource() instanceof final StreamSource src) {
                    final InputStream ret = src.getInputStream();
                    if (ret == null)
                        throw new IOException("Failed to open InputStream from StreamSource");
                    return ret;
                }
                throw new IllegalStateException("Unsupported source: " + input.getSource().getClass().getName());
            }
        };
    }

    @Deprecated(forRemoval = true)
    public static @NonNull ReadResource of(final @NonNull VInput input) {
        try {
            // Always read in memory
            return new ReadResource(resource(input), ReadResource.HASH_ALGORITHM_NAME, null, srcLength -> false);
        } catch (final IOException e) {
            throw new UncheckedIOException("Error opening/reading old input", e);
        } catch (final NoSuchAlgorithmException e) {
            // Should never happen
            throw new IllegalStateException("Unknown hash algorithm name '" + ReadResource.HASH_ALGORITHM_NAME + "'", e);
        }
    }

}
