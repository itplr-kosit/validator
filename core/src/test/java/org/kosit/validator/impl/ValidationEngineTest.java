package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
        final VConfiguration config = VConfiguration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
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
                UriHelper.resolve(Simple.REPOSITORY_URI, "simple.sch", true), true);

        assertThat(engine.validate(TestHelper.read(Simple.SIMPLE_VALID)).isConformant()).isTrue();
        assertThat(engine.validate(TestHelper.read(Simple.SCHEMATRON_INVALID)).isConformant()).isFalse();
    }

    @Test
    public void testAdHocConvenienceOnDefaultCheck(@TempDir final Path tempDir) throws IOException {
        // the convenience does not reach into an archive, so the schematron is materialized as a real file
        final Path schematron = tempDir.resolve("simple.sch");
        try ( final InputStream in = UriHelper.resolve(Simple.REPOSITORY_URI, "simple.sch", true).toURL().openStream() ) {
            Files.write(schematron, in.readAllBytes());
        }

        final DefaultVCheck engine = createEngine();
        final AdHocValidationResult result = engine.validateAdHoc(TestHelper.read(Simple.SIMPLE_VALID), schematron.toUri());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isConformant()).isTrue();
    }
}
