package org.kosit.validator.impl.conformatron;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.xml.validation.Schema;

import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.source.ICTResolvedValidationArtifact;
import org.conformatron.api.model.validation.ECTValidationType;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.conformatron.PrepareRulesAction.PrepareRulesResult;
import org.kosit.validator.impl.conformatron.RetrieveArtifactsAction.RetrieveArtifactsResult;

import net.sf.saxon.s9api.XsltExecutable;

/**
 * Tests {@link PrepareRulesAction} (step 6) on the artifacts produced by {@link RetrieveArtifactsAction} (step 5).
 */
public class PrepareRulesActionTest {

    private static final String DOCUMENT = "simple.xml";

    private final ContentRepository repository = new ContentRepository(Helper.getTestProcessor(),
            ResolvingMode.STRICT_RELATIVE.getStrategy(), Simple.REPOSITORY_URI);

    private final PrepareRulesAction action = new PrepareRulesAction(this.repository);

    private static List<ICTResolvedValidationArtifact> retrieve(final String... references) {
        final RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(Simple.REPOSITORY_URI).execute(List.of(references).stream()
                .map(r -> (org.conformatron.api.model.source.ICTValidationArtifactReference) ValidationArtifactReference.of(r)).toList(),
                DOCUMENT);
        assertThat(retrieved.isSuccess()).isTrue();
        return retrieved.artifacts();
    }

    @Test
    public void testCompilesSchematronAndSchema() {
        final PrepareRulesResult result = this.action.execute(retrieve("simple.xsd", "simple.sch"), DOCUMENT);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.ruleSets()).hasSize(2);

        final var xsd = result.ruleSets().get(0);
        assertThat(xsd.getEngineType()).isEqualTo(ECTValidationType.XSD);
        assertThat(xsd.getCompiledArtifact().getCompilation()).isInstanceOf(Schema.class);
        assertThat(xsd.getOutputFormatName()).isNull();
        assertThat(xsd.getPhase()).isNull();

        final var schematron = result.ruleSets().get(1);
        assertThat(schematron.getEngineType()).isEqualTo(ECTValidationType.SCHEMATRON_SCH);
        assertThat(schematron.getCompiledArtifact().getCompilation()).isInstanceOf(XsltExecutable.class);
        assertThat(schematron.getOutputFormatName()).isEqualTo(PreparedRuleSet.OUTPUT_FORMAT_SVRL);
        assertThat(schematron.getPhase()).isEqualTo(PreparedRuleSet.PHASE_ALL);
        assertThat(schematron.getEngineVersion()).isNotBlank();
        assertThat(schematron.getArtifactReference().getValidationArtifactReference().toString()).isEqualTo("simple.sch");

        assertThat(result.detections().getAll()).extracting("code").containsExactly(PrepareRulesAction.CODE_RULE_COMPILED,
                PrepareRulesAction.CODE_RULE_COMPILED);
    }

    @Test
    public void testAheadOfTimeTranspiledXsltIsPassThrough() {
        final PrepareRulesResult result = this.action.execute(retrieve("simple.xsl"), DOCUMENT);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(PrepareRulesAction.CODE_RULE_PRECOMPILED);
    }

    @Test
    public void testAlreadyCompiledArtifactIsPassedThrough() {
        final PrepareRulesResult prepared = this.action.execute(retrieve("simple.sch"), DOCUMENT);
        final var compiled = prepared.ruleSets().get(0).getCompiledArtifact();

        final ICTResolvedValidationArtifact precompiled = ResolvedValidationArtifact
                .precompiled(ValidationArtifactReference.of("simple.sch"), compiled);
        final PrepareRulesResult result = this.action.execute(List.of(precompiled), DOCUMENT);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.ruleSets().get(0).getCompiledArtifact()).isSameAs(compiled);
        assertThat(result.detections().getAll()).extracting("code").containsExactly(PrepareRulesAction.CODE_RULE_PRECOMPILED);
    }

    @Test
    public void testBrokenSchematronFailsTheStep() {
        final ICTResolvedValidationArtifact broken = ResolvedValidationArtifact.loaded(
                ValidationArtifactReference.of("does-not-compile.sch"), ECTValidationType.SCHEMATRON_SCH, "not a schematron".getBytes());

        final PrepareRulesResult result = this.action.execute(List.of(broken), DOCUMENT);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.status()).isEqualTo(ECTStepResult.FAILURE);
        assertThat(result.detections().getAll()).extracting("code").containsExactly(PrepareRulesAction.CODE_RULE_PREPARE_ERROR);
        assertThat(result.detections().getWorstSeverity().isFatalError()).isTrue();
    }

    @Test
    public void testNoArtifactsSkipsTheStep() {
        final PrepareRulesResult result = this.action.execute(List.of(), DOCUMENT);

        assertThat(result.status()).isEqualTo(ECTStepResult.SKIPPED);
        assertThat(result.ruleSets()).isEmpty();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(PrepareRulesAction.CODE_STEP_SKIPPED);
    }
}
