/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.time.LocalDate;
import java.util.Date;

import org.junit.Test;

import de.kosit.validationtool.impl.Helper;

/**
 * Test {@link ConfigurationBuilder}.
 * 
 * @author Andreas Penski
 */
public class ConfigurationBuilderTest {

    public static final LocalDate EPOCH = LocalDate.of(1970, 1, 1);

    @Test(expected = IllegalStateException.class)
    public void testNoConfiguration() {
        new ConfigurationBuilder().build(Helper.getTestProcessor());
    }

    @Test
    public void testNoFallback() {
        try {
            final ConfigurationBuilder builder = createSimpleConfiguration();
            builder.with((FallbackBuilder) null);
            builder.build(Helper.getTestProcessor());
            fail();
        } catch (final IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("fallback"));
        }
    }

    @Test
    public void testNoSchema() {
        try {
            final ConfigurationBuilder builder = createSimpleConfiguration();
            builder.getScenarios().get(0).validate((SchemaBuilder) null);
            builder.build(Helper.getTestProcessor());
            fail();
        } catch (final IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("schema"));
        }
    }

    @Test
    public void testInvalidSchematron() {
        try {
            final ConfigurationBuilder builder = createSimpleConfiguration();
            builder.getScenarios().get(0).validate(schematron("invalid").source(URI.create("DoesNotExist")));
            builder.build(Helper.getTestProcessor());
            fail();
        } catch (final IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("schematron"));
        }
    }

    @Test
    public void testInsufficientSchematron() {
        try {
            final ConfigurationBuilder builder = createSimpleConfiguration();
            builder.getScenarios().get(0).validate(schematron("invalid"));
            builder.build(Helper.getTestProcessor());
            fail();
        } catch (final IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("schematron"));
        }
    }

    @Test
    public void testNoReport() {
        try {
            final ConfigurationBuilder builder = createSimpleConfiguration();
            builder.getScenarios().get(0).with(report("invalid"));
            builder.build(Helper.getTestProcessor());
            fail();
        } catch (final IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("report"));
        }
    }

    @Test
    public void testDate() {
        assertThat(createSimpleConfiguration().date(EPOCH).build(Helper.getTestProcessor()).getDate()).isEqualTo("1970-01-01");
        assertThat(createSimpleConfiguration().date(new Date(EPOCH.toEpochDay())).build(Helper.getTestProcessor()).getDate())
                .isEqualTo("1970-01-01");
        assertThat(createSimpleConfiguration().date((Date) null).build(Helper.getTestProcessor()).getDate())
                .isEqualTo(LocalDate.now().toString());
        assertThat(createSimpleConfiguration().date((LocalDate) null).build(Helper.getTestProcessor()).getDate())
                .isEqualTo(LocalDate.now().toString());
    }

}
