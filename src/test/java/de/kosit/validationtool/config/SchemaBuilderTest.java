package de.kosit.validationtool.config;

import static de.kosit.validationtool.config.ConfigurationBuilder.schema;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import javax.xml.validation.Schema;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.scenarios.ResourceType;
import de.kosit.validationtool.model.scenarios.ValidateWithXmlSchema;

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
        assertThat(result.getObject().getKey().getResource().stream().map(ResourceType::getName).findFirst().get()).isEqualTo("myname");
    }

    @Test
    public void testInvalidSchema() {
        final SchemaBuilder builder = schema("myname").schemaLocation(Simple.INVALID);
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
