package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kosit.validator.config.ConfigurationBuilder.report;
import static org.kosit.validator.config.ConfigurationBuilder.schematron;
import static org.kosit.validator.config.TestConfigurationFactory.createSimpleConfiguration;

import java.net.URI;
import java.time.LocalDate;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.TestHelper;

/**
 * Test {@link ConfigurationBuilder}.
 *
 * @author Andreas Penski
 */
public class ConfigurationBuilderTest {

    public static final LocalDate EPOCH = LocalDate.of(1970, 1, 1);

    @Test
    public void testNoConfiguration() {
        assertThrows(IllegalStateException.class, () -> new ConfigurationBuilder().build(TestHelper.getTestProcessor()));
    }

    @Test
    public void testNoFallback() {
        final ConfigurationBuilder builder = createSimpleConfiguration();
        builder.with((FallbackBuilder) null);
        final Throwable t = assertThrows(IllegalStateException.class, () -> builder.build(TestHelper.getTestProcessor()));
        assertThat(t.getMessage()).contains("fallback");
    }

    @Test
    public void testNoSchema() {
        final ConfigurationBuilder builder = createSimpleConfiguration();
        builder.getScenarios().get(0).validate((SchemaBuilder) null);
        final Throwable t = assertThrows(IllegalStateException.class, () -> builder.build(TestHelper.getTestProcessor()));
        assertThat(t.getMessage()).contains("schema");
    }

    @Test
    public void testInvalidSchematron() {
        final ConfigurationBuilder builder = createSimpleConfiguration();
        builder.getScenarios().get(0).validate(schematron("invalid").source(URI.create("DoesNotExist")));
        final Throwable t = assertThrows(IllegalStateException.class, () -> builder.build(TestHelper.getTestProcessor()));
        assertThat(t.getMessage()).contains("schematron");
    }

    @Test
    public void testInsufficientSchematron() {
        final ConfigurationBuilder builder = createSimpleConfiguration();
        builder.getScenarios().get(0).validate(schematron("invalid"));
        final Throwable t = assertThrows(IllegalStateException.class, () -> builder.build(TestHelper.getTestProcessor()));
        assertThat(t.getMessage()).contains("schematron");
    }

    @Test
    public void testNoReport() {
        final ConfigurationBuilder builder = createSimpleConfiguration();
        builder.getScenarios().get(0).with(report("invalid"));
        final Throwable t = assertThrows(IllegalStateException.class, () -> builder.build(TestHelper.getTestProcessor()));
        assertThat(t.getMessage()).contains("report");
    }

    @Test
    public void testDate() {
        assertThat(createSimpleConfiguration().date(EPOCH).build(TestHelper.getTestProcessor()).getDate()).isEqualTo("1970-01-01");
        assertThat(createSimpleConfiguration().date(new Date(EPOCH.toEpochDay())).build(TestHelper.getTestProcessor()).getDate())
                .isEqualTo("1970-01-01");
        assertThat(createSimpleConfiguration().date((Date) null).build(TestHelper.getTestProcessor()).getDate())
                .isEqualTo(LocalDate.now().toString());
        assertThat(createSimpleConfiguration().date((LocalDate) null).build(TestHelper.getTestProcessor()).getDate())
                .isEqualTo(LocalDate.now().toString());
    }

}
