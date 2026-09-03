package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.VResult;
import org.kosit.validator.api.xvrl.compact.AcceptRecommendation;
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
        final VConfiguration d = VConfiguration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
        this.implementation = new DefaultVCheck(new TestEngineInformation(), d);
    }

    @Test
    public void testSimple() {
        final VResult result = this.implementation.checkInput(TestHelper.read(Simple.SIMPLE_VALID));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testInvalid() {
        final VResult result = this.implementation.checkInput(TestHelper.read(Simple.SCHEMA_INVALID));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);
        assertThat(result.getSchemaViolations()).isNotEmpty();
    }

    @Test
    public void testUnknown() {
        final VResult result = this.implementation.checkInput(TestHelper.read(Simple.UNKNOWN));
        assertThat(result).isNotNull();
        assertThat(result.isProcessingSuccessful()).isTrue();
        assertThat(result.isAcceptable()).isFalse();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);

    }

    @Test
    public void testWithoutAcceptMatch() {
        final VResult result = this.implementation.checkInput(TestHelper.read(Simple.FOO));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

}
