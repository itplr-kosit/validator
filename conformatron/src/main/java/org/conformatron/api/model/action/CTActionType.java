package org.conformatron.api.model.action;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;

/**
 * Pipeline action types.
 * <p>
 * The validation pipeline processes a document through a deterministic sequence of actions. Each action produces a
 * report. This enum provides the canonical identifiers used in CSVR/XVRL metadata ({@code <creator @name>}).
 * </p>
 * <p>
 * The pipeline order is the order in the enum.
 * </p>
 * <p>
 * Note: Custom actions beyond these canonical ones are permitted — the pipeline is extensible. These canonical names
 * serve as the well-known vocabulary.
 * </p>
 *
 * @author Andreas Schmitz
 * @author Philip Helger
 */
public enum CTActionType {

    /** Step 1: detect syntax (XML, JSON, EDIFACT, ...) */
    DETECT_SYNTAX("detect-syntax"),

    /** Step 2: Load and parse the source document. Checks well-formedness. */
    PARSE_DOCUMENT("parse-document"),

    /**
     * Step 3: Read in from user input or identify which validation scenario matches the document (via XPath) if the
     * input is missing. Result: Validation Scenario
     */
    DETECT_SCENARIOS("detect-scenarios"),

    /** Step 4: select the specific scenario */
    SELECT_SCENARIO("select-scenario"),

    /**
     * Step 5: Retrieve validation artifacts (schemas, Schematron files) from repository. Result: Validation Artifacts
     * are ready for application
     */
    RETRIEVE_ARTIFACTS("retrieve-artifacts"),

    /**
     * Step 6: Convert e.g. SCH to XSLT as well as XSLT to Java templates, so that rules are optimized for application
     */
    PREPARE_RULES("prepare-rules"),

    /**
     * Step 7: Rule-based validation using Schematron (compiled or pure). + XSD and Structure This action can occur
     * multiple times in the pipeline — once per Schematron rule set, in a determined fixed order. Results:
     * ICTDetectionList
     */
    APPLY_RULES("apply-rules"),

    /**
     * Step 8: Compute the final decision recommendation: accept, reject, or evaluate further. Result: Decision
     * Recommendation (Maybe based on ICTReportDigest, that is right now mostly redundant to ICTDetectionList)
     */
    COMPUTE_CONFORMANCE("compute-conformance"), DECISION_RECOMMENDATION("decision-recommendation");

    private final String name;

    CTActionType(@NonNull @Nonempty final String name) {
        this.name = name;
    }

    /**
     * @return The canonical name as used in XVRL {@code <creator @name>}. This is the stable identifier for
     *         serialization and matching.
     */
    @NonNull
    @Nonempty
    public String getName() {
        return name;
    }
}
