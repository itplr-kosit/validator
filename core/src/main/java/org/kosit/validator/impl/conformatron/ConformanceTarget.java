package org.kosit.validator.impl.conformatron;

import java.util.List;

import org.conformatron.api.model.scenario.ICTConformanceTarget;
import org.conformatron.api.model.scenario.ICTScenarioMatch;
import org.conformatron.api.model.source.ICTValidationArtifactReference;

/**
 * Validator implementation of {@link ICTConformanceTarget} (conformatron-api step 8, {@code COMPUTE_CONFORMANCE}): a
 * named group of rule sets against which conformance is evaluated.
 * <p>
 * Facade note: the legacy scenario model declares no conformance targets — {@link #ofScenario(ICTScenarioMatch)}
 * derives a single scenario-wide target covering all rule sets of the selected scenario. The legacy {@code acceptMatch}
 * is deliberately <b>not</b> carried over as {@code acceptSelector}: it is evaluated against the rendered report, which
 * does not exist in the canonical pipeline yet (ADR-004 follow-up).
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class ConformanceTarget implements ICTConformanceTarget {

    private final String targetId;

    private final String targetName;

    private final List<String> ruleSetReferences;

    private final String acceptSelector;

    private ConformanceTarget(final String targetId, final String targetName, final List<String> ruleSetReferences,
            final String acceptSelector) {
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("targetId may not be null or blank");
        }
        if (targetName == null || targetName.isBlank()) {
            throw new IllegalArgumentException("targetName may not be null or blank");
        }
        if (ruleSetReferences == null) {
            throw new IllegalArgumentException("ruleSetReferences may not be null");
        }
        this.targetId = targetId;
        this.targetName = targetName;
        this.ruleSetReferences = List.copyOf(ruleSetReferences);
        this.acceptSelector = acceptSelector;
    }

    /**
     * Creates a target from explicit values.
     *
     * @param targetId unique identifier (e.g. {@code "xrechnung-3.0"})
     * @param targetName display name
     * @param ruleSetReferences the rule set references (hrefs) this target is evaluated against, in order
     * @param acceptSelector optional XPath overriding the detection-based evaluation; may be {@code null}
     * @return the target
     */
    public static ConformanceTarget of(final String targetId, final String targetName, final List<String> ruleSetReferences,
            final String acceptSelector) {
        return new ConformanceTarget(targetId, targetName, ruleSetReferences, acceptSelector);
    }

    /**
     * Derives the scenario-wide default target: one target covering every rule set of the selected scenario, no accept
     * selector (see class Javadoc).
     *
     * @param scenario the scenario selected in step 4
     * @return the derived target
     */
    public static ConformanceTarget ofScenario(final ICTScenarioMatch scenario) {
        if (scenario == null) {
            throw new IllegalArgumentException("scenario may not be null");
        }
        final List<String> references = scenario.getArtifactReferences().stream()
                .map(ICTValidationArtifactReference::getValidationArtifactReference).map(Object::toString).toList();
        return new ConformanceTarget(scenario.getScenarioID(), scenario.getScenarioName(), references, null);
    }

    @Override
    public String getTargetID() {
        return this.targetId;
    }

    @Override
    public String getTargetName() {
        return this.targetName;
    }

    @Override
    public List<String> getRuleSetReferences() {
        return this.ruleSetReferences;
    }

    @Override
    public String getAcceptSelector() {
        return this.acceptSelector;
    }
}
