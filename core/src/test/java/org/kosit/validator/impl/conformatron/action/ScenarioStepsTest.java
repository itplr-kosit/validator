package org.kosit.validator.impl.conformatron.action;

import org.kosit.validator.impl.conformatron.model.XdmNodeValidationSource;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLResult;
import org.kosit.validator.impl.conformatron.model.ScenarioMatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kosit.validator.api.InputFactory.read;

import java.util.List;

import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.detection.ECTSeverity;
import org.conformatron.api.model.scenario.ICTScenarioMatch;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VInput;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.ScenarioRepository;
import org.kosit.validator.impl.xml.ProcessorProvider;
import org.kosit.validator.impl.tasks.DocumentParseAction;
import org.kosit.validator.impl.tasks.TestScenarioBuilder;

/**
 * Tests {@link DetectScenariosAction} (step 3) and {@link SelectScenarioAction} (step 4) against the legacy scenario
 * machinery via the facade types.
 */
public class ScenarioStepsTest {

    private final SelectScenarioAction selectAction = new SelectScenarioAction();

    private static XdmNodeValidationSource parseSimple() {
        final VInput VInput = read(Simple.SIMPLE_VALID);
        // same processor as the scenario match executables (Saxon configuration compatibility)
        final DocumentParseAction.ParseOutcome outcome = new DocumentParseAction(ProcessorProvider.getProcessor()).parseRetaining(VInput);
        return outcome.parsedSource();
    }

    private static Scenario createScenario(final String name, final String match) {
        final Scenario scenario = TestScenarioBuilder.createDefault();
        scenario.getConfiguration().setName(name);
        scenario.getConfiguration().setMatch(match);
        return scenario;
    }

    private static ICTScenarioMatch match(final String name) {
        return ScenarioMatch.of(createScenario(name, "/*"), parseSimple());
    }

    // --- step 3: DETECT_SCENARIOS ---

    @Test
    public void testDetectAcceptsDomParsedContentViaWrapping() {
        // the step-2 reference action produces a DOM source; a configured processor wraps it for the XPath matching
        final ParseXMLResult parsed = new ParseXMLAction().execute(read(Simple.SIMPLE_VALID));
        final ScenarioRepository repository = TestScenarioBuilder.createRepository(createScenario("simple", "/*"));

        final DetectScenariosAction.DetectScenariosResult result = new DetectScenariosAction(repository, ProcessorProvider.getProcessor())
                .execute(parsed.getParsedSource());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().get(0).getScenarioID()).isEqualTo("simple");
    }

    @Test
    public void testDetectRequiresXdmNodeContent() {
        final ScenarioRepository repository = TestScenarioBuilder.createRepository(createScenario("simple", "/*"));
        final DetectScenariosAction action = new DetectScenariosAction(repository);
        assertThrows(IllegalArgumentException.class, () -> action.execute(null));
    }

    @Test
    public void testDetectSingleMatch() {
        final ScenarioRepository repository = TestScenarioBuilder.createRepository(createScenario("simple", "/*"));
        final DetectScenariosAction.DetectScenariosResult result = new DetectScenariosAction(repository).execute(parseSimple());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().get(0).getScenarioID()).isEqualTo("simple");
        assertThat(result.matches().get(0).isUserSelected()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(DetectScenariosAction.CODE_SCENARIO_MATCHED);
    }

    @Test
    public void testDetectNoMatchFails() {
        final ScenarioRepository repository = TestScenarioBuilder.createRepository(createScenario("other", "/no-such-element"));
        final DetectScenariosAction.DetectScenariosResult result = new DetectScenariosAction(repository).execute(parseSimple());

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.status()).isEqualTo(ECTStepResult.FAILURE);
        assertThat(result.matches()).isEmpty();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(DetectScenariosAction.CODE_NO_SCENARIO_MATCHED);
        assertThat(result.detections().getWorstSeverity().getNumericLevel()).isEqualTo(ECTSeverity.ERROR.getNumericLevel());
    }

    @Test
    public void testDetectMultipleMatches() {
        final ScenarioRepository repository = TestScenarioBuilder.createRepository(createScenario("first", "/*"),
                createScenario("second", "/*"));
        final DetectScenariosAction.DetectScenariosResult result = new DetectScenariosAction(repository).execute(parseSimple());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.matches()).hasSize(2);
        assertThat(result.detections().getCount()).isEqualTo(2);
    }

    @Test
    public void testDetectRequestedScenarioId() {
        final ScenarioRepository repository = TestScenarioBuilder.createRepository(createScenario("simple", "/no-such-element"));
        final DetectScenariosAction.DetectScenariosResult result = new DetectScenariosAction(repository).execute(parseSimple(), "simple");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().get(0).isUserSelected()).isTrue();
        assertThat(result.matches().get(0).getMatchExpression()).isNull();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(DetectScenariosAction.CODE_SCENARIO_USER_SELECTED);
    }

    @Test
    public void testDetectUnknownRequestedIdFails() {
        final ScenarioRepository repository = TestScenarioBuilder.createRepository(createScenario("simple", "/*"));
        final DetectScenariosAction.DetectScenariosResult result = new DetectScenariosAction(repository).execute(parseSimple(),
                "does-not-exist");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.matches()).isEmpty();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(DetectScenariosAction.CODE_SCENARIO_UNKNOWN_ID);
    }

    // --- step 4: SELECT_SCENARIO ---

    @Test
    public void testSelectSingleCandidate() {
        final ICTScenarioMatch candidate = match("simple");
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
}
