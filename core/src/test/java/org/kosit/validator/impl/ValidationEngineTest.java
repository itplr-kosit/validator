package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.conformatron.api.model.conformance.CTDecision;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kosit.base.uri.UriHelper;
import org.kosit.cvr.report.AdHocValidationResult;
import org.kosit.validator.TestHelper;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kosit.validator.testdata.TestResources;

/**
 * The {@link ValidationEngine} contract with its two implementations: {@link ConformanceValidation} (the full pipeline
 * along the configured scenarios) and {@link SchematronValidation} (ad hoc against one Schematron). Both are used
 * through the contract here, not through their own API.
 */
public class ValidationEngineTest {

    private static VConfiguration simpleConfiguration() {
        return VConfiguration.load(TestResources.Simple.SCENARIOS, TestResources.Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
    }

    @Test
    public void testConformanceValidationIsAnEngine() {
        // the shared test repository lives inside an archive, so this engine is allowed to resolve into one
        final ValidationEngine<ConformanceValidationResult> engine = new ConformanceValidation(new TestEngineInformation(),
                TestHelper.getTestProcessor(), true, simpleConfiguration());

        final ConformanceValidationResult result = engine.validate(TestHelper.read(TestResources.Simple.SIMPLE_VALID));

        assertThat(result).isNotNull();
        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getDecision()).isEqualTo(CTDecision.ACCEPT);
    }

    @Test
    public void testSchematronValidationIsAnEngine() {
        final ValidationEngine<AdHocValidationResult> engine = new SchematronValidation(TestHelper.getTestProcessor(),
                UriHelper.resolve(TestResources.Simple.REPOSITORY_URI, "simple.sch", true), true);

        assertThat(engine.validate(TestHelper.read(TestResources.Simple.SIMPLE_VALID)).isConformant()).isTrue();
        assertThat(engine.validate(TestHelper.read(TestResources.Simple.SCHEMATRON_INVALID)).isConformant()).isFalse();
    }

    @Test
    public void testSchematronValidationAgainstAFileSystemSchematron(@TempDir final Path tempDir) throws IOException {
        // the two-argument validate does not reach into an archive, so the schematron is materialized as a real file
        final Path schematron = tempDir.resolve("simple.sch");
        try ( final InputStream in = UriHelper.resolve(TestResources.Simple.REPOSITORY_URI, "simple.sch", true).toURL().openStream() ) {
            Files.write(schematron, in.readAllBytes());
        }

        final AdHocValidationResult result = new SchematronValidation(TestHelper.getTestProcessor())
                .validate(TestHelper.read(TestResources.Simple.SIMPLE_VALID), schematron.toUri());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isConformant()).isTrue();
    }
}
