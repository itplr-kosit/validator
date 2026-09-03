package org.kosit.validator.impl.conformatron.action;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.validation.Schema;

import org.conformatron.api.model.action.CTAction;
import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.validation.CTResolvedValidationArtifact;
import org.conformatron.api.model.validation.CTStandardValidationType;
import org.conformatron.api.model.validation.CTValidationArtifactReference;
import org.kosit.base.string.StringHelper;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.SchXsltCompiler;
import org.kosit.validator.impl.conformatron.model.CompiledValidationArtifact;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.kosit.validator.impl.conformatron.model.SubjectDetection;
import org.kosit.validator.impl.conformatron.model.PreparedRuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XsltExecutable;

/**
 * Step 6 of the canonical pipeline, {@code PREPARE_RULES} (see
 * {@code conformatron-api/doc/steps/step-06-prepare-rules.md}): turns every artifact retrieved in step 5 into an
 * engine-ready {@link CTPreparedRuleSet} for step 7.
 * <p>
 * Three paths, mirroring the spec: an artifact that already carries a compilation is passed through unchanged
 * ({@code rule-precompiled}); a {@code .sch} is transpiled and compiled ({@code rule-compiled}); a {@code .xsl} was
 * transpiled ahead of time (e.g. by the Maven plugin) and only needs compilation, which the spec also counts as
 * pass-through ({@code rule-precompiled}). XSD artifacts become a JAXP {@code Schema}.
 * </p>
 * <p>
 * Facade strategy: compilation itself is done by the legacy {@link ContentRepository}, which owns the compiler
 * registry, the compile cache and — importantly — the secured resolvers.
 * </p>
 * <p>
 * <b>Known deviation from the spec</b> (feedback for step-06): the step is <i>not yet</i> free of repository access.
 * Schematron includes ({@code sch:include}, {@code xsl:import}) are resolved at compile time, so compilation runs
 * against the repository rather than purely against the bytes retrieved in step 5. Full self-containment would require
 * step 5 to resolve includes as well.
 * </p>
 *
 * @author Andreas Schmitz
 */
public class PrepareRulesAction implements CTAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareRulesAction.class);

    /** Detection code when an artifact was compiled by this step (INFO). */
    public static final String CODE_RULE_COMPILED = "rule-compiled";

    /** Detection code when an artifact was already prepared and is passed through (INFO). */
    public static final String CODE_RULE_PRECOMPILED = "rule-precompiled";

    /** Detection code when an artifact can not be prepared (FATAL, cancels the process). */
    public static final String CODE_RULE_PREPARE_ERROR = "rule-prepare-error";

    /** Detection code when the step is skipped because step 5 produced no artifacts (INFO). */
    public static final String CODE_STEP_SKIPPED = "step-skipped";

    private final ContentRepository repository;

    private final String compilerId;

    /**
     * @param repository the content repository doing the compilation (compiler registry, cache, secured resolvers)
     */
    public PrepareRulesAction(final ContentRepository repository) {
        this(repository, SchXsltCompiler.COMPILER_ID);
    }

    /**
     * @param repository the content repository doing the compilation
     * @param compilerId id of the Schematron compiler to use (e.g. {@code schxslt}, {@code iso-schematron})
     */
    public PrepareRulesAction(final ContentRepository repository, final String compilerId) {
        if (repository == null) {
            throw new IllegalArgumentException("repository may not be null");
        }
        this.repository = repository;
        this.compilerId = StringHelper.blankToDefault(compilerId, SchXsltCompiler.COMPILER_ID);
    }

    /**
     * Result of a single execution of this action.
     *
     * @param status success, failure (cancels the process) or skipped (no artifacts)
     * @param ruleSets the prepared rule sets, one per artifact; empty on failure or skip
     * @param detections this execution's contribution to the report; never {@code null}
     */
    public record PrepareRulesResult(CTStepResult status, List<CTPreparedRuleSet> ruleSets, CTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == CTStepResult.SUCCESS;
        }
    }

    @Override
    public String getName() {
        return CTActionType.PREPARE_RULES.getName();
    }

    @Override
    public CTActionType getType() {
        return CTActionType.PREPARE_RULES;
    }

    /**
     * Prepares all artifacts retrieved by step 5.
     *
     * @param artifacts the resolved artifacts; an empty list skips the step
     * @param resourceId the document name used as detection location
     * @return the result including the prepared rule sets and any detections
     */
    public PrepareRulesResult execute(final List<CTResolvedValidationArtifact> artifacts, final String resourceId) {
        if (artifacts == null) {
            throw new IllegalArgumentException("artifacts may not be null");
        }
        if (artifacts.isEmpty()) {
            final CTDetection skipped = Detection.of(CTStandardSeverity.NONE, CODE_STEP_SKIPPED, DetectionLocation.of(resourceId),
                    "No artifacts retrieved (reason: no-artifacts)");
            return new PrepareRulesResult(CTStepResult.SKIPPED, List.of(), DetectionList.of(skipped));
        }
        final List<CTPreparedRuleSet> ruleSets = new ArrayList<>();
        final List<CTDetection> detections = new ArrayList<>();
        for (final CTResolvedValidationArtifact artifact : artifacts) {
            if (!prepare(artifact, resourceId, ruleSets, detections)) {
                // compile failure cancels the process; the partial CVRL keeps what was prepared so far
                return new PrepareRulesResult(CTStepResult.FAILURE, List.copyOf(ruleSets), new DetectionList(detections));
            }
        }
        return new PrepareRulesResult(CTStepResult.SUCCESS, List.copyOf(ruleSets), new DetectionList(detections));
    }

    private boolean prepare(final CTResolvedValidationArtifact artifact, final String resourceId, final List<CTPreparedRuleSet> ruleSets,
            final List<CTDetection> detections) {
        final CTValidationArtifactReference reference = artifact.getReference();
        final String href = reference.getValidationArtifactReference().toString();
        try {
            if (artifact.isPrecompiled()) {
                // nothing to report: an artifact that arrives prepared needed no preparation here
                ruleSets.add(PreparedRuleSet.schematron(reference, artifact.getCompiledArtifact(), engineVersion()));
                return true;
            }
            final URI uri = reference.getValidationArtifactReference();
            switch (artifact.getValidationType()) {
                case CTStandardValidationType.XSD -> {
                    final Schema schema = this.repository.createSchema(uri);
                    ruleSets.add(PreparedRuleSet.xsd(reference, CompiledValidationArtifact.of(artifact.getValidationType(), schema)));
                    detections.add(compiled(href, resourceId, "XML Schema"));
                }
                case CTStandardValidationType.SCHEMATRON_SCHXSLT2_XSLT3 -> {
                    final XsltExecutable executable = this.repository.loadSchematronXslt(uri, this.compilerId);
                    ruleSets.add(PreparedRuleSet
                            .schematron(reference, CompiledValidationArtifact.of(artifact.getValidationType(), executable), engineVersion())
                            .withTranspilerId(this.compilerId));
                    detections.add(compiled(href, resourceId, "Schematron via " + this.compilerId));
                }
                case CTStandardValidationType.SCHEMATRON_XSLT2 -> {
                    final XsltExecutable executable = this.repository.loadXsltScript(uri);
                    ruleSets.add(PreparedRuleSet.schematron(reference,
                            CompiledValidationArtifact.of(artifact.getValidationType(), executable), engineVersion()));
                    // nothing to report: an artifact that was transpiled ahead of time needed no preparation here
                }
                default -> {
                    detections.add(about(href, Detection.of(CTStandardSeverity.ERROR, CODE_RULE_PREPARE_ERROR,
                            DetectionLocation.of(resourceId), "Unsupported validation type " + artifact.getValidationType().getID())));
                    return false;
                }
            }
            return true;
        } catch (final RuntimeException e) {
            LOGGER.error("Could not prepare artifact {}", href, e);
            detections.add(about(href, new Detection(CTStandardSeverity.ERROR, CODE_RULE_PREPARE_ERROR, DetectionLocation.of(resourceId),
                    "Artifact could not be prepared: " + e.getMessage(), e)));
            return false;
        }
    }

    private String engineVersion() {
        return this.repository.getProcessor().getSaxonProductVersion();
    }

    private static CTDetection compiled(final String href, final String resourceId, final String what) {
        return about(href,
                Detection.of(CTStandardSeverity.NONE, CODE_RULE_COMPILED, DetectionLocation.of(resourceId), "Compiled (" + what + ")"));
    }

    /**
     * Names and locates the artifact a detection is about, the same way step 5 does. On the failure path this is the
     * information that matters most — which of several rule sets did not prepare.
     */
    private static CTDetection about(final String href, final Detection detection) {
        return SubjectDetection.about(detection).identifiedBy(SubjectDetection.ATTR_ARTIFACT_ID, href).locatedAt(href).build();
    }
}
