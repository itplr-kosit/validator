package org.kosit.validator.config;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;

import javax.xml.validation.Schema;

import org.kosit.base.string.StringHelper;
import org.kosit.validator.config.SchemaBuilder.SchemaParseResult;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.scenario.v1.ResourceType;
import org.kosit.validator.scenario.v1.ValidateWithXmlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for Schema validation configuration.
 * 
 * @author Andreas Penski
 */
public class SchemaBuilder implements SingleProcessingResultBuilder<SchemaParseResult> {

    public static record SchemaParseResult(ValidateWithXmlSchema validationResult, Schema schema) {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaBuilder.class);

    private static final String DEFAULT_NAME = "manually configured";

    private Schema schema;

    private URI schemaLocation;

    private String name;

    private static SingleProcessingResult<SchemaParseResult, String> createError(final String msg) {
        return new SingleProcessingResult<>(null, Collections.singletonList(msg));
    }

    public static SchemaBuilder schema() {
        return new SchemaBuilder();
    }

    SchemaBuilder() {
    }

    @Override
    public SingleProcessingResult<SchemaParseResult, String> build(final ContentRepository repository) {
        if (this.schema == null && this.schemaLocation == null) {
            return createError("Must supply source location and/or executable for schema '" + this.name + "'");
        }
        SingleProcessingResult<SchemaParseResult, String> result;
        try {
            if (this.schema == null) {
                this.schema = repository.createSchema(this.schemaLocation);
            }
            result = new SingleProcessingResult<>(new SchemaParseResult(createObject(), this.schema));
        } catch (final IllegalStateException e) {
            LOGGER.error(e.getMessage(), e);
            result = createError("Can not create schema based " + this.schemaLocation + ". Exception is " + e.getMessage());
        }
        return result;
    }

    private ValidateWithXmlSchema createObject() {
        final ValidateWithXmlSchema o = new ValidateWithXmlSchema();
        final ResourceType r = new ResourceType();
        r.setName(StringHelper.isNotEmpty(this.name) ? this.name : DEFAULT_NAME);
        r.setLocation(this.schemaLocation != null ? this.schemaLocation.toASCIIString() : "manually configured");
        o.getResource().add(r);
        return o;
    }

    /**
     * Set a specific precompiled schema to check.
     * 
     * @param schema the {@link Schema}
     * @return this
     */
    public SchemaBuilder schema(final Schema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Set a specific schema location either to compile or to document the precompiled one .
     * 
     * @param schemaLocation the schema location as uri
     * @return this
     */
    public SchemaBuilder schemaLocation(final URI schemaLocation) {
        this.schemaLocation = schemaLocation;
        return this;
    }

    /**
     * Set a specific schema location either to compile or to document the precompiled one .
     *
     * @param schemaLocation the schema location as uri
     * @return this
     */
    public SchemaBuilder schemaLocation(final String schemaLocation) {
        return schemaLocation(URI.create(schemaLocation));
    }

    /**
     * Set a specific schema location either to compile or to document the precompiled one .
     *
     * @param schemaLocation the schema location as uri
     * @return this
     */
    public SchemaBuilder schemaLocation(final Path schemaLocation) {
        return schemaLocation(schemaLocation.toUri());
    }

    /**
     * Set a specific name to identify this schema.
     * 
     * @param name the name of the schema
     * @return this
     */
    public SchemaBuilder name(final String name) {
        this.name = name;
        return this;
    }

    Schema getSchema() {
        return this.schema;
    }

    URI getSchemaLocation() {
        return this.schemaLocation;
    }

    String getName() {
        return this.name;
    }
}
