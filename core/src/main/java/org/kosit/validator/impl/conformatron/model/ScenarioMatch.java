package org.kosit.validator.impl.conformatron.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.conformatron.api.model.scenario.CTScenarioMatch;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.conformatron.api.model.validation.CTValidationArtifactReference;
import org.kosit.cvr.model.SeverityOverrides;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.scenario.v2.ResourceType;
import org.kosit.validator.scenario.v2.ScenarioType;
import org.kosit.validator.scenario.v2.ValidateWithSchematron;

/**
 * Validator implementation of {@link CTScenarioMatch} (conformatron-api steps 3+4): a {@link Scenario} that applies to
 * the document, as the handshake object the downstream steps consume. The scenario itself stays reachable through
 * {@link #getScenario()}, because steps 5 and 6 need its artifact repository.
 * <p>
 * Known limitations of the framework/2 scenario model:
 * </p>
 * <ul>
 * <li>A scenario has no separate ID — {@link #getScenarioID()} falls back to the scenario name.</li>
 * <li>The XPath match does not expose the matched value — {@link #getMatchedValue()} is {@code null}.</li>
 * </ul>
 *
 * @author Andreas Schmitz
 */
public final class ScenarioMatch implements CTScenarioMatch {

    private final Scenario scenario;

    private final boolean userSelected;

    private final List<CTValidationArtifactReference> artifactReferences;

    private final CTParsedValidationSource parsedSource;

    private final SeverityOverrides severityOverrides;

    private ScenarioMatch(final Scenario scenario, final boolean userSelected, final CTParsedValidationSource parsedSource) {
        this.scenario = scenario;
        this.userSelected = userSelected;
        this.artifactReferences = collectArtifactReferences(scenario);
        this.parsedSource = parsedSource;
        this.severityOverrides = ScenarioSeverityOverrides.fromConfiguration(scenario.getConfiguration());
    }

    /**
     * Wraps an auto-detected scenario as conformatron handshake object.
     *
     * @param scenario the matched scenario
     * @param parsedSource the parsed source from step 2, carried through per specification
     * @return the wrapped match
     */
    public static ScenarioMatch of(final Scenario scenario, final CTParsedValidationSource parsedSource) {
        if (scenario == null) {
            throw new IllegalArgumentException("scenario may not be null");
        }
        if (parsedSource == null) {
            throw new IllegalArgumentException("parsedSource may not be null");
        }
        return new ScenarioMatch(scenario, false, parsedSource);
    }

    /**
     * Wraps a scenario that was fixed by explicit user input (conformatron-api step 3, {@code requestedScenarioId}
     * path): no XPath evaluation happened, so match expression and matched value are {@code null} per
     * {@link CTScenarioMatch} contract.
     *
     * @param scenario the user-requested scenario
     * @param parsedSource the parsed source from step 2, carried through per specification
     * @return the wrapped match with {@link #isUserSelected()} {@code == true}
     */
    public static ScenarioMatch userSelected(final Scenario scenario, final CTParsedValidationSource parsedSource) {
        if (scenario == null) {
            throw new IllegalArgumentException("scenario may not be null");
        }
        if (parsedSource == null) {
            throw new IllegalArgumentException("parsedSource may not be null");
        }
        return new ScenarioMatch(scenario, true, parsedSource);
    }

    /** @return the scenario that matched, with its artifact repository */
    public Scenario getScenario() {
        return this.scenario;
    }

    /**
     * The scenario's {@code customLevel} severity overrides, applied by step 7 ({@code APPLY_RULES}); never
     * {@code null} — a scenario without overrides yields {@link SeverityOverrides#NONE}.
     */
    public SeverityOverrides getSeverityOverrides() {
        return this.severityOverrides;
    }

    /**
     * The configuration file this scenario was read from. May be <code>null</code> for a scenario assembled in code.
     */
    public String getDefinitionFile() {
        return this.scenario.getDefinitionFile();
    }

    /** The wrapped scenario configuration, for embedding the individual scenario into the report. */
    public ScenarioType getConfiguration() {
        return this.scenario.getConfiguration();
    }

    /**
     * A pointer into the scenario configuration that locates this scenario, so a report consumer can look it up
     * quickly. The framework/2 scenario model has no separate id, so the pointer selects by name.
     *
     * @return an XPath expression selecting this scenario within the scenario configuration
     */
    public String getConfigurationLocation() {
        return "/*:scenarios/*:scenario[*:name='" + this.scenario.getName() + "']";
    }

    private static List<CTValidationArtifactReference> collectArtifactReferences(final Scenario scenario) {
        final ScenarioType configuration = scenario.getConfiguration();
        final List<CTValidationArtifactReference> references = new ArrayList<>();
        if (configuration.getValidateWithXmlSchema() != null) {
            for (final ResourceType resource : configuration.getValidateWithXmlSchema().getResource()) {
                references.add(ScenarioRuleSetReference.of(resource.getLocation(), null, handedOver(scenario, resource.getLocation())));
            }
        }
        for (final ValidateWithSchematron schematron : configuration.getValidateWithSchematron()) {
            if (schematron.getResource() != null) {
                // the rule set carries the processor the scenario names for it, step 6 honours it
                references.add(ScenarioRuleSetReference.of(schematron.getResource().getLocation(), schematron.getCompiler(),
                        handedOver(scenario, schematron.getResource().getLocation())));
            }
        }
        return List.copyOf(references);
    }

    private static org.conformatron.api.model.validation.CTCompiledValidationArtifact<?> handedOver(final Scenario scenario,
            final String location) {
        return scenario.precompiled(URI.create(location)).orElse(null);
    }

    @Override
    public String getScenarioID() {
        return this.scenario.getName();
    }

    @Override
    public String getScenarioName() {
        return this.scenario.getName();
    }

    @Override
    public String getMatchExpression() {
        return this.userSelected ? null : this.scenario.getConfiguration().getMatch();
    }

    @Override
    public String getMatchedValue() {
        // the XPath match does not expose the matched document value
        return null;
    }

    @Override
    public boolean isUserSelected() {
        return this.userSelected;
    }

    @Override
    public List<CTValidationArtifactReference> getArtifactReferences() {
        return this.artifactReferences;
    }

    @Override
    public CTParsedValidationSource getParsedSource() {
        return this.parsedSource;
    }
}
