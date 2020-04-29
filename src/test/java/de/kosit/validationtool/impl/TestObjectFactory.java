package de.kosit.validationtool.impl;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.trans.XPathException;

/**
 * @author Andreas Penski
 */
public class TestObjectFactory {

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

    private static final String DISSALLOW_DOCTYPE_DECL_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

    private static final String LOAD_EXTERNAL_DTD_FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    private static final String FEATURE_SECURE_PROCESSING = "http://javax.xml.XMLConstants/feature/secure-processing";

    private static Processor processor;

    private static String encode(final String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("Error encoding property while initializing saxon", e);
        }
    }

    public static Processor createProcessor() {
        if (processor == null) {
            processor = new Processor(false);
            // verhindere global im Prinzip alle resolving strategien
            final SecureUriResolver resolver = new SecureUriResolver();
            processor.getUnderlyingConfiguration().setCollectionFinder(resolver);
            processor.getUnderlyingConfiguration().setOutputURIResolver(resolver);
            processor.getUnderlyingConfiguration().setUnparsedTextURIResolver(resolver);

            // grundsätzlich Feature-konfiguration:
            processor.setConfigurationProperty(FeatureKeys.DTD_VALIDATION, false);
            processor.setConfigurationProperty(FeatureKeys.ENTITY_RESOLVER_CLASS, "");
            processor.setConfigurationProperty(FeatureKeys.XINCLUDE, false);
            processor.setConfigurationProperty(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS, false);

            // Konfiguration des zu verwendenden Parsers, wenn Saxon selbst einen erzeugen muss, bspw. beim XSL parsen
            processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(FEATURE_SECURE_PROCESSING), true);
            processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(DISSALLOW_DOCTYPE_DECL_FEATURE), true);
            processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(LOAD_EXTERNAL_DTD_FEATURE), false);
        }
        return processor;
    }
}
