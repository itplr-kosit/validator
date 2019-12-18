package de.kosit.validationtool.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.api.AcceptRecommendation;
import de.kosit.validationtool.api.CheckConfiguration;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.impl.Helper.Simple;

/**
 * Prüft die Funktionen des Validator auf Basis eines reduzierten Szenarios.
 * 
 * @author Andreas Penski
 */
public class SimpleScenarioCheck {

    private DefaultCheck implementation;

    @Before
    public void setup() throws URISyntaxException {
        final CheckConfiguration d = new CheckConfiguration(Simple.SCENARIOS);
        d.setScenarioRepository(Simple.REPOSITORY);
        this.implementation = new DefaultCheck(d);
    }

    @Test
    public void testSimple() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.SIMPLE_VALID.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testInvalid() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.INVALID.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);
        assertThat(result.getSchemaViolations()).isNotEmpty();
    }

    @Test
    public void testUnknown() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.UNKNOWN.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.isProcessingSuccessful()).isFalse();
    }

    @Test
    public void testWithoutAcceptMatch() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.FOO.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.UNDEFINED);
    }

}
