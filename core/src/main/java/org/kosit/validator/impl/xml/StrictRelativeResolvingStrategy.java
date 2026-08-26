package org.kosit.validator.impl.xml;

import java.net.URI;
import java.util.Objects;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.kosit.jaxb.xml.XMLHelper;

import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;

/**
 * @author Andreas Penski
 */
public class StrictRelativeResolvingStrategy extends BaseResolvingStrategy {

    public StrictRelativeResolvingStrategy() {
    }

    @Override
    public SchemaFactory createSchemaFactory() {
        return XMLHelper.createSafeSchemaFactory();
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
        Objects.requireNonNull(schema);
        XMLHelper.forceOpenJdkXmlImplementation();

        final Validator validator = schema.newValidator();
        disableExternalEntities(validator);
        allowExternalSchema(validator, "file" /* allow nothing external */);
        return validator;
    }
}
