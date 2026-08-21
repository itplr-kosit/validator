package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.api.InputFactory.read;

import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VCheck;
import org.kosit.validator.api.Configuration;
import org.kosit.validator.api.Result;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.conformatron.engine.SchematronValidation;
import org.kosit.validator.impl.conformatron.engine.SchematronValidation.AdHocValidationResult;

/**
 * Tests the {@link ValidationEngine} contract: {@link ConformanceValidation} (via {@link DefaultVCheck}) and
 * {@link SchematronValidation} as individual engine implementations, plus the legacy {@link VCheck} equivalence.
 */
public class ValidationEngineTest {

    private DefaultVCheck createEngine() {
        final Configuration config = Configuration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI).build(Helper.getTestProcessor());
        return new DefaultVCheck(new TestEngineInformation(), Helper.getTestProcessor(), config);
    }

    @Test
    public void testFullConformanceValidation() {
        final ValidationEngine<Result> engine = createEngine();
        final Result result = engine.validate(read(Simple.SIMPLE_VALID));

        assertThat(result).isNotNull();
        assertThat(result.isProcessingSuccessful()).isTrue();
    }

    @Test
    public void testValidateMatchesLegacyCheckInput() {
        final DefaultVCheck engine = createEngine();
        final Result viaEngine = engine.validate(read(Simple.SIMPLE_VALID));
        final Result viaLegacy = engine.checkInput(read(Simple.SIMPLE_VALID));

        assertThat(viaEngine.isProcessingSuccessful()).isEqualTo(viaLegacy.isProcessingSuccessful());
        assertThat(viaEngine.getAcceptRecommendation()).isEqualTo(viaLegacy.getAcceptRecommendation());
    }

    @Test
    public void testSchematronValidationIsAnEngine() {
        final ValidationEngine<AdHocValidationResult> engine = new SchematronValidation(Helper.getTestProcessor(),
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
