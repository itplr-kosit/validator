package org.kosit.validator.impl.xml;

import java.net.URI;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;

/**
 * @author Andreas Penski
 */
public class StrictRelativeResolvingStrategy extends BaseResolvingStrategy {

    @Override
    public SchemaFactory createSchemaFactory() {
        forceOpenJdkXmlImplementation();
        //
        @SuppressWarnings("java:S2755")
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        disableExternalEntities(sf);
        allowExternalSchema(sf, "file");
        return sf;
    }

    @Override
    public ResourceResolver createResourceResolver(final URI repositoryURI) {
        return new RelativeUriResolver(repositoryURI);
    }

    @Override
    public UnparsedTextURIResolver createUnparsedTextURIResolver(final URI scenarioRepository) {
        return new RelativeUriResolver(scenarioRepository);
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

    public StrictRelativeResolvingStrategy() {
    }
}
