package org.kost.validator.api.saxon;

import java.io.Reader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;

import org.kosit.base.xml.XmlHelper;

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
 * The one Saxon {@link Processor} of this runtime, configured so that a stylesheet or XPath expression can not reach
 * outside the document it is given: no external functions, no DTDs, no XInclude, and every resolving strategy Saxon
 * offers replaced by one that refuses.
 * <p>
 * The instance is built in the class initializer and handed out as is. That matters for more than tidiness: the
 * processor is shared by every thread of a server, and it is only secure once {@link #createProcessor()} has run to its
 * last line. A lazily assigned, non-volatile field would let one thread publish the reference before the hardening
 * behind it becomes visible to another, which is the textbook unsafe publication - a thread could then work with a
 * processor on which {@code ALLOW_EXTERNAL_FUNCTIONS} is still at its default. Class initialization gives that
 * guarantee for free and needs no lock on the hot path.
 * </p>
 * <p>
 * A processor that can not be configured is a fatal condition of the runtime, not of a single call, so a failure in the
 * initializer surfaces as an {@link ExceptionInInitializerError} when the class is first used.
 * </p>
 *
 * @author Andreas Penski
 */
public class ProcessorProvider {

    public static class SecureUriResolver implements CollectionFinder, OutputURIResolver, UnparsedTextURIResolver {

        public static final String MESSAGE = "Configuration error. Resolving is not allowed";

        private SecureUriResolver() {
        }

        @Override
        public Reader resolve(final URI absoluteURI, final String encoding, final Configuration config) throws XPathException {
            throw new IllegalStateException(MESSAGE);
        }

        @Override
        public ResourceCollection findCollection(final XPathContext context, final String collectionURI) {
            throw new IllegalStateException(MESSAGE);
        }

        public OutputURIResolver newInstance() {
            throw new IllegalStateException(MESSAGE);
        }

        public Result resolve(final String href, final String base) throws TransformerException {
            throw new IllegalStateException(MESSAGE);
        }

        public void close(final Result result) throws TransformerException {
            throw new IllegalStateException(MESSAGE);
        }
    }

    private static final Processor PROCESSOR = createProcessor();

    /**
     * @return the shared, hardened Saxon processor; the same instance for every caller and every thread
     */
    public static Processor getProcessor() {
        return PROCESSOR;
    }

    private static Processor createProcessor() {
        final Processor processor = new Processor(false);

        // globally disable basically all resolving strategies
        {
            final SecureUriResolver resolver = new SecureUriResolver();
            processor.getUnderlyingConfiguration().setCollectionFinder(resolver);
            processor.getUnderlyingConfiguration().getDefaultXsltCompilerInfo().setOutputURIResolver(resolver);
            processor.getUnderlyingConfiguration().setUnparsedTextURIResolver(resolver);
        }

        // basic feature configuration:
        processor.setConfigurationProperty(Feature.DTD_VALIDATION, Boolean.FALSE);
        processor.setConfigurationProperty(Feature.ENTITY_RESOLVER_CLASS, "");
        processor.setConfigurationProperty(Feature.XINCLUDE, Boolean.FALSE);
        processor.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, Boolean.FALSE);

        // configuration of the parser to be used when Saxon itself has to create one, e.g. when parsing XSL
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(XMLConstants.FEATURE_SECURE_PROCESSING), Boolean.TRUE);
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(XmlHelper.DISALLOW_DOCTYPE_DECL_FEATURE), Boolean.TRUE);
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(XmlHelper.LOAD_EXTERNAL_DTD_FEATURE), Boolean.FALSE);
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(XMLConstants.ACCESS_EXTERNAL_DTD), Boolean.FALSE);
        return processor;
    }

    private static String encode(final String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    private ProcessorProvider() {
    }

}
