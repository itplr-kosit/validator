package org.kosit.validator.impl.conformatron.engine;

import java.net.URI;
import java.util.List;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.conformatron.api.model.source.CTReadResource;
import org.kosit.base.uri.UriHelper;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.PrepareRulesAction;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.kosit.validator.impl.conformatron.model.ValidationArtifactReference;
import org.kosit.validator.impl.conformatron.util.ArtifactResolver;
import org.kosit.validator.xml.resolve.StrictRelativeResolvingStrategy;

import net.sf.saxon.s9api.Processor;

/**
 * <b>Prototype</b>: validates a document directly against a single Schematron — no scenario configuration, no
 * repository setup, no report transformation required. The Schematron URI is the only configuration.
 * <p>
 * The engine is a pure composition of canonical actions: {@link ParseXmlAction} (step 2),
 * {@link RetrieveArtifactsAction} (step 5, with the Schematron's parent directory as the artifact repository),
 * {@link PrepareRulesAction} (step 6) and {@link ApplyRulesAction} (step 7). Scenario detection/selection (steps 3+4)
 * is deliberately bypassed — the caller fixes the rule set explicitly, which is the ad-hoc equivalent of a
 * user-selected scenario.
 * </p>
 * <p>
 * Result semantics: {@code SUCCESS} means the rules were applied — findings (failed asserts) do <b>not</b> fail the
 * run; {@link AdHocValidationResult#isConformant()} tells whether the document satisfied the rules. Failures of the
 * composed steps surface under their canonical detection codes ({@code artifact-missing}, {@code rule-prepare-error},
 * {@code rule-engine-error}).
 * </p>
 *
 * @author Andreas Schmitz
 */
public class SchematronValidation implements ValidationEngine<SchematronValidation.AdHocValidationResult> {

    private final Processor processor;

    /** The fixed Schematron of this engine instance; {@code null} if only the two-arg {@code validate} is used. */
    private final URI schematron;

    /** Whether the fixed Schematron of this engine instance may live inside an archive. */
    private final boolean resolveInArchive;

    public SchematronValidation(final Processor processor) {
        this(processor, null);
    }

    /**
     * Creates an engine instance fixed to the given Schematron, which may not live inside an archive, see
     * {@link #SchematronValidation(Processor, URI, boolean)}.
     *
     * @param processor the Saxon processor
     * @param schematron URI of the Schematron file this engine validates against
     */
    public SchematronValidation(final Processor processor, final URI schematron) {
        this(processor, schematron, false);
    }

    /**
     * Creates an engine instance fixed to the given Schematron ({@link ValidationEngine} assembly: the rule set is a
     * construction concern).
     *
     * @param processor the Saxon processor
     * @param schematron URI of the Schematron file this engine validates against
     * @param resolveInArchive {@code true} if the Schematron lives inside an archive, see
     *            {@link ArtifactResolver#ArtifactResolver(URI, boolean)}
     */
    public SchematronValidation(final Processor processor, final URI schematron, final boolean resolveInArchive) {
        if (processor == null) {
            throw new IllegalArgumentException("processor may not be null");
        }
        this.processor = processor;
        this.schematron = schematron;
        this.resolveInArchive = resolveInArchive;
    }

    /**
     * Validates the document against the Schematron fixed at construction time ({@link ValidationEngine} contract).
     *
     * @param input the document to validate
     * @return the result including all detections
     */
    @Override
    public AdHocValidationResult validate(final CTReadResource input) {
        if (this.schematron == null) {
            throw new IllegalStateException("No schematron configured for this engine instance");
        }
        return validate(input, this.schematron, this.resolveInArchive);
    }

    /**
     * Result of an ad-hoc schematron validation run.
     *
     * @param status success or failure of the run itself (failure = parse, preparation or processing error)
     * @param parsedSource the parsed document; may be {@code null} if the source could not be read
     * @param detections all findings and errors of the run; never {@code null}
     */
    public record AdHocValidationResult(CTStepResult status, CTParsedValidationSource parsedSource, CTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == CTStepResult.SUCCESS;
        }

        /**
         * @return {@code true} if the run succeeded and the document satisfies the rules (no ERROR or FATAL detections)
         */
        public boolean isConformant() {
            return isSuccess() && !this.detections.containsAtLeastOneError();
        }
    }

    /**
     * Validates the document against the given Schematron, which may not live inside an archive, see
     * {@link #validate(CTReadResource, URI, boolean)}.
     *
     * @param document the document to validate
     * @param schematron URI of the Schematron file ({@code .sch})
     * @return the result including all detections
     */
    public AdHocValidationResult validate(final CTReadResource document, final URI schematron) {
        return validate(document, schematron, false);
    }

    /**
     * Validates the document against the given Schematron.
     *
     * @param document the document to validate
     * @param schematron URI of the Schematron file ({@code .sch}); relative resources of the Schematron are resolved
     *            against its parent directory
     * @param resolveInArchive {@code true} if the Schematron lives inside an archive
     *            ({@code jar:file:/some.jar!/rules/simple.sch}), see
     *            {@link ArtifactResolver#ArtifactResolver(URI, boolean)}
     * @return the result including all detections
     */
    public AdHocValidationResult validate(final CTReadResource document, final URI schematron, final boolean resolveInArchive) {
        if (schematron == null) {
            throw new IllegalArgumentException("schematron may not be null");
        }
        // step 2 (PARSE_DOCUMENT): reference action, retains bytes + hash
        final ParseXmlResult parsed = new ParseXmlAction().execute(document);
        if (parsed.isFailure()) {
            return new AdHocValidationResult(CTStepResult.FAILURE, parsed.getParsedSource(), parsed.getDetectionList());
        }

        final String documentName = parsed.getParsedSource().getSource().getName();
        // the parent directory of the schematron is the artifact repository of this ad-hoc run
        final URI base = UriHelper.resolve(schematron, ".", resolveInArchive);
        if (!base.isAbsolute()) {
            // no parent could be derived: the URI is relative, or it addresses an archive that may not be resolved in
            return new AdHocValidationResult(CTStepResult.FAILURE, parsed.getParsedSource(), new DetectionList(List.of(Detection.of(
                    CTStandardSeverity.ERROR, RetrieveArtifactsAction.CODE_ARTIFACT_ACCESS_DENIED, DetectionLocation.of(documentName),
                    "Can not derive an artifact repository from the Schematron '" + schematron + "'"
                            + (UriHelper.isArchiveUri(schematron) ? ", because resolving inside an archive is not enabled" : "")))));
        }
        final ValidationArtifactReference reference = ValidationArtifactReference.of(UriHelper.relativize(base, schematron).toString());

        // step 5 (RETRIEVE_ARTIFACTS): resolve confined to that directory
        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(base, resolveInArchive)
                .execute(List.of(reference), documentName);
        if (!retrieved.isSuccess()) {
            return new AdHocValidationResult(CTStepResult.FAILURE, parsed.getParsedSource(), retrieved.detections());
        }
        // step 6 (PREPARE_RULES): transpile + compile
        // the strategy of ResolvingMode.STRICT_RELATIVE, but with the archive permission of this run
        final ContentRepository repository = new ContentRepository(this.processor, new StrictRelativeResolvingStrategy(resolveInArchive),
                base);
        final PrepareRulesAction.PrepareRulesResult prepared = new PrepareRulesAction(repository).execute(retrieved.artifacts(),
                documentName);
        if (!prepared.isSuccess()) {
            return new AdHocValidationResult(CTStepResult.FAILURE, parsed.getParsedSource(), prepared.detections());
        }
        // step 7 (APPLY_RULES): findings do not fail the run, only engine errors do
        final ApplyRulesAction.ApplyRulesActionResult applied = new ApplyRulesAction().execute(parsed.getParsedSource(),
                prepared.ruleSets());
        return new AdHocValidationResult(applied.status(), parsed.getParsedSource(), applied.detections());
    }
}
