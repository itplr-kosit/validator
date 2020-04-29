package de.kosit.validationtool.impl.xml;

import java.net.URI;

import javax.xml.transform.URIResolver;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @author Andreas Penski
 */
@Slf4j
public class StrictLocalResolvingStrategy extends StrictRelativeResolvingStrategy {

    /**
     * e.g. don't allow any scheme
     */

    @Override
    public SchemaFactory createSchemaFactory() {
        final SchemaFactory schemaFactory = super.createSchemaFactory();
        allowExternalSchema(schemaFactory, "file", "jar");
        return schemaFactory;
    }

    @Override
    public URIResolver createResolver(final URI repository) {
        // intentionally return 'null', since all resolving is configured with the other objects
        return null;
    }

    @Override
    public Validator createValidator(final Schema schema) {
        final Validator validator = super.createValidator(schema);
        allowExternalSchema(validator, "file", "jar");
        return validator;
    }

}
