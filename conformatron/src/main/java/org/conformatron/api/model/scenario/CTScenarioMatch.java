package org.conformatron.api.model.scenario;

import java.util.List;

import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.conformatron.api.model.validation.CTValidationArtifactReference;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The result of {@code SELECT_SCENARIO} (step 4): exactly one matched scenario, carrying the identity and match
 * provenance needed by downstream steps.
 * <p>
 * Produced by step 4 and consumed by step 5 ({@code RETRIEVE_ARTIFACTS}).
 * </p>
 */
public interface CTScenarioMatch {

    /**
     * @return The unique identifier of the matched scenario (e.g. {@code "xrechnung-ubl-invoice-3.0"}).
     */
    @NonNull
    @Nonempty
    String getScenarioID();

    /**
     * @return Human-readable name of the matched scenario (e.g. {@code "XRechnung UBL Invoice 3.0"}).
     */
    @NonNull
    @Nonempty
    String getScenarioName();

    /**
     * @return The XPath match expression that identified this scenario, or {@code null} if the scenario was selected by
     *         explicit user input (not auto-detected).
     */
    @Nullable
    String getMatchExpression();

    /**
     * @return The matched value from the document (e.g. the {@code CustomizationID} content), or {@code null} if
     *         selected by explicit user input.
     */
    @Nullable
    String getMatchedValue();

    /**
     * @return {@code true} if the scenario was selected by explicit user input rather than XPath auto-detection.
     */
    boolean isUserSelected();

    /**
     * @return The artifact references declared by this scenario, to be resolved in step 5. Each entry is a URI/path
     *         relative to the artifact repository.
     */
    @NonNull
    List<CTValidationArtifactReference> getArtifactReferences();

    /**
     * @return The parsed validation source carried through from step 2.
     */
    @NonNull
    CTParsedValidationSource getParsedSource();
}
