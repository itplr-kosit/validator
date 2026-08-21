package org.kosit.validator.impl.conformatron.model;

import org.conformatron.api.model.rule.ICTPreparedRuleSet;
import org.conformatron.api.model.source.ICTCompiledValidationArtifact;
import org.conformatron.api.model.source.ICTValidationArtifactReference;
import org.conformatron.api.model.validation.ECTValidationType;

/**
 * Validator implementation of {@link ICTPreparedRuleSet} (conformatron-api step 6, {@code PREPARE_RULES}): an
 * engine-ready rule set with the identity needed by steps 7 and 8 and by the report metadata.
 * <p>
 * Field availability per engine (open question of the step spec, answered for this implementation):
 * </p>
 * <ul>
 * <li><b>Schematron</b>: output format {@code SVRL}, phase {@code #ALL}, engine version = the Saxon product version
 * used for compilation. The SVRL output format version stays {@code null} until the SVRL strategy ADR is decided.</li>
 * <li><b>XSD</b>: no reporting format and no phase — both {@code null}; violations are reported by the JAXP validator
 * itself.</li>
 * </ul>
 *
 * @author Andreas Schmitz
 */
public final class PreparedRuleSet implements ICTPreparedRuleSet {

    /** Output format of all Schematron engines used here. */
    public static final String OUTPUT_FORMAT_SVRL = "SVRL";

    /** Default Schematron phase when the scenario does not select one. */
    public static final String PHASE_ALL = "#ALL";

    private final ECTValidationType engineType;

    private final String engineVersion;

    private final String outputFormatName;

    private final String outputFormatVersion;

    private final String phase;

    private final ICTValidationArtifactReference artifactReference;

    private final ICTCompiledValidationArtifact<?> compiledArtifact;

    private PreparedRuleSet(final ECTValidationType engineType, final String engineVersion, final String outputFormatName,
            final String outputFormatVersion, final String phase, final ICTValidationArtifactReference artifactReference,
            final ICTCompiledValidationArtifact<?> compiledArtifact) {
        if (artifactReference == null) {
            throw new IllegalArgumentException("artifactReference may not be null");
        }
        if (compiledArtifact == null) {
            throw new IllegalArgumentException("compiledArtifact may not be null");
        }
        this.engineType = engineType;
        this.engineVersion = engineVersion;
        this.outputFormatName = outputFormatName;
        this.outputFormatVersion = outputFormatVersion;
        this.phase = phase;
        this.artifactReference = artifactReference;
        this.compiledArtifact = compiledArtifact;
    }

    /**
     * Creates a Schematron rule set (SVRL output, phase {@code #ALL}).
     *
     * @param artifactReference the reference the rule set was prepared from
     * @param compiledArtifact the compiled XSLT transformation
     * @param engineVersion the engine version used for compilation, may be {@code null}
     * @return the prepared rule set
     */
    public static PreparedRuleSet schematron(final ICTValidationArtifactReference artifactReference,
            final ICTCompiledValidationArtifact<?> compiledArtifact, final String engineVersion) {
        return new PreparedRuleSet(compiledArtifact.getValidationType(), engineVersion, OUTPUT_FORMAT_SVRL, null, PHASE_ALL,
                artifactReference, compiledArtifact);
    }

    /**
     * Creates an XSD rule set (no reporting format, no phase).
     *
     * @param artifactReference the reference the schema was prepared from
     * @param compiledArtifact the compiled schema
     * @return the prepared rule set
     */
    public static PreparedRuleSet xsd(final ICTValidationArtifactReference artifactReference,
            final ICTCompiledValidationArtifact<?> compiledArtifact) {
        return new PreparedRuleSet(ECTValidationType.XSD, null, null, null, null, artifactReference, compiledArtifact);
    }

    @Override
    public ECTValidationType getEngineType() {
        return this.engineType;
    }

    @Override
    public String getEngineVersion() {
        return this.engineVersion;
    }

    @Override
    public String getOutputFormatName() {
        return this.outputFormatName;
    }

    @Override
    public String getOutputFormatVersion() {
        return this.outputFormatVersion;
    }

    @Override
    public String getPhase() {
        return this.phase;
    }

    @Override
    public ICTValidationArtifactReference getArtifactReference() {
        return this.artifactReference;
    }

    @Override
    public ICTCompiledValidationArtifact<?> getCompiledArtifact() {
        return this.compiledArtifact;
    }
}
