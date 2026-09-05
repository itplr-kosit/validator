package org.kosit.validator.xml.resolve;

import javax.xml.validation.SchemaFactory;

public class RemoteResolvingStrategy extends StrictLocalResolvingStrategy {

    /**
     * Creates a strategy whose resolvers do not resolve within an archive repository.
     */
    public RemoteResolvingStrategy() {
        this(false);
    }

    /**
     * @param resolveInArchive <code>true</code> if the created resolvers may resolve within an archive repository, see
     *            {@link StrictRelativeResolvingStrategy#StrictRelativeResolvingStrategy(boolean)}
     */
    public RemoteResolvingStrategy(final boolean resolveInArchive) {
        super(resolveInArchive);
    }

    @Override
    public SchemaFactory createSchemaFactory() {
        final SchemaFactory schemaFactory = super.createSchemaFactory();
        allowExternalSchema(schemaFactory, "https,http,file");
        return schemaFactory;
    }
}
