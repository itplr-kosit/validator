package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.config.ConfigurationBuilder.schema;

import java.nio.file.Paths;

import javax.xml.validation.Schema;

import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.scenario.v1.ResourceType;

/**
 * Tests {@link SchemaBuilder}.
 * 
 * @author Andreas Penski
 */
public class SchemaBuilderTest {

    @Test
    public void testBuildSchema() {
        final SchemaBuilder builder = schema(Simple.SCHEMA);
        final var result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testNoConfiguration() {
        final SchemaBuilder builder = schema("no-config");
        final var result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void testBuildNamedSchema() {
        final SchemaBuilder builder = schema("myname").schemaLocation(Simple.SCHEMA);
        final var result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getObject().validationResult().getResource().stream().map(ResourceType::getName)).contains("myname");
    }

    @Test
    public void testInvalidSchema() {
        final SchemaBuilder builder = schema("myname").schemaLocation(Simple.SCHEMA_INVALID);
        final var result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void testNonExisting() {
        final SchemaBuilder builder = schema("myname").schemaLocation(Simple.REPOSITORY_URI.resolve("doesNotExist.xsd"));
        final var result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void testPath() {
        final SchemaBuilder builder = schema("myname").schemaLocation(Paths.get(Simple.SCHEMA));
        final var result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testStringLocation() {
        final SchemaBuilder builder = schema("myname").schemaLocation("simple.xsd");
        final var result = builder.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testPrecompiled() {
        final ContentRepository repository = Simple.createContentRepository();
        final Schema schema = repository.createSchema(Simple.SCHEMA);

        final SchemaBuilder builder = schema("myname").schema(schema);
        final var result = builder.build(repository);
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }
}
