package org.kosit.validator.impl.conformatron.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VInputFactory;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction.ApplyRulesActionResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLResult;
import org.kosit.validator.impl.conformatron.model.ValidationArtifactReference;

/**
 * Tests {@link ApplyRulesAction} (step 7) with real rule sets prepared by steps 5+6.
 */
public class ApplyRulesActionTest {

    private final ApplyRulesAction action = new ApplyRulesAction();

    private final ContentRepository repository = new ContentRepository(TestHelper.getTestProcessor(),
            ResolvingMode.STRICT_RELATIVE.getStrategy(), Simple.REPOSITORY_URI);

    private static CTParsedValidationSource parse(final URI document) {
        final ParseXMLResult parsed = new ParseXMLAction().execute(VInputFactory.read(document));
        assertThat(parsed.isSuccess()).isTrue();
        return parsed.getParsedSource();
    }

    private List<CTPreparedRuleSet> prepare(final String... references) {
        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(Simple.REPOSITORY_URI)
                .execute(Arrays.stream(references).map(ValidationArtifactReference::of).toList(), "test");
        assertThat(retrieved.isSuccess()).isTrue();

        final PrepareRulesAction.PrepareRulesResult prepared = new PrepareRulesAction(this.repository).execute(retrieved.artifacts(),
                "test");
        assertThat(prepared.isSuccess()).isTrue();
        return prepared.ruleSets();
    }

    @Test
    public void testCleanRunAppliesAllRuleSetsInOrder() {
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
    public void testFindingsAreANegativeButValidResult() {
        final ApplyRulesActionResult result = this.action.execute(parse(Simple.SCHEMATRON_INVALID), prepare("simple.xsd", "simple.sch"));

        // the step succeeded even though the document has findings
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.result().hasErrors()).isTrue();
        // the assert id is the detection code per spec
        assertThat(result.detections().getAll()).extracting("code").contains("content-1");
    }

    @Test
    public void testSchemaViolationsAreReportedWithLocation() {
        final ApplyRulesActionResult result = this.action.execute(parse(Simple.SCHEMA_INVALID), prepare("simple.xsd"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.result().hasErrors()).isTrue();
        assertThat(result.detections().getAll()).extracting("code").contains(ApplyRulesAction.CODE_SCHEMA_VIOLATION);
        assertThat(result.detections().getAll().get(0).getLocation().getLineNumber()).isPositive();
    }

    @Test
    public void testEngineFailureFailsFastAndSkipsRemaining() {
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
    public void testNoRuleSetsSkipsTheStep() {
        final ApplyRulesActionResult result = this.action.execute(parse(Simple.SIMPLE_VALID), List.of());

        assertThat(result.status()).isEqualTo(CTStepResult.SKIPPED);
        assertThat(result.result().isEmpty()).isTrue();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(ApplyRulesAction.CODE_STEP_SKIPPED);
    }
}
