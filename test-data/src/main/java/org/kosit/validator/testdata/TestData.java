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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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

    private static URI toUri(final String path) {
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
}
