package org.kosit.validator.impl.conformatron;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.api.InputFactory.read;

import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.detection.ECTSeverity;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.conformatron.AdHocSchematronValidation.AdHocValidationResult;

/**
 * Tests the ad-hoc schematron validation prototype: validate directly against a schematron, no scenario configuration.
 */
public class AdHocSchematronValidationTest {

    private final AdHocSchematronValidation validation = new AdHocSchematronValidation(Helper.getTestProcessor());

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
        assertThat(result.detections().getAll()).extracting("code").contains(AdHocSchematronValidation.CODE_FAILED_ASSERT);
        // the violated assert id and the SVRL location are part of the message
        assertThat(result.detections().getAll().get(0).getText().getDisplayTextLocaleIndependent()).contains("content-1");
    }

    @Test
    public void testProcessingErrorFailsTheRun() {
        final AdHocValidationResult result = this.validation.validate(read(Simple.SIMPLE_VALID),
                Simple.REPOSITORY_URI.resolve("simple-runtime-error.sch"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.status()).isEqualTo(ECTStepResult.FAILURE);
        assertThat(result.isConformant()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(AdHocSchematronValidation.CODE_RULES_PROCESSING_ERROR);
        assertThat(result.detections().getWorstSeverity().getNumericLevel()).isEqualTo(ECTSeverity.FATAL_ERROR.getNumericLevel());
        // document identity is retained even on failure
        assertThat(result.parsedSource()).isNotNull();
        assertThat(result.parsedSource().getHashBytes()).isNotEmpty();
    }

    @Test
    public void testPreparationErrorFailsTheRun() {
        final AdHocValidationResult result = this.validation.validate(read(Simple.SIMPLE_VALID),
                Simple.REPOSITORY_URI.resolve("does-not-exist.sch"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(AdHocSchematronValidation.CODE_RULES_PREPARATION_ERROR);
    }

    @Test
    public void testNotWellformedDocumentFailsBeforeRules() {
        final AdHocValidationResult result = this.validation.validate(read(Simple.NOT_WELLFORMED),
                Simple.REPOSITORY_URI.resolve("simple.sch"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").contains(ParseDocumentAction.CODE_NOT_WELLFORMED);
    }
}
