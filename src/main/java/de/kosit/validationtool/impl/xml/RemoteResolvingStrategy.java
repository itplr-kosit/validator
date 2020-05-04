package de.kosit.validationtool.impl.xml;

import javax.xml.validation.SchemaFactory;

public class RemoteResolvingStrategy extends StrictLocalResolvingStrategy {

    @Override
    public SchemaFactory createSchemaFactory() {
        final SchemaFactory schemaFactory = super.createSchemaFactory();
        allowExternalSchema(schemaFactory, "https,http,file");
        return schemaFactory;
    }
}
