package org.kosit.validator.impl.conformatron.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.scenario.CTScenarioMatch;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.junit.jupiter.api.Test;
import org.kosit.validator.TestHelper;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.TestScenarioBuilder;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.kosit.validator.impl.conformatron.model.ScenarioMatch;
import org.kosit.validator.testdata.TestResources;
import org.kost.validator.api.saxon.ProcessorProvider;

/**
 * Tests {@link DetectScenariosAction} (step 3) and {@link SelectScenarioAction} (step 4).
 */
public class SelectScenarioActionTest {

    /**
     * Casts the text of the first child to an integer; in simple.xml that text is "asldkfj", so it fails at runtime.
     */
    private static final String MATCH_FAILING_AT_RUNTIME = "xs:integer(/*/*[1]) = 1";

    private final SelectScenarioAction selectAction = new SelectScenarioAction();

    private static CTParsedValidationSource parseSimple() {
        // step 2 as the engine runs it; detection wraps the DOM into the model of the processor it is given
        return new ParseXmlAction().execute(TestHelper.read(TestResources.Simple.SIMPLE_VALID)).getParsedSource();
    }

    private static Scenario createScenario(final String name, final String match) {
        return TestScenarioBuilder.createScenario(name, match);
    }

    private static DetectScenariosAction detect(final Scenario... scenarios) {
        return new DetectScenariosAction(List.of(scenarios), ProcessorProvider.getProcessor());
    }

    private static CTScenarioMatch match(final String name) {
        return ScenarioMatch.of(createScenario(name, "/*"), parseSimple());
    }

    // --- step 3: DETECT_SCENARIOS ---

    @Test
    public void testDetectAcceptsDomParsedContentViaWrapping() {
        // the step-2 reference action produces a DOM source; a configured processor wraps it for the XPath matching
        final ParseXmlResult parsed = new ParseXmlAction().execute(TestHelper.read(TestResources.Simple.SIMPLE_VALID));

        final DetectScenariosResult result = detect(createScenario("simple", "/*")).execute(parsed.getParsedSource());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().get(0).getScenarioID()).isEqualTo("simple");
    }

    @Test
    public void testDetectRequiresXdmNodeContent() {
        final DetectScenariosAction action = detect(createScenario("simple", "/*"));
        assertThrows(NullPointerException.class, () -> action.execute(null));
    }

    @Test
    public void testDetectNeedsScenarios() {
        assertThrows(IllegalArgumentException.class, () -> new DetectScenariosAction(List.of()));
    }

    @Test
    public void testDetectSingleMatch() {
        final DetectScenariosResult result = detect(createScenario("simple", "/*")).execute(parseSimple());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().get(0).getScenarioID()).isEqualTo("simple");
        assertThat(result.matches().get(0).isUserSelected()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(DetectScenariosAction.CODE_SCENARIO_MATCHED);
    }

    @Test
    public void testDetectNoMatchFails() {
        final DetectScenariosResult result = detect(createScenario("other", "/no-such-element")).execute(parseSimple());

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.status()).isEqualTo(CTStepResult.FAILURE);
        assertThat(result.matches()).isEmpty();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(DetectScenariosAction.CODE_NO_SCENARIO_MATCHED);
        assertThat(result.detections().getWorstSeverity().getNumericLevel()).isEqualTo(CTStandardSeverity.ERROR.getNumericLevel());
    }

    @Test
    public void testAMatchThatCannotBeEvaluatedCancelsInsteadOfCountingAsNoMatch() {
        // the expression compiles - it is checked when the configuration is loaded - and only fails over this document
        final DetectScenariosResult result = detect(createScenario("broken", MATCH_FAILING_AT_RUNTIME)).execute(parseSimple());

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.status()).isEqualTo(CTStepResult.FAILURE);
        assertThat(result.matches()).isEmpty();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(DetectScenariosAction.CODE_SCENARIO_MATCH_ERROR);
        assertThat(result.detections().getWorstSeverity().getNumericLevel()).isEqualTo(CTStandardSeverity.ERROR.getNumericLevel());
        assertThat(result.detections().getAll().get(0).getText().getDisplayTextLocaleIndependent()).contains("broken");
    }

    @Test
    public void testAFailedMatchDoesNotLetAnotherScenarioTakeOver() {
        // the regression this guards: "could not tell" used to read as "does not apply", so the document was validated
        // against the next scenario - a verdict from a question the engine never answered, with nothing in the report
        final DetectScenariosResult result = detect(createScenario("broken", MATCH_FAILING_AT_RUNTIME), createScenario("simple", "/*"))
                .execute(parseSimple());

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.matches()).isEmpty();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(DetectScenariosAction.CODE_SCENARIO_MATCH_ERROR);
    }

    @Test
    public void testEveryBrokenMatchIsReportedNotJustTheFirst() {
        final DetectScenariosResult result = detect(createScenario("broken-1", MATCH_FAILING_AT_RUNTIME),
                createScenario("broken-2", MATCH_FAILING_AT_RUNTIME)).execute(parseSimple());

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(DetectScenariosAction.CODE_SCENARIO_MATCH_ERROR,
                DetectScenariosAction.CODE_SCENARIO_MATCH_ERROR);
        final List<String> texts = result.detections().getAll().stream().map(d -> d.getText().getDisplayTextLocaleIndependent()).toList();
        assertThat(texts).anySatisfy(t -> assertThat(t).contains("broken-1")).anySatisfy(t -> assertThat(t).contains("broken-2"));
    }

    @Test
    public void testDetectMultipleMatches() {
        final DetectScenariosResult result = detect(createScenario("first", "/*"), createScenario("second", "/*")).execute(parseSimple());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.matches()).hasSize(2);
        assertThat(result.detections().getCount()).isEqualTo(2);
    }

    @Test
    public void testAScenarioWithoutMatchAppliesUnconditionally() {
        // the conditional scenario does not match the document, the unconditional one is a candidate anyway
        final DetectScenariosResult result = detect(createScenario("other", "/no-such-element"), createScenario("always", null))
                .execute(parseSimple());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().get(0).getScenarioID()).isEqualTo("always");
        assertThat(result.matches().get(0).getMatchExpression()).isNull();
        assertThat(result.detections().getAll().get(0).getText().getDisplayTextLocaleIndependent()).contains("unconditionally");
    }

    @Test
    public void testDetectRequestedScenarioId() {
        final DetectScenariosResult result = detect(createScenario("simple", "/no-such-element")).execute(parseSimple(), "simple");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().get(0).isUserSelected()).isTrue();
        assertThat(result.matches().get(0).getMatchExpression()).isNull();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(DetectScenariosAction.CODE_SCENARIO_USER_SELECTED);
    }

    @Test
    public void testDetectUnknownRequestedIdFails() {
        final DetectScenariosResult result = detect(createScenario("simple", "/*")).execute(parseSimple(), "does-not-exist");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.matches()).isEmpty();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(DetectScenariosAction.CODE_SCENARIO_UNKNOWN_ID);
    }

    // --- step 4: SELECT_SCENARIO ---

    @Test
    public void testSelectSingleCandidate() {
        final CTScenarioMatch candidate = match("simple");
        final SelectScenarioAction.SelectScenarioResult result = this.selectAction.execute(List.of(candidate));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.selected()).isSameAs(candidate);
        assertThat(result.detections().getAll()).extracting("code").containsExactly(SelectScenarioAction.CODE_SCENARIO_SELECTED);
    }

    @Test
    public void testSelectAmbiguousFails() {
        final SelectScenarioAction.SelectScenarioResult result = this.selectAction.execute(List.of(match("first"), match("second")));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.selected()).isNull();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(SelectScenarioAction.CODE_SCENARIO_AMBIGUOUS);
        assertThat(result.detections().getAll().get(0).getText().getDisplayTextLocaleIndependent()).contains("first").contains("second");
    }

    @Test
    public void testSelectRejectsEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> this.selectAction.execute(List.of()));
        assertThrows(IllegalArgumentException.class, () -> this.selectAction.execute(null));
    }

    private static CTScenarioMatch unconditional(final String name) {
        return ScenarioMatch.of(createScenario(name, null), parseSimple());
    }

    @Test
    public void testUnconditionalScenariosAreAppliedInAddition() {
        final CTScenarioMatch matched = match("simple");
        final CTScenarioMatch always = unconditional("always");
        final SelectScenarioAction.SelectScenarioResult result = this.selectAction.execute(List.of(always, matched));

        assertThat(result.isSuccess()).isTrue();
        // the matched scenario leads, whatever the candidate order was
        assertThat(result.selected()).isSameAs(matched);
        assertThat(result.applied()).containsExactly(matched, always);
        assertThat(result.detections().getAll()).extracting("code").containsExactly(SelectScenarioAction.CODE_SCENARIO_SELECTED,
                SelectScenarioAction.CODE_SCENARIO_SELECTED);
        assertThat(result.detections().getAll().get(1).getText().getDisplayTextLocaleIndependent()).contains("in addition");
    }

    @Test
    public void testUnconditionalScenariosAloneAreSelected() {
        final CTScenarioMatch always = unconditional("always");
        final SelectScenarioAction.SelectScenarioResult result = this.selectAction.execute(List.of(always));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.selected()).isSameAs(always);
        assertThat(result.applied()).containsExactly(always);
    }

    @Test
    public void testUnconditionalScenariosDoNotMakeTheMatchAmbiguous() {
        // two matched candidates are ambiguous; the unconditional ones are not part of that question
        final SelectScenarioAction.SelectScenarioResult ambiguous = this.selectAction
                .execute(List.of(match("first"), unconditional("always"), match("second")));
        assertThat(ambiguous.isSuccess()).isFalse();
        assertThat(ambiguous.applied()).isEmpty();
        assertThat(ambiguous.detections().getAll().get(0).getText().getDisplayTextLocaleIndependent()).contains("first").contains("second")
                .doesNotContain("always");

        final SelectScenarioAction.SelectScenarioResult two = this.selectAction
                .execute(List.of(match("simple"), unconditional("always"), unconditional("also")));
        assertThat(two.isSuccess()).isTrue();
        assertThat(two.applied()).hasSize(3);
    }
}
