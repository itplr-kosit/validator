package org.kosit.validator.impl.tasks;

import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.detection;

import org.kosit.validator.impl.ActionMetadata;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.ScenarioRepository;
import org.kosit.validator.impl.conformatron.DetectScenariosAction;
import org.kosit.validator.impl.conformatron.SelectScenarioAction;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.xvrl.XVRLReportBuilder;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.xvrl.model.XVRLDetection;
import org.kosit.xvrl.model.XVRLReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XdmNode;

/**
 * Identifies the scenario matching the input, if one is configured. Sets the fallback scenario if none could be
 * identified.
 *
 * @author Andreas Penski
 */
public class ScenarioSelectionAction implements CheckAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioSelectionAction.class);

    public static final Process.Key<Scenario, String> KEY = new Process.Key<>(Scenario.class, String.class);

    public static final ActionMetadata METADATA = new ActionMetadata("Scenario Selection", "scenario_selection");

    private final ScenarioRepository repository;

    private final DetectScenariosAction detectScenariosAction;

    private final SelectScenarioAction selectScenarioAction;

    private static XVRLReport generateXVRLReport(final Result<Scenario, String> scenarioTypeResult, final String name) {
        final XVRLReportBuilder builder = XVRLReportBuilder.builder(METADATA);
        if (scenarioTypeResult.getObject().isFallback()) {
            builder.add(detection().addError("No valid scenario configuration found for '" + name + "'").code("fallback-match"));
        } else {
            builder.add(detection().addMessage("Scenario '" + scenarioTypeResult.getObject().getName() + "' identified for '" + name + "'")
                    .severity(XVRLDetection.Severity.INFO).code("scenario-matched"));
            builder.add(detection().id("scenario").code(scenarioTypeResult.getObject().getName()));
        }
        return builder.build();
    }

    @Override
    public ProcessStepResult<Scenario, String> check(final Process results) {
        final Result<Scenario, String> scenarioTypeResult;
        final Result<XdmNode, XMLSyntaxError> parseResult = results.getResult(DocumentParseAction.KEY);
        if (parseResult.isValid()) {
            scenarioTypeResult = determineScenario(parseResult.getObject());
        } else {
            scenarioTypeResult = new Result<>(this.repository.getFallbackScenario());
        }
        if (!scenarioTypeResult.getObject().isFallback()) {
            LOGGER.info("Scenario \'{}\' identified for \'{}\'", scenarioTypeResult.getObject().getName(), results.getInput().getName());
        } else {
            LOGGER.info("No valid scenario configuration found for \'{}\'", results.getInput().getName());
        }
        runConformatronScenarioSteps(results);
        final ProcessStepResult<Scenario, String> result = new ProcessStepResult<>(ScenarioSelectionAction.KEY);
        result.setResult(scenarioTypeResult);
        result.setReport(generateXVRLReport(scenarioTypeResult, results.getInput().getName()));
        return result;
    }

    /**
     * Facade migration (conformatron-api steps 3+4): runs {@link DetectScenariosAction} and
     * {@link SelectScenarioAction} to expose the handshake objects via {@link Process#getScenarioMatches()} and
     * {@link Process#getScenarioMatch()}. The legacy behavior above (fallback scenario on zero/ambiguous matches) is
     * deliberately untouched; on those paths only the new-API objects reflect the failure. The double XPath evaluation
     * (legacy selection + detection) is a temporary cost of running both worlds in parallel and disappears with the
     * legacy path.
     */
    private void runConformatronScenarioSteps(final Process results) {
        if (results.getParsedSource() == null) {
            return;
        }
        final DetectScenariosAction.DetectScenariosResult detected = this.detectScenariosAction.execute(results.getParsedSource());
        if (!detected.isSuccess()) {
            return;
        }
        results.setScenarioMatches(detected.matches());
        final SelectScenarioAction.SelectScenarioResult selected = this.selectScenarioAction.execute(detected.matches());
        if (selected.isSuccess()) {
            results.setScenarioMatch(selected.selected());
        }
    }

    private Result<Scenario, String> determineScenario(final XdmNode document) {
        final Result<Scenario, String> result = this.repository.selectScenario(document);
        if (result.isInvalid()) {
            return new Result<>(this.repository.getFallbackScenario());
        }
        return result;
    }

    public ScenarioSelectionAction(final ScenarioRepository repository) {
        this.repository = repository;
        this.detectScenariosAction = new DetectScenariosAction(repository);
        this.selectScenarioAction = new SelectScenarioAction();
    }
}
