/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
