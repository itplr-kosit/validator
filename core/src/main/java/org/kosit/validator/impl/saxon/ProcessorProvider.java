package org.kosit.validator.impl.saxon;

import java.io.Reader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;

import org.kosit.base.xml.XMLHelper;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.trans.XPathException;

/**
 * @author Andreas Penski
 */
public class ProcessorProvider {

    private static class SecureUriResolver implements CollectionFinder, ResourceResolver, UnparsedTextURIResolver {

        public static final String MESSAGE = "Configuration error. Resolving is not allowed";

        @Override
        public Source resolve(final ResourceRequest request) throws XPathException {
            throw new IllegalStateException(MESSAGE);
        }

        @Override
        public Reader resolve(final URI absoluteURI, final String encoding, final Configuration config) throws XPathException {
            throw new IllegalStateException(MESSAGE);
        }

        @Override
        public ResourceCollection findCollection(final XPathContext context, final String collectionURI) {
            throw new IllegalStateException(MESSAGE);
        }
    }

    private static Processor processor;

    public static Processor getProcessor() {
        if (processor == null) {
            processor = createProcessor();
        }
        return processor;
    }

    private static final Processor createProcessor() {
        final Processor processor = new Processor(false);
        // globally disable basically all resolving strategies
        final SecureUriResolver resolver = new SecureUriResolver();
        processor.getUnderlyingConfiguration().setCollectionFinder(resolver);
        processor.getUnderlyingConfiguration().setResourceResolver(resolver);
        processor.getUnderlyingConfiguration().setUnparsedTextURIResolver(resolver);

        // basic feature configuration:
        processor.setConfigurationProperty(Feature.DTD_VALIDATION, false);
        processor.setConfigurationProperty(Feature.ENTITY_RESOLVER_CLASS, "");
        processor.setConfigurationProperty(Feature.XINCLUDE, false);
        processor.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);

        // configuration of the parser to be used when Saxon itself has to create one, e.g. when parsing XSL
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(XMLConstants.FEATURE_SECURE_PROCESSING), true);
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(XMLHelper.DISALLOW_DOCTYPE_DECL_FEATURE), true);
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(XMLHelper.LOAD_EXTERNAL_DTD_FEATURE), false);
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(XMLConstants.ACCESS_EXTERNAL_DTD), false);
        return processor;
    }

    private static String encode(final String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    private ProcessorProvider() {
    }

}
