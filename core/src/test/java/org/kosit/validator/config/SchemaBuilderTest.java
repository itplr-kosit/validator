package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.config.ConfigurationBuilder.schema;

import java.nio.file.Paths;

import javax.xml.validation.Schema;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.model.scenarios.ResourceType;
import org.kosit.validator.model.scenarios.ValidateWithXmlSchema;

/**
 * Tests {@link SchemaBuilder}.
 * 
 * @author Andreas Penski
 */
public class SchemaBuilderTest {

    @Test
    public void testBuildSchema() {
        final SchemaBuilder builder = schema(Simple.SCHEMA);
        final Result<Pair<ValidateWithXmlSchema, Schema>, String> result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testNoConfiguration() {
        final SchemaBuilder builder = schema("no-config");
        final Result<Pair<ValidateWithXmlSchema, Schema>, String> result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void testBuildNamedSchema() {
        final SchemaBuilder builder = schema("myname").schemaLocation(Simple.SCHEMA);
        final Result<Pair<ValidateWithXmlSchema, Schema>, String> result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getObject().getKey().getResource().stream().map(ResourceType::getName)).contains("myname");
    }

    @Test
    public void testInvalidSchema() {
        final SchemaBuilder builder = schema("myname").schemaLocation(Simple.SCHEMA_INVALID);
        final Result<Pair<ValidateWithXmlSchema, Schema>, String> result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void testNonExisting() {
        final SchemaBuilder builder = schema("myname").schemaLocation(Simple.REPOSITORY_URI.resolve("doesNotExist.xsd"));
        final Result<Pair<ValidateWithXmlSchema, Schema>, String> result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void testPath() {
        final SchemaBuilder builder = schema("myname").schemaLocation(Paths.get(Simple.SCHEMA));
        final Result<Pair<ValidateWithXmlSchema, Schema>, String> result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testStringLocation() {
        final SchemaBuilder builder = schema("myname").schemaLocation("simple.xsd");
        final Result<Pair<ValidateWithXmlSchema, Schema>, String> result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testPrecompiled() {
        final ContentRepository repository = Simple.createContentRepository();
        final Schema schema = repository.createSchema(Simple.SCHEMA);

        final SchemaBuilder builder = schema("myname").schema(schema);
        final Result<Pair<ValidateWithXmlSchema, Schema>, String> result = builder.build(repository);
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }
}
