package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;

import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.conformance.CTDecision;
import org.conformatron.api.model.detection.CTDetection;
import org.junit.jupiter.api.Test;
import org.kosit.base.uri.UriHelper;
import org.kosit.validator.TestHelper;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.report.CvrAssert;
import org.kosit.validator.testdata.TestResources;
import org.kost.validator.api.xml.XmlDetection;

/**
 * The ad hoc validation — "run this Schematron against this document" — as a scenario assembled at runtime
 * ({@link Scenario#adHoc}) and run by the one engine: no scenarios.xml, no match, the directory of the Schematron as
 * repository, and a CVR like every other run.
 */
public class AdHocValidationTest {

    private static URI schematron(final String name) {
        return UriHelper.resolve(TestResources.Simple.REPOSITORY_URI, name, true);
    }

    private static ConformanceValidationResult validate(final URI document, final String schematron) {
        return ConformanceValidation.adHoc(new TestEngineInformation(), TestHelper.getTestProcessor(), schematron(schematron), true)
                .validate(TestHelper.read(document));
    }

    private static List<String> codes(final ConformanceValidationResult result) {
        return result.getAllDetections().stream().map(CTDetection::getCode).toList();
    }

    @Test
    public void testTheScenarioAppliesUnconditionallyWithTheRuleSetAlone() {
        final Scenario scenario = Scenario.adHoc(TestHelper.getTestProcessor(), schematron("simple.sch"), true);

        assertThat(scenario.isUnconditional()).isTrue();
        assertThat(scenario.getName()).isEqualTo("simple.sch");
        assertThat(scenario.getConfiguration().getValidateWithXmlSchema()).isNull();
        assertThat(scenario.getConfiguration().getValidateWithSchematron()).hasSize(1);
        assertThat(scenario.getConfiguration().getValidateWithSchematron().get(0).getResource().getLocation()).isEqualTo("simple.sch");
        // the directory of the schematron is the repository
        assertThat(scenario.getRepository().getRepository().toString()).endsWith("/repository/");
        assertThat(scenario.getDefinitionFile()).isNull();
    }

    @Test
    public void testConformantDocument() throws Exception {
        final ConformanceValidationResult result = validate(TestResources.Simple.SIMPLE_VALID, "simple.sch");

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.isConformant()).isTrue();
        assertThat(result.getDecision()).isEqualTo(CTDecision.ACCEPT);
        assertThat(result.getSelectedScenarioName()).isEqualTo("simple.sch");
        // the report of an ad hoc run is a CVR like any other
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        result.writeCvr(out);
        CvrAssert.assertValidCvr("adhoc-conformant", out.toByteArray());
    }

    @Test
    public void testDocumentWithFindings() {
        final ConformanceValidationResult result = validate(TestResources.Simple.SCHEMATRON_INVALID, "simple.sch");

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.isConformant()).isFalse();
        assertThat(result.getDecision()).isEqualTo(CTDecision.REJECT);
        // per step-07 spec the assert id becomes the detection code
        assertThat(codes(result)).contains("content-1");
    }

    @Test
    public void testProcessingErrorCancelsTheRun() {
        final ConformanceValidationResult result = validate(TestResources.Simple.SIMPLE_VALID, "simple-runtime-error.sch");

        assertThat(result.isCompleted()).isFalse();
        assertThat(result.getCancelledAt()).isEqualTo(CTActionType.APPLY_RULES);
        assertThat(result.getDecision()).isEqualTo(CTDecision.REJECT);
        assertThat(codes(result)).contains(ApplyRulesAction.CODE_RULE_ENGINE_ERROR);
    }

    @Test
    public void testASchematronWithoutRepositoryIsAConfigurationError() {
        // no repository can be derived from these two: an archive without the permission to reach into it, and a
        // relative URI - that is a mistake of whoever assembles the engine, not a property of any document
        assertThrows(IllegalArgumentException.class, () -> Scenario.adHoc(TestHelper.getTestProcessor(),
                UriHelper.resolve(TestResources.getJarRepository(), "simple.sch", true), false));
        assertThrows(IllegalArgumentException.class, () -> Scenario.adHoc(TestHelper.getTestProcessor(), URI.create("simple.sch"), false));
    }

    @Test
    public void testASetOfArtifactsIsOneScenario() {
        // schema and rules together, the repository derived as their common parent directory
        final Scenario scenario = Scenario.adHoc(TestHelper.getTestProcessor(), List.of(schematron("simple.xsd"), schematron("simple.sch")),
                null, true);

        assertThat(scenario.isUnconditional()).isTrue();
        assertThat(scenario.getName()).isEqualTo("simple.xsd, simple.sch");
        assertThat(scenario.getConfiguration().getValidateWithXmlSchema().getResource()).extracting("location")
                .containsExactly("simple.xsd");
        assertThat(scenario.getConfiguration().getValidateWithSchematron()).hasSize(1);
        assertThat(scenario.getRepository().getRepository().toString()).endsWith("/repository/");
    }

    @Test
    public void testTheSetAppliesSchemaAndRules() {
        final ConformanceValidation engine = ConformanceValidation.adHoc(new TestEngineInformation(), TestHelper.getTestProcessor(),
                List.of(schematron("simple.xsd"), schematron("simple.sch")), TestResources.Simple.REPOSITORY_URI, true);

        final ConformanceValidationResult valid = engine.validate(TestHelper.read(TestResources.Simple.SIMPLE_VALID));
        assertThat(valid.getFindingsByRuleSet()).hasSize(2);
        assertThat(valid.getDecision()).isEqualTo(CTDecision.ACCEPT);

        // the schema rejects what the rules alone would not see
        final ConformanceValidationResult invalid = engine.validate(TestHelper.read(TestResources.Simple.SCHEMA_INVALID));
        assertThat(invalid.getDecision()).isEqualTo(CTDecision.REJECT);
        assertThat(codes(invalid)).contains(ApplyRulesAction.CODE_SCHEMA_VIOLATION);
    }

    @Test
    public void testArtifactsMustLieWithinTheRepository() {
        // the repository is the input directory, the artifact lives in the repository directory next to it
        final URI elsewhere = UriHelper.resolve(TestResources.Simple.REPOSITORY_URI, "../input/", true);
        assertThrows(IllegalArgumentException.class,
                () -> Scenario.adHoc(TestHelper.getTestProcessor(), List.of(schematron("simple.sch")), elsewhere, true));
    }

    @Test
    public void testAnArtifactOfUnknownKindIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Scenario.adHoc(TestHelper.getTestProcessor(), List.of(schematron("some.txt")), null, true));
        assertThrows(IllegalArgumentException.class, () -> Scenario.adHoc(TestHelper.getTestProcessor(), List.of(), null, true));
    }

    @Test
    public void testMissingSchematronCancelsInTheRetrieveStep() {
        final ConformanceValidationResult result = validate(TestResources.Simple.SIMPLE_VALID, "does-not-exist.sch");

        assertThat(result.getCancelledAt()).isEqualTo(CTActionType.RETRIEVE_ARTIFACTS);
        // reported under the canonical step-5 code, not a generic ad hoc preparation error
        assertThat(codes(result)).contains(RetrieveArtifactsAction.CODE_ARTIFACT_MISSING);
    }

    @Test
    public void testNotWellformedDocumentCancelsBeforeRules() {
        final ConformanceValidationResult result = validate(TestResources.Simple.NOT_WELLFORMED, "simple.sch");

        assertThat(result.getCancelledAt()).isEqualTo(CTActionType.PARSE_DOCUMENT);
        assertThat(codes(result)).contains(XmlDetection.CODE_NOT_WELLFORMED);
    }
}
