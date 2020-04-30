package de.kosit.validationtool.config;

import static de.kosit.validationtool.config.SimpleConfigTest.createScenarioConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.model.Result;

/**
 * Test {@link ScenarioBuilder}.
 * 
 * @author Andreas Penski
 */
public class ScenarioBuilderTest {

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @Test
    public void simpleValid() {
        final Result<Scenario, String> result = createScenarioConfiguration().build(Simple.createContentRepository());
        assertThat(result.isValid()).isTrue();
        assertThat(result.getObject().getConfiguration()).isNotNull();
    }

    @Test
    public void testNoSchema() {
        final ScenarioBuilder builder = createScenarioConfiguration();
        builder.validate((SchemaBuilder) null);
        final Result<Scenario, String> result = builder.build(Simple.createContentRepository());
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("schema"));
    }

    @Test
    public void testNoMatch() {
        final ScenarioBuilder builder = createScenarioConfiguration();
        builder.match((String) null);
        final Result<Scenario, String> result = builder.build(Simple.createContentRepository());
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("match"));
    }

    @Test
    public void testInvalidMatch() {
        final ScenarioBuilder builder = createScenarioConfiguration();
        builder.match("/////");
        final Result<Scenario, String> result = builder.build(Simple.createContentRepository());
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("match"));
    }

    @Test
    public void testNoAccept() {
        final ScenarioBuilder builder = createScenarioConfiguration();
        builder.acceptWith((String) null);
        final Result<Scenario, String> result = builder.build(Simple.createContentRepository());
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testInvalidAccept() {
        final ScenarioBuilder builder = createScenarioConfiguration();
        builder.acceptWith("/////");
        final Result<Scenario, String> result = builder.build(Simple.createContentRepository());
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("accept"));
    }
}
