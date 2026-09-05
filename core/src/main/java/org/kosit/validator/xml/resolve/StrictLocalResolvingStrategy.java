package org.kosit.validator.xml.resolve;

import java.net.URI;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sf.saxon.lib.ResourceResolver;

/**
 * This is a slightly more open implementation that allows resolving artifacts from local filesystems. Your are not
 * bound to a specific 'repository'. But your validation artifacts (schema, xsl, etc.) must be available locally. This
 * implementation does not allow loading from http sources
 * 
 * @author Andreas Penski
 */
public class StrictLocalResolvingStrategy extends StrictRelativeResolvingStrategy {

    /**
     * Creates a strategy whose resolvers do not resolve within an archive repository.
     */
    public StrictLocalResolvingStrategy() {
        this(false);
    }

    /**
     * @param resolveInArchive <code>true</code> if the created resolvers may resolve within an archive repository, see
     *            {@link StrictRelativeResolvingStrategy#StrictRelativeResolvingStrategy(boolean)}
     */
    public StrictLocalResolvingStrategy(final boolean resolveInArchive) {
        super(resolveInArchive);
    }

    /**
     * Allow loading schema files from any local location.
     * 
     * @return a configured {@link SchemaFactory}
     */
    @Override
    public SchemaFactory createSchemaFactory() {
        final SchemaFactory schemaFactory = super.createSchemaFactory();
        allowExternalSchema(schemaFactory, "file");
        return schemaFactory;
    }

    /**
     * The default resolver is able to resolve locally and relative.
     * 
     * @param repository the repository is not used by this strategy
     * @return null!
     */
    @Override
    public ResourceResolver createResourceResolver(final URI repository) {
        // intentionally return 'null', since all resolving is configured with the other objects
        return null;
    }

    @Override
    public Validator createValidator(final Schema schema) {
        final Validator validator = super.createValidator(schema);
        allowExternalSchema(validator, "file");
        return validator;
    }
}
