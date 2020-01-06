package de.kosit.validationtool.impl.input;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.input.CountingInputStream;

import de.kosit.validationtool.api.Input;

/**
 * Helper for stream handling.
 * 
 * @author Andreas Penski
 */
public class StreamHelper {

    /**
     * Helper class, which generates the hashcode while reading the stream e.g. for parsing the document. This allows
     * generating the hashcode without an aditional reading step.
     */
    private static class DigestingInputStream extends FilterInputStream {

        private final MessageDigest digest;

        private final LazyReadInput reference;

        DigestingInputStream(final LazyReadInput input, final InputStream in, final MessageDigest digest) {
            super(new DigestInputStream(in, digest));
            this.digest = digest;
            this.reference = input;
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.reference.setHashCode(this.digest.digest());
        }

    }

    private static class CountInputStream extends FilterInputStream {

        private final LazyReadInput reference;

        public CountInputStream(final LazyReadInput input, final InputStream stream) {
            super(new org.apache.commons.io.input.CountingInputStream(stream));
            this.reference = input;
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.reference.setLength(((CountingInputStream) this.in).getByteCount());
        }
    }

    private static final int EOF = -1;

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private StreamHelper() {
        // hide
    }

    public static MessageDigest createDigest(final String algorithm) {
        try {
            final MessageDigest digest;
            digest = MessageDigest.getInstance(algorithm);
            return digest;
        } catch (final NoSuchAlgorithmException e) {
            // should not happen
            throw new IllegalArgumentException(String.format("Specified method %s is not available", algorithm), e);
        }
    }

    /**
     * Wraps the {@link InputStream} with a counting length implementation.
     * 
     * @param input the {@link LazyReadInput input}
     * @param stream the stream
     * @return a wrapped stream
     */
    public static InputStream wrapCount(final LazyReadInput input, final InputStream stream) {
        return new CountInputStream(input, stream);
    }

    /**
     * Wraps the {@link InputStream} with an implementation the generates a hash sum over the stream data.
     *
     * @param input the {@link LazyReadInput input}
     * @param stream the stream
     * @return a wrapped stream
     */
    public static InputStream wrapDigesting(final LazyReadInput input, final InputStream stream, final String digestAlgorithm) {
        return new DigestingInputStream(input, stream, createDigest(digestAlgorithm));
    }

    /**
     * Drains the {@link Input} without further processing. This is useful to computing hashcode etc.
     * 
     * @param input the input
     * @return the input drained once
     * @throws IOException on I/O errors
     */
    public static Input drain(final Input input) throws IOException {
        final StreamSource s = (StreamSource) input.getSource();
        try ( final InputStream stream = s.getInputStream() ) {
            drain(stream);
        }
        return input;

    }

    /**
     * Drains the {@link InputStream} without further processing. This is useful to computing hashcode etc.
     *
     * @param input the input
     * @throws IOException on I/O errors
     */
    public static void drain(final InputStream input) throws IOException {
        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        int n;
        while (EOF != (n = input.read(buffer))) {
            // nothing
        }

    }
}
