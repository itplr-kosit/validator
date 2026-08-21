package org.kosit.validator.impl.conformatron.engine;

import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLAction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.api.VInputFactory.read;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.conformatron.engine.SchematronValidation.AdHocValidationResult;

/**
 * Tests the ad-hoc schematron validation prototype: validate directly against a schematron, no scenario configuration.
 */
public class SchematronValidationTest {

    private final SchematronValidation validation = new SchematronValidation(Helper.getTestProcessor());

    @Test
    public void testConformantDocument() {
        final AdHocValidationResult result = this.validation.validate(read(Simple.SIMPLE_VALID),
                Simple.REPOSITORY_URI.resolve("simple.sch"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isConformant()).isTrue();
        assertThat(result.parsedSource()).isNotNull();
        assertThat(result.detections().containsAtLeastOneError()).isFalse();
    }

    @Test
    public void testDocumentWithFindings() {
        final AdHocValidationResult result = this.validation.validate(read(Simple.SCHEMATRON_INVALID),
                Simple.REPOSITORY_URI.resolve("simple.sch"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isConformant()).isFalse();
        // per step-07 spec the assert id becomes the detection code
        assertThat(result.detections().getAll()).extracting("code").contains("content-1");
    }

    @Test
    public void testProcessingErrorFailsTheRun() {
        final AdHocValidationResult result = this.validation.validate(read(Simple.SIMPLE_VALID),
                Simple.REPOSITORY_URI.resolve("simple-runtime-error.sch"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.status()).isEqualTo(CTStepResult.FAILURE);
        assertThat(result.isConformant()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(ApplyRulesAction.CODE_RULE_ENGINE_ERROR);
        assertThat(result.detections().getWorstSeverity().getNumericLevel()).isEqualTo(CTStandardSeverity.ERROR.getNumericLevel());
        // document identity is retained even on failure
        assertThat(result.parsedSource()).isNotNull();
        assertThat(result.parsedSource().getHashBytes()).isNotEmpty();
    }

    @Test
    public void testMissingSchematronFailsInRetrieveStep() {
        final AdHocValidationResult result = this.validation.validate(read(Simple.SIMPLE_VALID),
                Simple.REPOSITORY_URI.resolve("does-not-exist.sch"));

        assertThat(result.isSuccess()).isFalse();
        // reported under the canonical step-5 code, not a generic ad-hoc preparation error
        assertThat(result.detections().getAll()).extracting("code").containsExactly(RetrieveArtifactsAction.CODE_ARTIFACT_MISSING);
    }

    @Test
    public void testNotWellformedDocumentFailsBeforeRules() {
        final AdHocValidationResult result = this.validation.validate(read(Simple.NOT_WELLFORMED),
                Simple.REPOSITORY_URI.resolve("simple.sch"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").contains(ParseXMLAction.CODE_NOT_WELLFORMED);
    }
}
