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
package org.kosit.validator.helper;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.kosit.validator.impl.conformatron.source.ResourceHelper;

/**
 * A JUnit extension that maintains a {@link ResourceHelper} for the lifetime of a single test method. It is the JUnit 6
 * counterpart of the JUnit 4 {@code ExternalResource} rule: a new {@link ResourceHelper} is created before each test
 * method and it is closed again after each test method, so that all temporary files and closeables acquired by a test
 * are released.
 * <p>
 * Usage:
 *
 * <pre>
 * 
 * &#64;RegisterExtension
 * private final ResourceHelperExtension resHelper = new ResourceHelperExtension();
 *
 * &#64;Test
 * public void testSomething() throws IOException {
 *     final ReadResource readRes = ReadResource.of(res, resHelper.get());
 * }
 * </pre>
 *
 * @author Philip Helger
 */
public final class ResourceHelperExtension implements BeforeEachCallback, AfterEachCallback {

    private @Nullable ResourceHelper resHelper;

    public ResourceHelperExtension() {
    }

    public void beforeEach(final @NonNull ExtensionContext ctx) {
        resHelper = new ResourceHelper();
    }

    public void afterEach(final @NonNull ExtensionContext ctx) {
        final ResourceHelper old = resHelper;
        // Ensure a stale helper is never handed out afterwards
        resHelper = null;
        if (old != null)
            old.close();
    }

    /**
     * @return The {@link ResourceHelper} of the currently running test method. Never <code>null</code>.
     * @throws IllegalStateException If no test method is currently running, meaning the extension was not registered
     *             via {@code @RegisterExtension} or {@code @ExtendWith}.
     */
    public @NonNull ResourceHelper get() {
        final ResourceHelper ret = resHelper;
        if (ret == null)
            throw new IllegalStateException("No ResourceHelper is available. Is this extension registered correctly?");
        return ret;
    }
}
