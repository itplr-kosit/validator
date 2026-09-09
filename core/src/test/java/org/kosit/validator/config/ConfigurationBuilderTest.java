package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kosit.validator.config.ConfigurationBuilder.schematron;
import static org.kosit.validator.config.TestConfigurationFactory.createSimpleConfiguration;

import java.net.URI;
import java.time.LocalDate;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.kosit.validator.TestHelper;
import org.kosit.validator.api.ScenarioSet;

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
    public void testNoSchemaIsAllowed() {
        // a scenario may validate with Schematron alone
        final ConfigurationBuilder builder = createSimpleConfiguration();
        builder.getScenarios().get(0).validate((SchemaBuilder) null);
        final ScenarioSet set = builder.build(TestHelper.getTestProcessor());
        assertThat(set.getScenarios().get(0).getConfiguration().getValidateWithXmlSchema()).isNull();
        assertThat(set.getScenarios().get(0).getConfiguration().getValidateWithSchematron()).isNotEmpty();
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
    public void testIdentity() {
        final ScenarioSet set = createSimpleConfiguration().build(TestHelper.getTestProcessor());
        assertThat(set.getName()).isEqualTo("Simple-API");
        assertThat(set.getAuthor()).isEqualTo("me");
        // assembled in code, read from no file
        assertThat(set.getDefinitionFile()).isNull();
        assertThat(set.getScenarios()).hasSize(1);
        assertThat(set.getScenarios().get(0).getDefinitionFile()).isNull();
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
