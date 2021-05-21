/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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

import java.io.Reader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;

import lombok.SneakyThrows;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.trans.XPathException;

/**
 * @author Andreas Penski
 */
public class ProcessorProvider {

    private static class SecureUriResolver implements CollectionFinder, OutputURIResolver, UnparsedTextURIResolver {

        public static final String MESSAGE = "Configuration error. Resolving ist not allowed";

        @Override
        public OutputURIResolver newInstance() {
            return this;
        }

        @Override
        public Result resolve(final String href, final String base) throws TransformerException {
            throw new IllegalStateException(MESSAGE);
        }

        @Override
        public void close(final Result result) throws TransformerException {
            throw new IllegalStateException(MESSAGE);
        }

        @Override
        public Reader resolve(final URI absoluteURI, final String encoding, final Configuration config) throws XPathException {
            throw new IllegalStateException(MESSAGE);
        }

        @Override
        public ResourceCollection findCollection(final XPathContext context, final String collectionURI) throws XPathException {
            throw new IllegalStateException(MESSAGE);
        }
    }

    protected static final String DISSALLOW_DOCTYPE_DECL_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

    protected static final String LOAD_EXTERNAL_DTD_FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    protected static final String FEATURE_SECURE_PROCESSING = "http://javax.xml.XMLConstants/feature/secure-processing";

    private static Processor processor;

    @SneakyThrows
    private static String encode(final String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8.name());
    }

    public static Processor getProcessor() {
        if (processor == null) {
            processor = createProcessor();
        }
        return processor;
    }

    private static Processor createProcessor() {
        final Processor processor = new Processor(false);
        // verhindere global im Prinzip alle resolving strategien
        final SecureUriResolver resolver = new SecureUriResolver();
        processor.getUnderlyingConfiguration().setCollectionFinder(resolver);
        processor.getUnderlyingConfiguration().setOutputURIResolver(resolver);// NOSONAR
        processor.getUnderlyingConfiguration().setUnparsedTextURIResolver(resolver);

        // grundsätzlich Feature-konfiguration:
        processor.setConfigurationProperty(Feature.DTD_VALIDATION, false);
        processor.setConfigurationProperty(Feature.ENTITY_RESOLVER_CLASS, "");
        processor.setConfigurationProperty(Feature.XINCLUDE, false);
        processor.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);

        // Konfiguration des zu verwendenden Parsers, wenn Saxon selbst einen erzeugen muss, bspw. beim XSL parsen
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(FEATURE_SECURE_PROCESSING), true); // NOSONAR
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(DISSALLOW_DOCTYPE_DECL_FEATURE), true);// NOSONAR
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(LOAD_EXTERNAL_DTD_FEATURE), false);// NOSONAR
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(XMLConstants.ACCESS_EXTERNAL_DTD), false);// NOSONAR
        return processor;
    }
}
