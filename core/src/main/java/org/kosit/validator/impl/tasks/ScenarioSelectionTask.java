package org.kosit.validator.impl.tasks;

import org.kosit.base.error.SimpleError;
import org.kosit.validator.impl.ActionMetadata;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.ScenarioRepository;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosResult;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.xvrl.XvrlDetectionBuilder;
import org.kosit.validator.xvrl.XvrlReportBuilder;
import org.kosit.xvrl.model.XvrlReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XdmNode;

/**
 * Identifies the scenario matching the input, if one is configured. Sets the fallback scenario if none could be
 * identified.
 *
 * @author Andreas Penski
 */
public class ScenarioSelectionTask implements CheckTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioSelectionTask.class);

    public static final Process.ProcessKey<Scenario, String> KEY = new Process.ProcessKey<>(Scenario.class, String.class);

    public static final ActionMetadata METADATA = new ActionMetadata("Scenario Selection", "scenario_selection");

    private final ScenarioRepository repository;

    private final DetectScenariosAction detectScenariosAction;

    private final SelectScenarioAction selectScenarioAction;

    private static XvrlReport generateXvrlReport(final SingleProcessingResult<Scenario, String> scenarioTypeResult, final String name) {
        final XvrlReportBuilder builder = XvrlReportBuilder.builder(METADATA);
        if (scenarioTypeResult.getObject().isFallback()) {
            builder.addDetection(XvrlDetectionBuilder.builderError().addMessage("No valid scenario configuration found for '" + name + "'")
                    .code("fallback-match"));
        } else {
            builder.addDetection(XvrlDetectionBuilder.builderInfo()
                    .addMessage("Scenario '" + scenarioTypeResult.getObject().getName() + "' identified for '" + name + "'")
                    .code("scenario-matched"));
            builder.addDetection(XvrlDetectionBuilder.builder().id("scenario").code(scenarioTypeResult.getObject().getName()));
        }
        return builder.build();
    }

    @Override
    public ProcessStepResult<Scenario, String> check(final Process results) {
        final SingleProcessingResult<Scenario, String> scenarioTypeResult;
        final SingleProcessingResult<XdmNode, SimpleError> parseResult = results.getResult(DocumentParseTask.KEY);
        if (parseResult.isValid()) {
            scenarioTypeResult = determineScenario(parseResult.getObject());
        } else {
            scenarioTypeResult = new SingleProcessingResult<>(this.repository.getFallbackScenario());
        }
        if (!scenarioTypeResult.getObject().isFallback()) {
            LOGGER.info("Scenario \'{}\' identified for \'{}\'", scenarioTypeResult.getObject().getName(), results.getInput().getName());
        } else {
            LOGGER.info("No valid scenario configuration found for \'{}\'", results.getInput().getName());
        }
        runConformatronScenarioSteps(results);
        final ProcessStepResult<Scenario, String> result = new ProcessStepResult<>(ScenarioSelectionTask.KEY);
        result.setResult(scenarioTypeResult);
        result.setReport(generateXvrlReport(scenarioTypeResult, results.getInput().getName()));
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
        final DetectScenariosResult detected = this.detectScenariosAction.execute(results.getParsedSource());
        if (!detected.isSuccess()) {
            return;
        }
        results.setScenarioMatches(detected.matches());
        final SelectScenarioAction.SelectScenarioResult selected = this.selectScenarioAction.execute(detected.matches());
        if (selected.isSuccess()) {
            results.setScenarioMatch(selected.selected());
        }
    }

    private SingleProcessingResult<Scenario, String> determineScenario(final XdmNode document) {
        final SingleProcessingResult<Scenario, String> result = this.repository.selectScenario(document);
        if (result.isInvalid()) {
            return new SingleProcessingResult<>(this.repository.getFallbackScenario());
        }
        return result;
    }

    public ScenarioSelectionTask(final ScenarioRepository repository) {
        this.repository = repository;
        this.detectScenariosAction = new DetectScenariosAction(repository);
        this.selectScenarioAction = new SelectScenarioAction();
    }
}
