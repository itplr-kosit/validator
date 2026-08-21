package org.kosit.validator.impl.conformatron.action;

import org.kosit.validator.impl.conformatron.model.ValidationArtifactReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.api.InputFactory.read;

import java.net.URI;
import java.util.List;

import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.detection.ICTDetectionList;
import org.conformatron.api.model.rule.ICTPreparedRuleSet;
import org.conformatron.api.model.source.ICTParsedValidationSource;
import org.conformatron.api.model.source.ICTValidationArtifactReference;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction.ApplyRulesActionResult;

/**
 * Tests {@link ApplyRulesAction} (step 7) with real rule sets prepared by steps 5+6.
 */
public class ApplyRulesActionTest {

    private final ApplyRulesAction action = new ApplyRulesAction();

    private final ContentRepository repository = new ContentRepository(Helper.getTestProcessor(),
            ResolvingMode.STRICT_RELATIVE.getStrategy(), Simple.REPOSITORY_URI);

    private static ICTParsedValidationSource parse(final URI document) {
        final ParseXMLAction.ParseXMLResult parsed = new ParseXMLAction().execute(read(document));
        assertThat(parsed.isSuccess()).isTrue();
        return parsed.parsedSource();
    }

    private List<ICTPreparedRuleSet> prepare(final String... references) {
        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(Simple.REPOSITORY_URI).execute(
                List.of(references).stream().map(r -> (ICTValidationArtifactReference) ValidationArtifactReference.of(r)).toList(), "test");
        assertThat(retrieved.isSuccess()).isTrue();
        final PrepareRulesAction.PrepareRulesResult prepared = new PrepareRulesAction(this.repository).execute(retrieved.artifacts(),
                "test");
        assertThat(prepared.isSuccess()).isTrue();
        return prepared.ruleSets();
    }

    @Test
    public void testCleanRunAppliesAllRuleSetsInOrder() {
        final ApplyRulesActionResult result = this.action.execute(parse(Simple.SIMPLE_VALID), prepare("simple.xsd", "simple.sch"));

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
        assertThat(result.status()).isEqualTo(ECTStepResult.FAILURE);
        // both keys stay present: the failed one and the skipped one
        assertThat(result.result().getResultsByRuleSet()).hasSize(2);
        final List<ICTDetectionList> lists = List.copyOf(result.result().getResultsByRuleSet().values());
        assertThat(lists.get(0).getAll()).extracting("code").containsExactly(ApplyRulesAction.CODE_RULE_ENGINE_ERROR);
        assertThat(lists.get(1).getAll()).extracting("code").containsExactly(ApplyRulesAction.CODE_STEP_SKIPPED);
    }

    @Test
    public void testNoRuleSetsSkipsTheStep() {
        final ApplyRulesActionResult result = this.action.execute(parse(Simple.SIMPLE_VALID), List.of());

        assertThat(result.status()).isEqualTo(ECTStepResult.SKIPPED);
        assertThat(result.result().isEmpty()).isTrue();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(ApplyRulesAction.CODE_STEP_SKIPPED);
    }
}
