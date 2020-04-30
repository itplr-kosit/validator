package de.kosit.validationtool.config;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;

import javax.xml.validation.Schema;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.scenarios.ResourceType;
import de.kosit.validationtool.model.scenarios.ValidateWithXmlSchema;

/**
 * Builder for Schema validation configuration.
 * 
 * @author Andreas Penski
 */
@Slf4j
public class SchemaBuilder implements Builder<Pair<ValidateWithXmlSchema, Schema>> {

    private static final String DEFAULT_NAME = "manually configured";

    private Schema schema;

    private URI schemaLocation;

    private String name;

    @Override
    public Result<Pair<ValidateWithXmlSchema, Schema>, String> build(final ContentRepository repository) {
        if (this.schema == null && this.schemaLocation == null) {
            return createError(String.format("Must supply source location and/or executable for schema '%s'", this.name));
        }
        Result<Pair<ValidateWithXmlSchema, Schema>, String> result;
        try {
            if (this.schema == null) {
                this.schema = repository.createSchema(this.schemaLocation);
            }
            result = new Result<>(new ImmutablePair<>(createObject(), this.schema));
        } catch (final IllegalStateException e) {
            log.error(e.getMessage(), e);
            result = createError(String.format("Can not create schema based %s. Exception is %s", this.schemaLocation, e.getMessage()));
        }

        return result;
    }

    private ValidateWithXmlSchema createObject() {
        final ValidateWithXmlSchema o = new ValidateWithXmlSchema();
        final ResourceType r = new ResourceType();
        r.setName(isNotEmpty(this.name) ? this.name : DEFAULT_NAME);
        r.setLocation(this.schemaLocation.toASCIIString());
        o.getResource().add(r);
        return o;
    }

    private static Result<Pair<ValidateWithXmlSchema, Schema>, String> createError(final String msg) {
        return new Result<>(null, Collections.singletonList(msg));
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
}
