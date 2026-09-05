package org.kosit.validator.xml.resolve;

import java.net.URI;
import java.util.Objects;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.kosit.base.xml.XmlHelper;

import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;

/**
 * @author Andreas Penski
 */
public class StrictRelativeResolvingStrategy extends AbstractResolvingStrategy {

    /** whether the created resolvers may resolve within an archive repository */
    private final boolean resolveInArchive;

    /**
     * Creates a strategy whose resolvers do not resolve within an archive repository.
     */
    public StrictRelativeResolvingStrategy() {
        this(false);
    }

    /**
     * @param resolveInArchive <code>true</code> if the created resolvers may resolve within an archive repository like
     *            <code>jar:file:/some.jar!/repository/</code>, see
     *            {@link RelativeUriResolver#resolve(java.net.URI, java.net.URI, boolean)}
     */
    public StrictRelativeResolvingStrategy(final boolean resolveInArchive) {
        this.resolveInArchive = resolveInArchive;
    }

    @Override
    public SchemaFactory createSchemaFactory() {
        return XmlHelper.createSafeSchemaFactory();
    }

    @Override
    public ResourceResolver createResourceResolver(final URI repositoryURI) {
        return new RelativeUriResolver(repositoryURI, this.resolveInArchive);
    }

    @Override
    public UnparsedTextURIResolver createUnparsedTextURIResolver(final URI scenarioRepository) {
        return new RelativeUriResolver(scenarioRepository, this.resolveInArchive);
    }

    @Override
    public Validator createValidator(final Schema schema) {
        Objects.requireNonNull(schema);
        XmlHelper.forceOpenJdkXmlImplementation();

        final Validator validator = schema.newValidator();
        disableExternalEntities(validator);
        allowExternalSchema(validator, "file" /* allow nothing external */);
        return validator;
    }
}
