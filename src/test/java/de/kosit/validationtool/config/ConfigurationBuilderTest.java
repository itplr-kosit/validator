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

import static de.kosit.validationtool.config.ConfigurationBuilder.report;
import static de.kosit.validationtool.config.ConfigurationBuilder.schematron;
import static de.kosit.validationtool.config.TestConfigurationFactory.createSimpleConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.LocalDate;
import java.util.Date;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test {@link ConfigurationBuilder}.
 * 
 * @author Andreas Penski
 */
public class ConfigurationBuilderTest {

    public static final LocalDate EPOCH = LocalDate.of(1970, 1, 1);

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @Test
    public void testNoConfiguration() {
        this.exceptions.expect(IllegalStateException.class);
        new ConfigurationBuilder().build();
    }

    @Test
    public void testNoFallback() {
        this.exceptions.expect(IllegalStateException.class);
        this.exceptions.expectMessage(Matchers.containsString("fallback"));
        final ConfigurationBuilder builder = createSimpleConfiguration();
        builder.with((FallbackBuilder) null);
        builder.build();
    }

    @Test
    public void testNoSchema() {
        this.exceptions.expect(IllegalStateException.class);
        this.exceptions.expectMessage(Matchers.containsString("schema"));
        final ConfigurationBuilder builder = createSimpleConfiguration();
        builder.getScenarios().get(0).validate((SchemaBuilder) null);
        builder.build();
    }

    @Test
    public void testInvalidSchematron() {
        this.exceptions.expect(IllegalStateException.class);
        this.exceptions.expectMessage(Matchers.containsString("schematron"));
        final ConfigurationBuilder builder = createSimpleConfiguration();
        builder.getScenarios().get(0).validate(schematron("invalid").source(URI.create("DoesNotExist")));
        builder.build();
    }

    @Test
    public void testInsufficientSchematron() {
        this.exceptions.expect(IllegalStateException.class);
        this.exceptions.expectMessage(Matchers.containsString("schematron"));
        final ConfigurationBuilder builder = createSimpleConfiguration();
        builder.getScenarios().get(0).validate(schematron("invalid"));
        builder.build();
    }

    @Test
    public void testNoReport() {
        this.exceptions.expect(IllegalStateException.class);
        this.exceptions.expectMessage(Matchers.containsString("report"));
        final ConfigurationBuilder builder = createSimpleConfiguration();
        builder.getScenarios().get(0).with(report("invalid"));
        builder.build();
    }

    @Test
    public void testDate() {
        assertThat(createSimpleConfiguration().date(EPOCH).build().getDate()).isEqualTo("1970-01-01");
        assertThat(createSimpleConfiguration().date(new Date(EPOCH.toEpochDay())).build().getDate()).isEqualTo("1970-01-01");
        assertThat(createSimpleConfiguration().date((Date) null).build().getDate()).isEqualTo(LocalDate.now().toString());
        assertThat(createSimpleConfiguration().date((LocalDate) null).build().getDate()).isEqualTo(LocalDate.now().toString());
    }

}
