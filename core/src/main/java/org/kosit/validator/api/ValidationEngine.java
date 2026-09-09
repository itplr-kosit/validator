package org.kosit.validator.api;

import org.conformatron.api.model.source.CTReadResource;

/**
 * The validation engine contract of the validator (successor of the legacy {@code VCheck} interface, see ADR-008): an
 * engine validates a document and returns its result. This interface is a <b>pure contract</b> — behavior lives in the
 * implementing class.
 * <p>
 * There is one engine, {@code org.kosit.validator.impl.ConformanceValidation}: the canonical pipeline along its
 * scenarios — scenario detection and selection, retrieval and preparation of the validation artifacts, schema and
 * Schematron validation, conformance statement and decision recommendation, reported as a CVR. What differs is how it
 * is assembled: over the scenarios of a configuration ({@link ScenarioSet}), or over one scenario built at runtime from
 * a single Schematron ({@code ConformanceValidation.adHoc}) — the ad hoc validation of a rule set against a document is
 * not a second engine but the same pipeline with a scenario that applies unconditionally.
 * </p>
 * <p>
 * How an engine instance is assembled is a construction concern of the implementing class and deliberately not part of
 * this contract (ADR-008).
 * </p>
 *
 * @param <R> the engine-specific result type
 *
 * @author Andreas Schmitz
 */
public interface ValidationEngine<R> {

    /**
     * Validates the given document.
     *
     * @param input the document to validate
     * @return the engine-specific result
     */
    R validate(CTReadResource input);
}
