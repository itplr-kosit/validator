package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.AcceptRecommendation;
import org.kosit.validator.api.Configuration;
import org.kosit.validator.api.InputFactory;
import org.kosit.validator.api.Result;
import org.kosit.validator.impl.Helper.Simple;

/**
 * Tests the validator functionality based on a reduced scenario.
 *
 * @author Andreas Penski
 */
public class SimpleScenarioCheckTest {

    private DefaultCheck implementation;

    @BeforeEach
    public void setup() {
        final Configuration d = Configuration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI).build(Helper.getTestProcessor());
        this.implementation = new DefaultCheck(new TestEngineInformation(), d);
    }

    @Test
    public void testSimple() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.SIMPLE_VALID.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testInvalid() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.SCHEMA_INVALID.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);
        assertThat(result.getSchemaViolations()).isNotEmpty();
    }

    @Test
    public void testUnknown() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.UNKNOWN.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.isProcessingSuccessful()).isTrue();
        assertThat(result.isAcceptable()).isFalse();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);

    }

    @Test
    public void testWithoutAcceptMatch() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.FOO.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

}
