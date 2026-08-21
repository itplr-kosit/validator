package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.api.VInputFactory.read;

import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VResult;
import org.kosit.validator.api.VCheck;
import org.kosit.validator.api.VConfiguration;
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
        final VResult result = engine.validate(read(Simple.SIMPLE_VALID));

        assertThat(result).isNotNull();
        assertThat(result.isProcessingSuccessful()).isTrue();
    }

    @Test
    public void testValidateMatchesLegacyCheckInput() {
        final DefaultVCheck engine = createEngine();
        final VResult viaEngine = engine.validate(read(Simple.SIMPLE_VALID));
        final VResult viaLegacy = engine.checkInput(read(Simple.SIMPLE_VALID));

        assertThat(viaEngine.isProcessingSuccessful()).isEqualTo(viaLegacy.isProcessingSuccessful());
        assertThat(viaEngine.getAcceptRecommendation()).isEqualTo(viaLegacy.getAcceptRecommendation());
    }

    @Test
    public void testSchematronValidationIsAnEngine() {
        final ValidationEngine<AdHocValidationResult> engine = new SchematronValidation(TestHelper.getTestProcessor(),
                Simple.REPOSITORY_URI.resolve("simple.sch"));

        assertThat(engine.validate(read(Simple.SIMPLE_VALID)).isConformant()).isTrue();
        assertThat(engine.validate(read(Simple.SCHEMATRON_INVALID)).isConformant()).isFalse();
    }

    @Test
    public void testAdHocConvenienceOnDefaultCheck() {
        final DefaultVCheck engine = createEngine();
        final AdHocValidationResult result = engine.validateAdHoc(read(Simple.SIMPLE_VALID), Simple.REPOSITORY_URI.resolve("simple.sch"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isConformant()).isTrue();
    }
}
