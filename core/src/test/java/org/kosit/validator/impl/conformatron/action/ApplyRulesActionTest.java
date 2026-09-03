package org.kosit.validator.impl.conformatron.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kosit.validator.helper.ResourceHelperExtension;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction.ApplyRulesActionResult;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.SeverityOverrides;
import org.kosit.validator.impl.conformatron.model.ValidationArtifactReference;
import org.kosit.validator.impl.conformatron.source.ReadResource;
import org.kosit.validator.impl.conformatron.source.Resource;
import org.kosit.validator.scenario.v1.CreateReportType;
import org.kosit.validator.scenario.v1.CustomErrorLevel;
import org.kosit.validator.scenario.v1.ErrorLevelType;
import org.kosit.validator.scenario.v1.ScenarioType;

/**
 * Tests {@link ApplyRulesAction} (step 7) with real rule sets prepared by steps 5+6.
 */
public class ApplyRulesActionTest {

    @RegisterExtension
    private final ResourceHelperExtension resHelper = new ResourceHelperExtension();

    private final ApplyRulesAction action = new ApplyRulesAction();

    private final ContentRepository repository = new ContentRepository(TestHelper.getTestProcessor(), TestHelper.getTestResolvingStrategy(),
            Simple.REPOSITORY_URI);

    private @NonNull CTParsedValidationSource parse(final @NonNull URI document) throws IOException {
        final ParseXmlResult parsed = new ParseXmlAction().execute(ReadResource.of(Resource.of(document), resHelper.get()));
        assertNotNull(parsed);
        assertTrue(parsed.isSuccess());
        assertNotNull(parsed.getParsedSource());

        return parsed.getParsedSource();
    }

    private List<CTPreparedRuleSet> prepare(final String... references) {
        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(Simple.REPOSITORY_URI, true)
                .execute(Arrays.stream(references).map(ValidationArtifactReference::of).toList(), "test");
        assertThat(retrieved.isSuccess()).isTrue();

        final PrepareRulesAction.PrepareRulesResult prepared = new PrepareRulesAction(this.repository).execute(retrieved.artifacts(),
                "test");
        assertThat(prepared.isSuccess()).isTrue();
        return prepared.ruleSets();
    }

    @Test
    public void testCleanRunAppliesAllRuleSetsInOrder() throws IOException {
        final var step1 = parse(Simple.SIMPLE_VALID);
        assertNotNull(step1);

        final var step2 = prepare("simple.xsd", "simple.sch");
        assertNotNull(step2);

        final ApplyRulesActionResult result = this.action.execute(step1, step2);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.result().getResultsByRuleSet()).hasSize(2);
        assertThat(result.result().hasErrors()).isFalse();
        // one rules-applied INFO per clean rule set, in execution order
        assertThat(result.detections().getAll()).extracting("code").containsExactly(ApplyRulesAction.CODE_RULES_APPLIED,
                ApplyRulesAction.CODE_RULES_APPLIED);
    }

    @Test
    public void testFindingsAreANegativeButValidResult() throws IOException {
        final ApplyRulesActionResult result = this.action.execute(parse(Simple.SCHEMATRON_INVALID), prepare("simple.xsd", "simple.sch"));

        // the step succeeded even though the document has findings
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.result().hasErrors()).isTrue();
        // the assert id is the detection code per spec
        assertThat(result.detections().getAll()).extracting("code").contains("content-1");
    }

    @Test
    public void testSchemaViolationsAreReportedWithLocation() throws IOException {
        final ApplyRulesActionResult result = this.action.execute(parse(Simple.SCHEMA_INVALID), prepare("simple.xsd"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.result().hasErrors()).isTrue();
        assertThat(result.detections().getAll()).extracting("code").contains(ApplyRulesAction.CODE_SCHEMA_VIOLATION);
        assertThat(result.detections().getAll().get(0).getLocation().getLineNumber()).isPositive();
    }

    @Test
    public void testEngineFailureFailsFastAndSkipsRemaining() throws IOException {
        final ApplyRulesActionResult result = this.action.execute(parse(Simple.SIMPLE_VALID),
                prepare("simple-runtime-error.sch", "simple.sch"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.status()).isEqualTo(CTStepResult.FAILURE);
        // both keys stay present: the failed one and the skipped one
        assertThat(result.result().getResultsByRuleSet()).hasSize(2);
        final List<CTDetectionList> lists = List.copyOf(result.result().getResultsByRuleSet().values());
        assertThat(lists.get(0).getAll()).extracting("code").containsExactly(ApplyRulesAction.CODE_RULE_ENGINE_ERROR);
        assertThat(lists.get(1).getAll()).extracting("code").containsExactly(ApplyRulesAction.CODE_STEP_SKIPPED);
    }

    @Test
    public void testCustomLevelOverrideDowngradesFinding() throws IOException {
        // the scenario demotes the failing rule to information -> the finding is no longer an error
        final SeverityOverrides overrides = overridesFor("content-1", ErrorLevelType.INFORMATION);
        final ApplyRulesActionResult result = this.action.execute(parse(Simple.SCHEMATRON_INVALID), prepare("simple.xsd", "simple.sch"),
                overrides);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.result().hasErrors()).isFalse();
        final CTDetection overridden = result.detections().getAll().stream().filter(d -> "content-1".equals(d.getCode())).findFirst()
                .orElseThrow();
        assertThat(overridden.getSeverity()).isEqualTo(CTStandardSeverity.NONE);
        // the declared severity stays auditable on the detection
        assertThat(((Detection) overridden).getOriginalSeverity()).isEqualTo(CTStandardSeverity.ERROR);
    }

    @Test
    public void testEngineErrorAndSkipMarkersAreNeverOverridable() throws IOException {
        // 1.x PROCESSING_ERROR exemption, enforced structurally: overrides only reach rule findings
        final SeverityOverrides overrides = overridesFor(ApplyRulesAction.CODE_RULE_ENGINE_ERROR, ErrorLevelType.INFORMATION);
        final ApplyRulesActionResult result = this.action.execute(parse(Simple.SIMPLE_VALID),
                prepare("simple-runtime-error.sch", "simple.sch"), overrides);

        assertThat(result.status()).isEqualTo(CTStepResult.FAILURE);
        final CTDetection engineError = result.detections().getAll().stream()
                .filter(d -> ApplyRulesAction.CODE_RULE_ENGINE_ERROR.equals(d.getCode())).findFirst().orElseThrow();
        assertThat(engineError.getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(((Detection) engineError).getOriginalSeverity()).isNull();
    }

    private static SeverityOverrides overridesFor(final String code, final ErrorLevelType level) {
        final CustomErrorLevel custom = new CustomErrorLevel();
        custom.setLevel(level);
        custom.getValue().add(code);
        final CreateReportType report = new CreateReportType();
        report.getCustomLevel().add(custom);
        final ScenarioType scenario = new ScenarioType();
        scenario.getCreateReport().add(report);
        return SeverityOverrides.fromConfiguration(scenario);
    }

    @Test
    public void testNoRuleSetsSkipsTheStep() throws IOException {
        final ApplyRulesActionResult result = this.action.execute(parse(Simple.SIMPLE_VALID), List.of());

        assertThat(result.status()).isEqualTo(CTStepResult.SKIPPED);
        assertThat(result.result().isEmpty()).isTrue();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(ApplyRulesAction.CODE_STEP_SKIPPED);
    }
}
