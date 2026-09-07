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
package org.kosit.validator.testdata;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jspecify.annotations.NonNull;

/**
 * Locates the shared test data of this artifact on the classpath.
 * <p>
 * This replaces the former {@code Paths.get("src/test/resources")} anchor, which resolved against the working directory
 * of the test JVM and therefore only worked as long as every module kept its own copy of the test data.
 *
 * @author Philip Helger
 */
public final class TestData {

    private TestData() {
    }

    private static @NonNull URI toUri(final @NonNull String path) {
        final URL resource = TestData.class.getResource("/" + path);
        if (resource == null) {
            throw new IllegalStateException("Test data resource not found on the classpath: '" + path
                    + "'. Is validator-test-data declared as a test dependency and unpacked?");
        }
        try {
            return resource.toURI();
        } catch (final URISyntaxException e) {
            throw new IllegalStateException("Test data resource has a malformed URL: " + resource, e);
        }
    }

    /**
     * Locates a single test data file.
     *
     * @param path the path relative to the root of the test data, e.g. {@code examples/simple/scenarios.xml}
     * @return the URI of the file, never {@code null}
     * @throws IllegalStateException if the resource is not on the classpath
     */
    public static URI file(final String path) {
        return toUri(path);
    }

    /**
     * Locates a test data directory. The trailing slash is mandatory, because {@link Class#getResource(String)} returns
     * the path unchanged: without it the resulting URI resolves relative artifacts into the <em>parent</em> directory.
     *
     * @param path the path relative to the root of the test data, ending with a slash, e.g.
     *            {@code examples/simple/repository/}
     * @return the URI of the directory, never {@code null}
     * @throws IllegalStateException if the resource is not on the classpath
     */
    public static URI dir(final String path) {
        if (!path.endsWith("/")) {
            throw new IllegalArgumentException("Directory path must end with '/': '" + path + "'");
        }
        return toUri(path);
    }

    /**
     * Builds a URI below an existing test data directory that deliberately does not exist. Needed for negative tests,
     * because {@link Class#getResource(String)} simply returns {@code null} for something that is not there.
     *
     * @param dirPath the path of an existing directory, ending with a slash
     * @param name the name of the non existing artifact within that directory
     * @return the URI of the non existing artifact, never {@code null}
     */
    public static URI missing(final String dirPath, final String name) {
        // Deliberately not URI.resolve: that fails for the opaque "jar:" URIs returned when the test data is read
        // straight from the artifact instead of from an unpacked directory.
        return URI.create(dir(dirPath).toASCIIString() + name);
    }

    private static Path codeSource() {
        final CodeSource source = TestData.class.getProtectionDomain().getCodeSource();
        if (source == null) {
            throw new IllegalStateException("The test data does not report a code source to build an archive from");
        }
        try {
            return Paths.get(source.getLocation().toURI());
        } catch (final URISyntaxException e) {
            throw new IllegalStateException("The test data has a malformed code source: " + source.getLocation(), e);
        }
    }

    /**
     * Packs the compiled test data into a temporary jar, including the directory entries, so that the result is laid
     * out like the artifact this module produces.
     *
     * @param directory the directory the test data was compiled into
     * @return the path of the temporary jar, never {@code null}
     */
    private static Path pack(final Path directory) {
        try {
            final Path archive = Files.createTempFile("validator-test-data", ".jar");
            archive.toFile().deleteOnExit();
            final List<Path> entries;
            try ( final Stream<Path> walk = Files.walk(directory) ) {
                entries = walk.filter(path -> !path.equals(directory)).toList();
            }
            try ( final ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(archive)) ) {
                for (final Path entry : entries) {
                    final boolean isDirectory = Files.isDirectory(entry);
                    final String name = directory.relativize(entry).toString().replace('\\', '/');
                    out.putNextEntry(new ZipEntry(isDirectory ? name + "/" : name));
                    if (!isDirectory) {
                        Files.copy(entry, out);
                    }
                    out.closeEntry();
                }
            }
            return archive;
        } catch (final IOException e) {
            throw new UncheckedIOException("Can not pack the test data into an archive", e);
        }
    }

    /**
     * Holds the archive of the test data, created on first use only.
     */
    private static final class Archive {

        static final Path PATH;

        static {
            final Path source = codeSource();
            PATH = Files.isDirectory(source) ? pack(source) : source;
        }

        private Archive() {
        }
    }

    /**
     * Locates test data inside an archive. Reaching into an archive is a separate code path of the resolving
     * strategies, and the tests covering it need artifacts that really live in a jar. Whether this module itself is a
     * jar on the class path depends on the Maven goal - {@code mvn verify} packages it, {@code mvn test} and the IDE
     * hand out the plain output directory - so the test data is packed on demand instead.
     *
     * @param path the path relative to the root of the test data, e.g. {@code simple/packaged/repository/}
     * @return the {@code jar:} URI of the artifact, never {@code null}
     * @throws IllegalStateException if the resource is not on the classpath
     */
    public static URI inArchive(final String path) {
        // resolved against the class path as well, so that a typo fails here instead of somewhere down the line
        toUri(path);
        return URI.create("jar:" + Archive.PATH.toUri() + "!/" + path);
    }
}
