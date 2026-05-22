/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
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
package de.kosit.validationtool.impl.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;

import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * {@link LSResourceResolver} that can load objects relative to a base path from the application's classpath.
 *
 * @author Andreas Penski
 */
public class ClassPathResourceResolver implements LSResourceResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathResourceResolver.class);

    /**
     * Simple {@link LSInput} implementation that can supply a stream
     */
    private static class LSInputImpl implements LSInput {

        private Reader characterStream;

        private InputStream byteStream;

        private String systemId;

        private String publicId;

        private String baseURI;

        private String encoding;

        private boolean certifiedText;

        private String stringData;

        /**
         * Instantiates a new instance.
         *
         * @param publicId the publicId
         * @param systemId the systemId
         * @param baseURI the baseURI
         */
        public LSInputImpl(final String publicId, final String systemId, final String baseURI) {
            this.publicId = publicId;
            this.systemId = systemId;
            this.baseURI = baseURI;
        }

        @Override
        public boolean getCertifiedText() {
            return this.certifiedText;
        }

        public Reader getCharacterStream() {
            return this.characterStream;
        }

        public InputStream getByteStream() {
            return this.byteStream;
        }

        public String getSystemId() {
            return this.systemId;
        }

        public String getPublicId() {
            return this.publicId;
        }

        public String getBaseURI() {
            return this.baseURI;
        }

        public String getEncoding() {
            return this.encoding;
        }

        public String getStringData() {
            return this.stringData;
        }

        public void setCharacterStream(final Reader characterStream) {
            this.characterStream = characterStream;
        }

        public void setByteStream(final InputStream byteStream) {
            this.byteStream = byteStream;
        }

        public void setSystemId(final String systemId) {
            this.systemId = systemId;
        }

        public void setPublicId(final String publicId) {
            this.publicId = publicId;
        }

        public void setBaseURI(final String baseURI) {
            this.baseURI = baseURI;
        }

        public void setEncoding(final String encoding) {
            this.encoding = encoding;
        }

        public void setCertifiedText(final boolean certifiedText) {
            this.certifiedText = certifiedText;
        }

        public void setStringData(final String stringData) {
            this.stringData = stringData;
        }

        public LSInputImpl() {
        }
    }

    private final URI base;

    /**
     * Instantiates a new resolver with the given base path
     *
     * @param basePath the base path
     */
    public ClassPathResourceResolver(final String basePath) {
        if (!Strings.CS.startsWith(basePath, "/")) {
            throw new IllegalArgumentException("Base path must start with a slash");
        }
        this.base = URI.create(basePath + (basePath.endsWith("/") == basePath.length() > 1 ? "" : "/"));
    }

    public ClassPathResourceResolver(final URI jarUri) {
        this.base = jarUri;
    }

    @Override
    public LSInput resolveResource(final String type, final String namespaceURI, final String publicId, final String systemId,
            final String baseURI) {
        final URI resolved = RelativeUriResolver.resolve(URI.create(systemId), this.base);
        if (resolved != null) {
            try {
                final URL resource = resolved.isAbsolute() ? resolved.toURL()
                        : ClassPathResourceResolver.class.getResource(resolved.toASCIIString());
                final LSInputImpl input = new LSInputImpl(publicId, systemId, resolved.toASCIIString());
                // intentionally not closed, since xml stack wants it open upon return
                final InputStream in = resource.openStream();
                input.setByteStream(in);
                return input;
            } catch (final IOException e) {
                LOGGER.error("Error loading schema resource from {}", resolved, e);
            }
        }
        // not found
        return null;
    }
}
