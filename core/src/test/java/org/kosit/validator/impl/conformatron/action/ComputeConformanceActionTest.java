package org.kosit.validator.impl.conformatron.action;

import org.kosit.validator.impl.conformatron.model.ApplyRulesResult;
import org.kosit.validator.impl.conformatron.model.ValidationArtifactReference;
import org.kosit.validator.impl.conformatron.model.ConformanceTarget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kosit.validator.api.InputFactory.read;

import java.net.URI;
import java.util.List;

import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.conformance.ECTConformanceResult;
import org.conformatron.api.model.rule.ICTApplyRulesResult;
import org.conformatron.api.model.rule.ICTPreparedRuleSet;
import org.conformatron.api.model.scenario.ICTConformanceTarget;
import org.conformatron.api.model.source.ICTParsedValidationSource;
import org.conformatron.api.model.source.ICTValidationArtifactReference;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction.ComputeConformanceActionResult;

/**
 * Tests {@link ComputeConformanceAction} (step 8) on real step-7 results.
 */
public class ComputeConformanceActionTest {

    private final ComputeConformanceAction action = new ComputeConformanceAction();

    private final ContentRepository repository = new ContentRepository(Helper.getTestProcessor(),
            ResolvingMode.STRICT_RELATIVE.getStrategy(), Simple.REPOSITORY_URI);

    private static final ICTConformanceTarget TARGET = ConformanceTarget.of("simple-target", "Simple Target",
            List.of("simple.xsd", "simple.sch", "simple-runtime-error.sch"), null);

    private ICTApplyRulesResult applyRules(final URI document, final String... references) {
        final ICTParsedValidationSource parsed = new ParseXMLAction().execute(read(document)).parsedSource();
        final List<ICTValidationArtifactReference> refs = List.of(references).stream()
                .map(r -> (ICTValidationArtifactReference) ValidationArtifactReference.of(r)).toList();
        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(Simple.REPOSITORY_URI).execute(refs,
                "test");
        final List<ICTPreparedRuleSet> ruleSets = new PrepareRulesAction(this.repository).execute(retrieved.artifacts(), "test").ruleSets();
        return new ApplyRulesAction().execute(parsed, ruleSets).result();
    }

    @Test
    public void testConformantDocument() {
        final ComputeConformanceActionResult result = this.action.execute(applyRules(Simple.SIMPLE_VALID, "simple.xsd", "simple.sch"),
                List.of(TARGET));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.result().hasNonConformantTarget()).isFalse();
        // per-rule-set granularity: one statement per rule set
        assertThat(result.result().getStatementsByRuleSet()).hasSize(2);
        assertThat(result.result().getAllStatements()).extracting("result").containsOnly(ECTConformanceResult.CONFORMANT);
        assertThat(result.detections().getAll()).extracting("code").containsOnly(ComputeConformanceAction.CODE_TARGET_CONFORMANT);
    }

    @Test
    public void testFindingsMakeTheTargetNonConformant() {
        final ComputeConformanceActionResult result = this.action.execute(applyRules(Simple.SCHEMATRON_INVALID, "simple.xsd", "simple.sch"),
                List.of(TARGET));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.result().hasNonConformantTarget()).isTrue();
        // the XSD rule set passed, the schematron rule set drove the non-conformance — traceable per rule set
        assertThat(result.result().getAllStatements()).extracting("result").containsExactly(ECTConformanceResult.CONFORMANT,
                ECTConformanceResult.NON_CONFORMANT);
        assertThat(result.detections().containsAtLeastOneError()).isTrue();
    }

    @Test
    public void testSkippedRuleSetIsNotConformant() {
        // engine error on the first rule set -> second one is skipped by step 7; neither may look conformant
        final ComputeConformanceActionResult result = this.action
                .execute(applyRules(Simple.SIMPLE_VALID, "simple-runtime-error.sch", "simple.sch"), List.of(TARGET));

        assertThat(result.result().getAllStatements()).extracting("result").containsExactly(ECTConformanceResult.NON_CONFORMANT,
                ECTConformanceResult.NON_CONFORMANT);
    }

    @Test
    public void testEmptyApplyRulesResultSkipsTheStepButForwardsAResult() {
        final ICTParsedValidationSource parsed = new ParseXMLAction().execute(read(Simple.SIMPLE_VALID)).parsedSource();
        final ComputeConformanceActionResult result = this.action.execute(ApplyRulesResult.empty(parsed), List.of(TARGET));

        assertThat(result.status()).isEqualTo(ECTStepResult.SKIPPED);
        assertThat(result.result().isEmpty()).isTrue();
        assertThat(result.result().getParsedSource()).isSameAs(parsed);
        assertThat(result.detections().getAll()).extracting("code").containsExactly(ComputeConformanceAction.CODE_STEP_SKIPPED);
    }

    @Test
    public void testAcceptSelectorTargetsAreRejectedForNow() {
        final ICTConformanceTarget withSelector = ConformanceTarget.of("t", "T", List.of("simple.sch"), "count(//x) = 0");
        final ICTApplyRulesResult applied = applyRules(Simple.SIMPLE_VALID, "simple.sch");

        assertThrows(IllegalArgumentException.class, () -> this.action.execute(applied, List.of(withSelector)));
    }
}
