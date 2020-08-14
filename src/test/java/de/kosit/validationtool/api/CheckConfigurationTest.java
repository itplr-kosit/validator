package de.kosit.validationtool.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import de.kosit.validationtool.impl.Helper.Simple;

/**
 * Test {@link CheckConfiguration }.
 * 
 * @author Andreas Penski
 */
@Deprecated
public class CheckConfigurationTest {

    @Test
    public void testDelegation() {
        final CheckConfiguration config = new CheckConfiguration(Simple.SCENARIOS);
        config.setScenarioRepository(Simple.REPOSITORY_URI);
        assertThat(config.getScenarios()).isNotEmpty();
        assertThat(config.getContentRepository()).isNotNull();
        assertThat(config.getFallbackScenario()).isNotNull();
        assertThat(config.getAuthor()).isNotEmpty();
        assertThat(config.getDate()).isNotEmpty();
        assertThat(config.getName()).isNotEmpty();
        assertThat(config.getScenarioRepository()).isNotNull();
    }
}
