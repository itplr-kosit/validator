package org.kosit.validator.impl.conformatron.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.conformatron.api.model.scenario.ICTScenarioMatch;
import org.conformatron.api.model.source.ICTParsedValidationSource;
import org.conformatron.api.model.source.ICTValidationArtifactReference;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.model.scenarios.ResourceType;
import org.kosit.validator.model.scenarios.ScenarioType;
import org.kosit.validator.model.scenarios.ValidateWithSchematron;

/**
 * Validator implementation of {@link ICTScenarioMatch} (conformatron-api steps 3+4). Facade: wraps the legacy
 * {@link Scenario} selected by the existing {@code ScenarioSelectionAction} so downstream steps can consume the
 * conformatron handshake type while the legacy scenario machinery keeps doing the heavy lifting.
 * <p>
 * Known facade limitations (to be resolved when the scenario steps are fully migrated):
 * </p>
 * <ul>
 * <li>The legacy scenario model has no separate ID — {@link #getScenarioID()} falls back to the scenario name.</li>
 * <li>The legacy XPath selector does not expose the matched value — {@link #getMatchedValue()} is {@code null}.</li>
 * <li>Fallback scenarios are not representable as a match (neither auto-detected nor user-selected) and must not be
 * wrapped; callers keep the handshake object {@code null} in that case.</li>
 * </ul>
 *
 * @author Andreas Schmitz
 */
public final class ScenarioMatch implements ICTScenarioMatch {

    private final String scenarioId;

    private final String scenarioName;

    private final String matchExpression;

    private final boolean userSelected;

    private final List<ICTValidationArtifactReference> artifactReferences;

    private final ICTParsedValidationSource parsedSource;

    private ScenarioMatch(final String scenarioId, final String scenarioName, final String matchExpression, final boolean userSelected,
            final List<ICTValidationArtifactReference> artifactReferences, final ICTParsedValidationSource parsedSource) {
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
        this.matchExpression = matchExpression;
        this.userSelected = userSelected;
        this.artifactReferences = List.copyOf(artifactReferences);
        this.parsedSource = parsedSource;
    }

    /**
     * Wraps a legacy auto-detected scenario as conformatron handshake object.
     *
     * @param scenario the matched legacy scenario; must not be a fallback scenario (see class Javadoc)
     * @param parsedSource the parsed source from step 2, carried through per specification
     * @return the wrapped match
     */
    public static ScenarioMatch of(final Scenario scenario, final ICTParsedValidationSource parsedSource) {
        if (scenario == null) {
            throw new IllegalArgumentException("scenario may not be null");
        }
        if (scenario.isFallback()) {
            throw new IllegalArgumentException("A fallback scenario is not a match and can not be wrapped");
        }
        if (parsedSource == null) {
            throw new IllegalArgumentException("parsedSource may not be null");
        }
        final ScenarioType configuration = scenario.getConfiguration();
        return new ScenarioMatch(scenario.getName(), scenario.getName(), configuration.getMatch(), false,
                collectArtifactReferences(configuration), parsedSource);
    }

    /**
     * Wraps a legacy scenario that was fixed by explicit user input (conformatron-api step 3,
     * {@code requestedScenarioId} path): no XPath evaluation happened, so match expression and matched value are
     * {@code null} per {@link ICTScenarioMatch} contract.
     *
     * @param scenario the user-requested legacy scenario; must not be a fallback scenario (see class Javadoc)
     * @param parsedSource the parsed source from step 2, carried through per specification
     * @return the wrapped match with {@link #isUserSelected()} {@code == true}
     */
    public static ScenarioMatch userSelected(final Scenario scenario, final ICTParsedValidationSource parsedSource) {
        if (scenario == null) {
            throw new IllegalArgumentException("scenario may not be null");
        }
        if (scenario.isFallback()) {
            throw new IllegalArgumentException("A fallback scenario is not a match and can not be wrapped");
        }
        if (parsedSource == null) {
            throw new IllegalArgumentException("parsedSource may not be null");
        }
        return new ScenarioMatch(scenario.getName(), scenario.getName(), null, true, collectArtifactReferences(scenario.getConfiguration()),
                parsedSource);
    }

    private static List<ICTValidationArtifactReference> collectArtifactReferences(final ScenarioType configuration) {
        if (configuration == null) {
            return Collections.emptyList();
        }
        final List<ICTValidationArtifactReference> references = new ArrayList<>();
        if (configuration.getValidateWithXmlSchema() != null) {
            configuration.getValidateWithXmlSchema().getResource().stream().map(ResourceType::getLocation)
                    .map(ValidationArtifactReference::of).forEach(references::add);
        }
        for (final ValidateWithSchematron schematron : configuration.getValidateWithSchematron()) {
            if (schematron.getResource() != null) {
                references.add(ValidationArtifactReference.of(schematron.getResource().getLocation()));
            }
        }
        return references;
    }

    @Override
    public String getScenarioID() {
        return this.scenarioId;
    }

    @Override
    public String getScenarioName() {
        return this.scenarioName;
    }

    @Override
    public String getMatchExpression() {
        return this.matchExpression;
    }

    @Override
    public String getMatchedValue() {
        // the legacy XPath selector does not expose the matched document value
        return null;
    }

    @Override
    public boolean isUserSelected() {
        return this.userSelected;
    }

    @Override
    public List<ICTValidationArtifactReference> getArtifactReferences() {
        return this.artifactReferences;
    }

    @Override
    public ICTParsedValidationSource getParsedSource() {
        return this.parsedSource;
    }
}
