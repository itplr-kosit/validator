package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.AcceptRecommendation;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.VInputFactory;
import org.kosit.validator.api.VResult;
import org.kosit.validator.impl.TestHelper.Simple;

/**
 * Tests the validator functionality based on a reduced scenario.
 *
 * @author Andreas Penski
 */
public class SimpleScenarioVCheckTest {

    private DefaultVCheck implementation;

    @BeforeEach
    public void setup() {
        final VConfiguration d = VConfiguration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI).build(TestHelper.getTestProcessor());
        this.implementation = new DefaultVCheck(new TestEngineInformation(), d);
    }

    @Test
    public void testSimple() throws MalformedURLException {
        final VResult result = this.implementation.checkInput(VInputFactory.read(Simple.SIMPLE_VALID.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testInvalid() throws MalformedURLException {
        final VResult result = this.implementation.checkInput(VInputFactory.read(Simple.SCHEMA_INVALID.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);
        assertThat(result.getSchemaViolations()).isNotEmpty();
    }

    @Test
    public void testUnknown() throws MalformedURLException {
        final VResult result = this.implementation.checkInput(VInputFactory.read(Simple.UNKNOWN.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.isProcessingSuccessful()).isTrue();
        assertThat(result.isAcceptable()).isFalse();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);

    }

    @Test
    public void testWithoutAcceptMatch() throws MalformedURLException {
        final VResult result = this.implementation.checkInput(VInputFactory.read(Simple.FOO.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

}
