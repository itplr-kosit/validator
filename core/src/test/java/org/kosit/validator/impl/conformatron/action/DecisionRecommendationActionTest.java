package org.kosit.validator.impl.conformatron.action;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;

import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.conformance.CTConformanceResult;
import org.conformatron.api.model.conformance.CTConformanceStatement;
import org.conformatron.api.model.conformance.CTDecision;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.rule.CTApplyRulesResult;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.scenario.CTConformanceTarget;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.conformatron.api.model.validation.CTValidationArtifactReference;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.conformatron.action.DecisionRecommendationAction.DecisionRecommendationResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.XmlDetection;
import org.kosit.validator.impl.conformatron.model.ComputeConformanceResult;
import org.kosit.validator.impl.conformatron.model.ConformanceStatement;
import org.kosit.validator.impl.conformatron.model.ConformanceTarget;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.kosit.validator.impl.conformatron.model.SubjectDetection;
import org.kosit.validator.impl.conformatron.model.ValidationArtifactReference;

/**
 * Tests {@link DecisionRecommendationAction} (step 9) on real step-8 results — one test per specified path.
 */
public class DecisionRecommendationActionTest {

    private final DecisionRecommendationAction action = new DecisionRecommendationAction();

    private final ContentRepository repository = new ContentRepository(TestHelper.getTestProcessor(),
            ResolvingMode.STRICT_RELATIVE.getStrategy(), Simple.REPOSITORY_URI);

    private static final CTConformanceTarget TARGET = ConformanceTarget.of("simple-target", "Simple Target",
            List.of("simple.xsd", "simple.sch"), null);

    private CTApplyRulesResult applyRules(final URI document, final String... references) {
        final CTParsedValidationSource parsed = new ParseXmlAction().execute(TestHelper.read(document)).getParsedSource();
        final List<CTValidationArtifactReference> refs = List.of(references).stream()
                .map(r -> (CTValidationArtifactReference) ValidationArtifactReference.of(r)).toList();
        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(Simple.REPOSITORY_URI).execute(refs,
                "test");
        final List<CTPreparedRuleSet> ruleSets = new PrepareRulesAction(this.repository).execute(retrieved.artifacts(), "test").ruleSets();
        return new ApplyRulesAction().execute(parsed, ruleSets).result();
    }

    private static CTDetection only(final DecisionRecommendationResult result) {
        assertThat(result.detections().getAll()).as("exactly one summary detection").hasSize(1);
        return result.detections().getAll().get(0);
    }

    @Test
    public void testAllTargetsConformantIsAccepted() {
        final DecisionRecommendationResult result = this.action.execute(new ComputeConformanceAction()
                .execute(applyRules(Simple.SIMPLE_VALID, "simple.xsd", "simple.sch"), List.of(TARGET)).result());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.decision()).isEqualTo(CTDecision.ACCEPT);
        final CTDetection detection = only(result);
        assertThat(detection.getCode()).isEqualTo(DecisionRecommendationAction.CODE_ACCEPT);
        assertThat(detection.getSeverity()).isEqualTo(CTStandardSeverity.NONE);
        // the decision travels as an attribute a consumer can read without parsing text
        assertThat(((SubjectDetection) detection).getAttributes()).containsEntry(SubjectDetection.ATTR_DECISION, "ACCEPT");
        assertThat(result.rationale()).contains("Simple Target");
    }

    @Test
    public void testNonConformantTargetIsRejectedNamingTheRuleSet() {
        final DecisionRecommendationResult result = this.action.execute(new ComputeConformanceAction()
                .execute(applyRules(Simple.SCHEMATRON_INVALID, "simple.xsd", "simple.sch"), List.of(TARGET)).result());

        assertThat(result.decision()).isEqualTo(CTDecision.REJECT);
        final CTDetection detection = only(result);
        assertThat(detection.getCode()).isEqualTo(DecisionRecommendationAction.CODE_REJECT);
        assertThat(detection.getSeverity().isError()).isTrue();
        // per-step provenance: the rationale points at the rule set that drove the rejection, not just "rejected"
        assertThat(result.rationale()).contains("Simple Target").contains("simple.sch").doesNotContain("simple.xsd");
    }

    @Test
    public void testCancelledRunIsRejectedNamingTheStep() {
        final CTDetection notWellformed = Detection.of(CTStandardSeverity.ERROR, XmlDetection.CODE_NOT_WELLFORMED,
                DetectionLocation.of("broken.xml"), "not well-formed");

        final DecisionRecommendationResult result = this.action.executeCancelled(CTActionType.PARSE_DOCUMENT,
                DetectionList.of(notWellformed), "broken.xml");

        assertThat(result.status()).isEqualTo(CTStepResult.SUCCESS);
        assertThat(result.decision()).isEqualTo(CTDecision.REJECT);
        assertThat(result.rationale()).contains(CTActionType.PARSE_DOCUMENT.getName()).contains(XmlDetection.CODE_NOT_WELLFORMED);
        assertThat(only(result).getLocation().getResourceId()).isEqualTo("broken.xml");
    }

    @Test
    public void testInconclusiveTargetAsksForFurtherEvaluation() {
        // INCONCLUSIVE is not producible by step 8 yet (issue 04a), so the statement is built by hand
        final CTApplyRulesResult applied = applyRules(Simple.SIMPLE_VALID, "simple.xsd");
        final LinkedHashMap<CTPreparedRuleSet, CTConformanceStatement> statements = new LinkedHashMap<>();
        statements.put(applied.getResultsByRuleSet().keySet().iterator().next(),
                ConformanceStatement.of(TARGET, CTConformanceResult.INCONCLUSIVE, "acceptSelector could not be evaluated"));

        final DecisionRecommendationResult result = this.action.execute(new ComputeConformanceResult(applied, statements));

        assertThat(result.decision()).isEqualTo(CTDecision.EVALUATE_FURTHER);
        final CTDetection detection = only(result);
        assertThat(detection.getCode()).isEqualTo(DecisionRecommendationAction.CODE_EVALUATE_FURTHER);
        assertThat(detection.getSeverity()).isEqualTo(CTStandardSeverity.WARNING);
        assertThat(result.rationale()).contains("acceptSelector could not be evaluated");
    }

    @Test
    public void testEmptyConformanceIsRejected() {
        // step 8 skipped (nothing to evaluate) must not end in an acceptance
        final DecisionRecommendationResult result = this.action.execute(ComputeConformanceResult.empty(applyRules(Simple.SIMPLE_VALID)));

        assertThat(result.decision()).isEqualTo(CTDecision.REJECT);
        assertThat(result.rationale()).contains("No conformance statement");
    }
}
