/*
 * Copyright 2017-2026  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kosit.validator.impl.conformatron.source;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import org.conformatron.api.annotation.CheckForSigned;
import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.annotation.Nonnegative;
import org.conformatron.api.model.source.CTResource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.io.StreamHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator implementation of Conformatron resources.
 *
 * @author Philip Helger
 */
public final class Resource {

    private static final class ResourceInputStream implements CTResource {

        private final @NonNull String name;

        private final @NonNull InputStream is;

        private ResourceInputStream(@NonNull @Nonempty final String name, final @NonNull InputStream is) {
            Objects.requireNonNull(name);
            Objects.requireNonNull(is);
            this.name = name;
            this.is = is;
        }

        public @NonNull @Nonempty String getName() {
            return name;
        }

        public @CheckForSigned long getLength() {
            return -1;
        }

        public @NonNull InputStream getInputStream() {
            return is;
        }
    }

    /**
     * Implementation of {@link CTResource} based on a byte array.
     * 
     * @author Philip Helger
     *
     */
    private static final class ResourceByteArray implements CTResource {

        private final @NonNull String name;

        private final byte @NonNull [] bytes;

        private ResourceByteArray(@NonNull @Nonempty final String name, final byte @NonNull [] bytes) {
            Objects.requireNonNull(name);
            Objects.requireNonNull(bytes);
            this.name = name;
            this.bytes = bytes;
        }

        public @NonNull @Nonempty String getName() {
            return name;
        }

        public @Nonnegative long getLength() {
            return bytes.length;
        }

        public @NonNull ByteArrayInputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }
    }

    private static final class ResourceFile implements CTResource {

        private final @NonNull File f;

        private ResourceFile(@NonNull final File f) {
            Objects.requireNonNull(f);
            this.f = f;
        }

        public @NonNull @Nonempty String getName() {
            return f.getAbsolutePath();
        }

        public @Nonnegative long getLength() {
            return f.length();
        }

        public @NonNull BufferedInputStream getInputStream() throws IOException {
            // Buffer immediately for performance
            return new BufferedInputStream(new FileInputStream(f));
        }
    }

    private static final class ResourceURL implements CTResource {

        private static final Logger LOGGER = LoggerFactory.getLogger(Resource.ResourceURL.class);

        private final @NonNull URL u;

        private ResourceURL(@NonNull final URL u) {
            Objects.requireNonNull(u);
            this.u = u;
        }

        public @NonNull @Nonempty String getName() {
            return u.toExternalForm();
        }

        public @CheckForSigned long getLength() {
            // We don't know
            return -1;
        }

        @Nullable
        private static InputStream getInputStream(@NonNull final URL aURL, @CheckForSigned final int nConnectTimeoutMS,
                @CheckForSigned final int nReadTimeoutMS, @Nullable final Consumer<? super URLConnection> aConnectionModifier) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "getInputStream ('" + aURL + "', " + nConnectTimeoutMS + ", " + nReadTimeoutMS + ", " + aConnectionModifier + ")");
            }
            URLConnection aConnection;
            HttpURLConnection aHTTPConnection = null;
            try {
                aConnection = aURL.openConnection();
                if (nConnectTimeoutMS >= 0)
                    aConnection.setConnectTimeout(nConnectTimeoutMS);
                if (nReadTimeoutMS >= 0)
                    aConnection.setReadTimeout(nReadTimeoutMS);
                if (aConnection instanceof final HttpURLConnection aHUC)
                    aHTTPConnection = aHUC;

                // Disable caching
                aConnection.setUseCaches(false);

                // Apply optional callback
                if (aConnectionModifier != null)
                    aConnectionModifier.accept(aConnection);

                // by default follow-redirects is true for HTTPUrlConnections
                final InputStream ret = aConnection.getInputStream();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("  returning " + ret);
                }

                return ret;
            } catch (final IOException ex) {
                if (ex instanceof SocketTimeoutException) {
                    LOGGER.warn("Timeout to open input stream for '" + aURL + "': " + ex.getClass().getName() + " - " + ex.getMessage());
                } else {
                    LOGGER.warn("Failed to open input stream for '" + aURL + "': " + ex.getClass().getName() + " - " + ex.getMessage());
                }

                if (aHTTPConnection != null) {
                    // Read error completely for keep-alive (see
                    // http://docs.oracle.com/javase/6/docs/technotes/guides/net/http-keepalive.html)
                    InputStream aErrorIS = null;
                    try {
                        aErrorIS = aHTTPConnection.getErrorStream();
                        if (aErrorIS != null) {
                            final byte[] aBuf = new byte[1024];
                            // read the response body
                            while (aErrorIS.read(aBuf) > 0) {
                                // Read next
                            }
                        }
                    } catch (final IOException ex2) {
                        // deal with the exception
                        LOGGER.warn("Failed to consume error stream for '" + aURL + "': " + ex2.getClass().getName() + " - "
                                + ex2.getMessage());
                    } finally {
                        StreamHelper.close(aErrorIS);
                    }
                }
            }
            return null;
        }

        public @NonNull BufferedInputStream getInputStream() throws IOException {
            // Buffer immediately for performance
            return new BufferedInputStream(getInputStream(u, -1, -1, null));
        }
    }

    public static @NonNull ResourceInputStream of(final @NonNull @Nonempty String name, final @NonNull InputStream is) {
        return new ResourceInputStream(name, is);
    }

    public static @NonNull CTResource stdin() {
        return of("stdin", System.in);
    }

    public static @NonNull ResourceByteArray of(final @NonNull @Nonempty String name, final byte @NonNull [] bytes) {
        return new ResourceByteArray(name, bytes);
    }

    public static @NonNull ResourceByteArray utf8(final @NonNull @Nonempty String name, final @NonNull String str) {
        return of(name, str.getBytes(StandardCharsets.UTF_8));
    }

    public static @NonNull ResourceFile of(final @NonNull File f) {
        return new ResourceFile(f);
    }

    public static @NonNull ResourceFile of(final @NonNull Path p) {
        return new ResourceFile(p.toFile());
    }

    public static @NonNull ResourceURL of(final @NonNull URL u) {
        return new ResourceURL(u);
    }

    public static @NonNull ResourceURL of(final @NonNull URI u) {
        try {
            return new ResourceURL(u.toURL());
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Failed to convert URI '" + u + "' to a URL", e);
        }
    }

    private Resource() {
    }
}
