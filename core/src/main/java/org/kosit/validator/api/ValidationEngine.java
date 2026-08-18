package org.kosit.validator.api;

/**
 * The validation engine contract of the validator (successor of the legacy {@link Check} interface, see ADR-008): an
 * engine validates a document and returns its engine-specific result. This interface is a <b>pure contract</b> —
 * behavior lives in the individual implementing classes (validator design philosophy):
 *
 * <ol>
 * <li>{@code org.kosit.validator.impl.ConformanceValidation} — <b>full conformance validation</b>: the complete
 * pipeline (all steps) along the configured scenarios — scenario detection/selection, schema and schematron validation,
 * report generation and acceptance recommendation.</li>
 * <li>{@code org.kosit.validator.impl.conformatron.SchematronValidation} — <b>ad-hoc validation</b>: the pure technical
 * validation engine — the document is validated directly against a single Schematron, without scenario configuration,
 * repository setup or report transformation. No conformance statement is derived; the result answers only whether the
 * document satisfies the given rules.</li>
 * </ol>
 *
 * How an engine instance is assembled (scenarios and pipeline steps, or the fixed Schematron) is a construction concern
 * of the implementing class and deliberately not part of this contract (ADR-008). The result types converge on the CVRL
 * model once it exists (ADR-004).
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
    R validate(Input input);
}
