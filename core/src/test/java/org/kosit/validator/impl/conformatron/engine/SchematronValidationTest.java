package org.kosit.validator.impl.conformatron.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;
import org.kosit.base.uri.UriHelper;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.XmlDetection;
import org.kosit.validator.impl.conformatron.engine.SchematronValidation.AdHocValidationResult;

/**
 * Tests the ad-hoc schematron validation prototype: validate directly against a schematron, no scenario configuration.
 */
public class SchematronValidationTest {

    private final SchematronValidation validation = new SchematronValidation(TestHelper.getTestProcessor());

    @Test
    public void testConformantDocument() {
        final AdHocValidationResult result = this.validation.validate(TestHelper.read(Simple.SIMPLE_VALID),
                UriHelper.resolve(Simple.REPOSITORY_URI, "simple.sch", true), true);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isConformant()).isTrue();
        assertThat(result.parsedSource()).isNotNull();
        assertThat(result.detections().containsAtLeastOneError()).isFalse();
    }

    @Test
    public void testDocumentWithFindings() {
        final AdHocValidationResult result = this.validation.validate(TestHelper.read(Simple.SCHEMATRON_INVALID),
                UriHelper.resolve(Simple.REPOSITORY_URI, "simple.sch", true), true);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isConformant()).isFalse();
        // per step-07 spec the assert id becomes the detection code
        assertThat(result.detections().getAll()).extracting("code").contains("content-1");
    }

    @Test
    public void testProcessingErrorFailsTheRun() {
        final AdHocValidationResult result = this.validation.validate(TestHelper.read(Simple.SIMPLE_VALID),
                UriHelper.resolve(Simple.REPOSITORY_URI, "simple-runtime-error.sch", true), true);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.status()).isEqualTo(CTStepResult.FAILURE);
        assertThat(result.isConformant()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(ApplyRulesAction.CODE_RULE_ENGINE_ERROR);
        assertThat(result.detections().getWorstSeverity().getNumericLevel()).isEqualTo(CTStandardSeverity.ERROR.getNumericLevel());
        // document identity is retained even on failure
        assertThat(result.parsedSource()).isNotNull();
        assertThat(result.parsedSource().getSource().getReadResource().getHashBytes()).isNotEmpty();
    }

    @Test
    public void testSchematronThatHasNoRepositoryIsReportedAsDetection() {
        // no repository can be derived from these two, and that is reported like every other step failure instead of
        // escaping as an exception: an archive without the permission to reach into it, and a relative URI
        final AdHocValidationResult packaged = this.validation.validate(TestHelper.read(Simple.SIMPLE_VALID),
                UriHelper.resolve(TestHelper.getJarRepository(), "simple.sch", true));
        final AdHocValidationResult relative = this.validation.validate(TestHelper.read(Simple.SIMPLE_VALID), URI.create("simple.sch"));

        assertThat(packaged.isSuccess()).isFalse();
        assertThat(packaged.detections().getAll()).extracting("code").containsExactly(RetrieveArtifactsAction.CODE_ARTIFACT_ACCESS_DENIED);
        assertThat(relative.isSuccess()).isFalse();
        assertThat(relative.detections().getAll()).extracting("code").containsExactly(RetrieveArtifactsAction.CODE_ARTIFACT_ACCESS_DENIED);
    }

    @Test
    public void testMissingSchematronFailsInRetrieveStep() {
        final AdHocValidationResult result = this.validation.validate(TestHelper.read(Simple.SIMPLE_VALID),
                UriHelper.resolve(Simple.REPOSITORY_URI, "does-not-exist.sch", true), true);

        assertThat(result.isSuccess()).isFalse();
        // reported under the canonical step-5 code, not a generic ad-hoc preparation error
        assertThat(result.detections().getAll()).extracting("code").containsExactly(RetrieveArtifactsAction.CODE_ARTIFACT_MISSING);
    }

    @Test
    public void testNotWellformedDocumentFailsBeforeRules() {
        final AdHocValidationResult result = this.validation.validate(TestHelper.read(Simple.NOT_WELLFORMED),
                UriHelper.resolve(Simple.REPOSITORY_URI, "simple.sch", true), true);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").contains(XmlDetection.CODE_NOT_WELLFORMED);
    }
}
