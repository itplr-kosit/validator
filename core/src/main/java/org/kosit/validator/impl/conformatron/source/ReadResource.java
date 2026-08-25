package org.kosit.validator.impl.conformatron.source;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.annotation.Nonnegative;
import org.conformatron.api.model.source.CTReadResource;
import org.conformatron.api.model.source.CTResource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.validator.api.VInput;
import org.kosit.validator.api.helper.ThrowingSupplier;

/**
 * Default implementation of {@link CTReadResource}
 * 
 * @author Philip Helger
 *
 */
public final class ReadResource implements CTReadResource {

    @FunctionalInterface
    public interface TempFileUsageDecider {

        /**
         * @param srcLength Number of bytes of the source message.
         * @return
         */
        boolean useTempFile(long srcLength);
    }

    private static final long MAX_IN_MEMORY_BYTES = 5 * 1024 * 1024;

    private static final String HASH_ALGORITHM_NAME = "SHA-512";

    private final @NonNull CTResource res;

    private final @Nonnegative long resLength;

    private final @NonNull String hashAlgorithm;

    private final @NonNull ThrowingSupplier<InputStream, IOException> streamSupplier;

    private final byte @NonNull [] hashBytes;

    public static @NonNull ReadResource of(final @NonNull VInput input) throws IOException {
        try {
            // Always read in memory
            return new ReadResource(Resource.of(input), HASH_ALGORITHM_NAME, null, srcLength -> false);
        } catch (final NoSuchAlgorithmException e) {
            // Should never happen
            throw new IllegalStateException("Unknown hash algorithm name '" + HASH_ALGORITHM_NAME + "'", e);
        }
    }

    public static @NonNull ReadResource of(final @NonNull CTResource res, final @NonNull ResourceHelper resHelper) throws IOException {
        try {
            return new ReadResource(res, HASH_ALGORITHM_NAME, resHelper, srcLength -> srcLength < 0 || srcLength > MAX_IN_MEMORY_BYTES);
        } catch (final NoSuchAlgorithmException e) {
            // Should never happen
            throw new IllegalStateException("Unknown hash algorithm name '" + HASH_ALGORITHM_NAME + "'", e);
        }
    }

    ReadResource(@NonNull final CTResource res, final @NonNull @Nonempty String hashAlgorithm, final @Nullable ResourceHelper resHelper,
            final @NonNull TempFileUsageDecider tempFileUsage) throws IOException, NoSuchAlgorithmException {
        Objects.requireNonNull(res);
        Objects.requireNonNull(hashAlgorithm);

        this.res = res;
        this.hashAlgorithm = hashAlgorithm;

        final MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
        final long srcLength = res.getLength();

        // If length is unknown or too large, back by temporary file
        if (tempFileUsage.useTempFile(srcLength)) {
            // Use a temporary file
            Objects.requireNonNull(resHelper);
            final File f = resHelper.createTempFile();
            try ( final InputStream is = res.getInputStream();
                  final BufferedInputStream bis = new BufferedInputStream(is);
                  final DigestInputStream dis = new DigestInputStream(bis, md);
                  final OutputStream fos = new FileOutputStream(f) ) {
                // Write all in temp file
                this.resLength = dis.transferTo(fos);
            }
            this.streamSupplier = () -> new BufferedInputStream(new FileInputStream(f));
        } else {
            // Keep in memory as byte array
            this.resLength = srcLength;
            try ( final InputStream is = res.getInputStream();
                  final BufferedInputStream bis = new BufferedInputStream(is);
                  final DigestInputStream dis = new DigestInputStream(bis, md);
                  final ByteArrayOutputStream baos = new ByteArrayOutputStream() ) {
                // read all bytes
                dis.transferTo(baos);
                // Extract bytes
                final byte[] bytes = baos.toByteArray();
                this.streamSupplier = () -> new ByteArrayInputStream(bytes);
            }
        }

        this.hashBytes = md.digest();
    }

    public @NonNull CTResource getSource() {
        return res;
    }

    public @NonNull InputStream getSourceStream() throws IOException {
        return streamSupplier.get();
    }

    public @NonNull String getHashAlgorithmName() {
        return hashAlgorithm;
    }

    public byte @NonNull [] getHashBytes() {
        return hashBytes;
    }
}