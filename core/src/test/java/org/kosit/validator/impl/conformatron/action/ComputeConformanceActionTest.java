package org.kosit.validator.impl.conformatron.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kosit.validator.api.VInputFactory.read;

import java.net.URI;
import java.util.List;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.conformance.CTConformanceResult;
import org.conformatron.api.model.rule.CTApplyRulesResult;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.scenario.CTConformanceTarget;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.conformatron.api.model.source.CTValidationArtifactReference;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction.ComputeConformanceActionResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLAction;
import org.kosit.validator.impl.conformatron.model.ApplyRulesResult;
import org.kosit.validator.impl.conformatron.model.ConformanceTarget;
import org.kosit.validator.impl.conformatron.model.ValidationArtifactReference;

/**
 * Tests {@link ComputeConformanceAction} (step 8) on real step-7 results.
 */
public class ComputeConformanceActionTest {

    private final ComputeConformanceAction action = new ComputeConformanceAction();

    private final ContentRepository repository = new ContentRepository(TestHelper.getTestProcessor(),
            ResolvingMode.STRICT_RELATIVE.getStrategy(), Simple.REPOSITORY_URI);

    private static final CTConformanceTarget TARGET = ConformanceTarget.of("simple-target", "Simple Target",
            List.of("simple.xsd", "simple.sch", "simple-runtime-error.sch"), null);

    private CTApplyRulesResult applyRules(final URI document, final String... references) {
        final CTParsedValidationSource parsed = new ParseXMLAction().execute(read(document)).getParsedSource();
        final List<CTValidationArtifactReference> refs = List.of(references).stream()
                .map(r -> (CTValidationArtifactReference) ValidationArtifactReference.of(r)).toList();
        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(Simple.REPOSITORY_URI).execute(refs,
                "test");
        final List<CTPreparedRuleSet> ruleSets = new PrepareRulesAction(this.repository).execute(retrieved.artifacts(), "test").ruleSets();
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
        assertThat(result.result().getAllStatements()).extracting("result").containsOnly(CTConformanceResult.CONFORMANT);
        assertThat(result.detections().getAll()).extracting("code").containsOnly(ComputeConformanceAction.CODE_TARGET_CONFORMANT);
    }

    @Test
    public void testFindingsMakeTheTargetNonConformant() {
        final ComputeConformanceActionResult result = this.action.execute(applyRules(Simple.SCHEMATRON_INVALID, "simple.xsd", "simple.sch"),
                List.of(TARGET));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.result().hasNonConformantTarget()).isTrue();
        // the XSD rule set passed, the schematron rule set drove the non-conformance — traceable per rule set
        assertThat(result.result().getAllStatements()).extracting("result").containsExactly(CTConformanceResult.CONFORMANT,
                CTConformanceResult.NON_CONFORMANT);
        assertThat(result.detections().containsAtLeastOneError()).isTrue();
    }

    @Test
    public void testSkippedRuleSetIsNotConformant() {
        // engine error on the first rule set -> second one is skipped by step 7; neither may look conformant
        final ComputeConformanceActionResult result = this.action
                .execute(applyRules(Simple.SIMPLE_VALID, "simple-runtime-error.sch", "simple.sch"), List.of(TARGET));

        assertThat(result.result().getAllStatements()).extracting("result").containsExactly(CTConformanceResult.NON_CONFORMANT,
                CTConformanceResult.NON_CONFORMANT);
    }

    @Test
    public void testEmptyApplyRulesResultSkipsTheStepButForwardsAResult() {
        final CTParsedValidationSource parsed = new ParseXMLAction().execute(read(Simple.SIMPLE_VALID)).getParsedSource();
        final ComputeConformanceActionResult result = this.action.execute(ApplyRulesResult.empty(parsed), List.of(TARGET));

        assertThat(result.status()).isEqualTo(CTStepResult.SKIPPED);
        assertThat(result.result().isEmpty()).isTrue();
        assertThat(result.result().getParsedSource()).isSameAs(parsed);
        assertThat(result.detections().getAll()).extracting("code").containsExactly(ComputeConformanceAction.CODE_STEP_SKIPPED);
    }

    @Test
    public void testAcceptSelectorTargetsAreRejectedForNow() {
        final CTConformanceTarget withSelector = ConformanceTarget.of("t", "T", List.of("simple.sch"), "count(//x) = 0");
        final CTApplyRulesResult applied = applyRules(Simple.SIMPLE_VALID, "simple.sch");

        assertThrows(IllegalArgumentException.class, () -> this.action.execute(applied, List.of(withSelector)));
    }
}
