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
import org.kosit.validator.TestHelper;
import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kosit.validator.testdata.TestResources;

/**
 * The {@link ValidationEngine} contract and its one implementation, {@link ConformanceValidation}, in its two
 * assemblies: over the scenarios of a configuration, and over one scenario built at runtime from a Schematron (ad hoc).
 * Both are used through the contract here, not through their own API.
 */
public class ValidationEngineTest {

    private static ScenarioSet simpleConfiguration() {
        return ScenarioSet.load(TestResources.Simple.SCENARIOS, TestResources.Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
    }

    @Test
    public void testTheEngineOverAConfiguration() {
        // the shared test repository lives inside an archive, so this engine is allowed to resolve into one
        final ValidationEngine<ConformanceValidationResult> engine = new ConformanceValidation(new TestEngineInformation(),
                TestHelper.getTestProcessor(), true, simpleConfiguration());

        final ConformanceValidationResult result = engine.validate(TestHelper.read(TestResources.Simple.SIMPLE_VALID));

        assertThat(result).isNotNull();
        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getDecision()).isEqualTo(CTDecision.ACCEPT);
    }

    @Test
    public void testTheEngineOverASingleSchematron() {
        final ValidationEngine<ConformanceValidationResult> engine = ConformanceValidation.adHoc(new TestEngineInformation(),
                TestHelper.getTestProcessor(), UriHelper.resolve(TestResources.Simple.REPOSITORY_URI, "simple.sch", true), true);

        assertThat(engine.validate(TestHelper.read(TestResources.Simple.SIMPLE_VALID)).isConformant()).isTrue();
        assertThat(engine.validate(TestHelper.read(TestResources.Simple.SCHEMATRON_INVALID)).isConformant()).isFalse();
    }

    @Test
    public void testTheEngineOverASchematronInTheFileSystem(@TempDir final Path tempDir) throws IOException {
        // without the archive permission the schematron has to be a real file
        final Path schematron = tempDir.resolve("simple.sch");
        try ( final InputStream in = UriHelper.resolve(TestResources.Simple.REPOSITORY_URI, "simple.sch", true).toURL().openStream() ) {
            Files.write(schematron, in.readAllBytes());
        }

        final ConformanceValidationResult result = ConformanceValidation
                .adHoc(new TestEngineInformation(), TestHelper.getTestProcessor(), schematron.toUri(), false)
                .validate(TestHelper.read(TestResources.Simple.SIMPLE_VALID));

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.isConformant()).isTrue();
    }
}
