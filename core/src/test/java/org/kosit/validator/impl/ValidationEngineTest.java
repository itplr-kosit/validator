package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.kosit.base.uri.UriHelper;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.VResult;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.conformatron.engine.SchematronValidation;
import org.kosit.validator.impl.conformatron.engine.SchematronValidation.AdHocValidationResult;

/**
 * Tests the {@link ValidationEngine} contract: {@link ConformanceValidation} (via {@link DefaultVCheck}) and
 * {@link SchematronValidation} as individual engine implementations, plus the legacy {@link VCheck} equivalence.
 */
public class ValidationEngineTest {

    private DefaultVCheck createEngine() {
        final VConfiguration config = VConfiguration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI).build(TestHelper.getTestProcessor());
        return new DefaultVCheck(new TestEngineInformation(), TestHelper.getTestProcessor(), config);
    }

    @Test
    public void testFullConformanceValidation() {
        final ValidationEngine<VResult> engine = createEngine();
        final VResult result = engine.validate(TestHelper.read(Simple.SIMPLE_VALID));

        assertThat(result).isNotNull();
        assertThat(result.isProcessingSuccessful()).isTrue();
    }

    @Test
    public void testValidateMatchesLegacyCheckInput() {
        final DefaultVCheck engine = createEngine();
        final VResult viaEngine = engine.validate(TestHelper.read(Simple.SIMPLE_VALID));
        final VResult viaLegacy = engine.checkInput(TestHelper.read(Simple.SIMPLE_VALID));

        assertThat(viaEngine.isProcessingSuccessful()).isEqualTo(viaLegacy.isProcessingSuccessful());
        assertThat(viaEngine.getAcceptRecommendation()).isEqualTo(viaLegacy.getAcceptRecommendation());
    }

    @Test
    public void testSchematronValidationIsAnEngine() {
        final ValidationEngine<AdHocValidationResult> engine = new SchematronValidation(TestHelper.getTestProcessor(),
                UriHelper.resolve(Simple.REPOSITORY_URI, "simple.sch"));

        assertThat(engine.validate(TestHelper.read(Simple.SIMPLE_VALID)).isConformant()).isTrue();
        assertThat(engine.validate(TestHelper.read(Simple.SCHEMATRON_INVALID)).isConformant()).isFalse();
    }

    @Test
    public void testAdHocConvenienceOnDefaultCheck() {
        final DefaultVCheck engine = createEngine();
        final AdHocValidationResult result = engine.validateAdHoc(TestHelper.read(Simple.SIMPLE_VALID),
                UriHelper.resolve(Simple.REPOSITORY_URI, "simple.sch"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isConformant()).isTrue();
    }
}
