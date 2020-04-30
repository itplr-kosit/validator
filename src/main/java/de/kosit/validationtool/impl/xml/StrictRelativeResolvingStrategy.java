package de.kosit.validationtool.impl.xml;

import java.io.Reader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class StrictRelativeResolvingStrategy extends BaseResolvingStrategy {

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

    /**
     * e.g. don't allow any scheme
     */
    private static final String EMPTY_SCHEME = "";

    @Override
    public SchemaFactory createSchemaFactory() {
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        disableExternalEntities(sf);
        allowExternalSchema(sf, "file");
        return sf;
    }

    @Override
    protected Processor createProcessor() {
        final Processor processor = new Processor(false);
        // verhindere global im Prinzip alle resolving strategien
        final SecureUriResolver resolver = new SecureUriResolver();
        processor.getUnderlyingConfiguration().setCollectionFinder(resolver);
        processor.getUnderlyingConfiguration().setOutputURIResolver(resolver);
        processor.getUnderlyingConfiguration().setUnparsedTextURIResolver(resolver);

        // grundsätzlich Feature-konfiguration:
        processor.setConfigurationProperty(Feature.DTD_VALIDATION, false);
        processor.setConfigurationProperty(Feature.ENTITY_RESOLVER_CLASS, "");
        processor.setConfigurationProperty(Feature.XINCLUDE, false);
        processor.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);

        // Konfiguration des zu verwendenden Parsers, wenn Saxon selbst einen erzeugen muss, bspw. beim XSL parsen
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(FEATURE_SECURE_PROCESSING), true);
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(DISSALLOW_DOCTYPE_DECL_FEATURE), true);
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(LOAD_EXTERNAL_DTD_FEATURE), false);
        return processor;
    }

    @SneakyThrows
    private static String encode(final String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8.name());
    }

    @Override
    public URIResolver createResolver(final URI repositoryURI) {
        return new RelativeUriResolver(repositoryURI);
    }

    @Override
    public Validator createValidator(final Schema schema) {
        if (schema == null) {
            throw new IllegalArgumentException("No schema supplied. Can not create validator");
        }
        forceOpenJdkXmlImplementation();
        final Validator validator = schema.newValidator();
        disableExternalEntities(validator);
        allowExternalSchema(validator, "file" /* allow nothing external */);
        return validator;

    }

}
