package org.kosit.validator.impl.conformatron.action;

import org.kosit.validator.impl.conformatron.model.PreparedRuleSet;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.CompiledValidationArtifact;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.validation.Schema;

import org.apache.commons.lang3.StringUtils;
import org.conformatron.api.model.action.ECTActionType;
import org.conformatron.api.model.action.ECTActionType;
import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.action.ICTAction;
import org.conformatron.api.model.detection.ECTSeverity;
import org.conformatron.api.model.detection.ICTDetection;
import org.conformatron.api.model.detection.ICTDetectionList;
import org.conformatron.api.model.rule.ICTPreparedRuleSet;
import org.conformatron.api.model.source.ICTResolvedValidationArtifact;
import org.conformatron.api.model.source.ICTValidationArtifactReference;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.SchXsltCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XsltExecutable;

/**
 * Step 6 of the canonical pipeline, {@code PREPARE_RULES} (see
 * {@code conformatron-api/doc/steps/step-06-prepare-rules.md}): turns every artifact retrieved in step 5 into an
 * engine-ready {@link ICTPreparedRuleSet} for step 7.
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
public class PrepareRulesAction implements ICTAction {

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
        this.compilerId = StringUtils.defaultIfBlank(compilerId, SchXsltCompiler.COMPILER_ID);
    }

    /**
     * Result of a single execution of this action.
     *
     * @param status success, failure (cancels the process) or skipped (no artifacts)
     * @param ruleSets the prepared rule sets, one per artifact; empty on failure or skip
     * @param detections this execution's contribution to the report; never {@code null}
     */
    public record PrepareRulesResult(ECTStepResult status, List<ICTPreparedRuleSet> ruleSets, ICTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == ECTStepResult.SUCCESS;
        }
    }

    @Override
    public String getName() {
        return ECTActionType.PREPARE_RULES.getName();
    }

    @Override
    public ECTActionType getType() {
        return ECTActionType.PREPARE_RULES;
    }

    /**
     * Prepares all artifacts retrieved by step 5.
     *
     * @param artifacts the resolved artifacts; an empty list skips the step
     * @param resourceId the document name used as detection location
     * @return the result including the prepared rule sets and any detections
     */
    public PrepareRulesResult execute(final List<ICTResolvedValidationArtifact> artifacts, final String resourceId) {
        if (artifacts == null) {
            throw new IllegalArgumentException("artifacts may not be null");
        }
        if (artifacts.isEmpty()) {
            final ICTDetection skipped = Detection.of(ECTSeverity.INFO, CODE_STEP_SKIPPED, DetectionLocation.ofResource(resourceId),
                    "No artifacts retrieved (reason: no-artifacts)");
            return new PrepareRulesResult(ECTStepResult.SKIPPED, List.of(), DetectionList.of(skipped));
        }
        final List<ICTPreparedRuleSet> ruleSets = new ArrayList<>();
        final List<ICTDetection> detections = new ArrayList<>();
        for (final ICTResolvedValidationArtifact artifact : artifacts) {
            if (!prepare(artifact, resourceId, ruleSets, detections)) {
                // compile failure cancels the process; the partial CVRL keeps what was prepared so far
                return new PrepareRulesResult(ECTStepResult.FAILURE, List.copyOf(ruleSets), new DetectionList(detections));
            }
        }
        return new PrepareRulesResult(ECTStepResult.SUCCESS, List.copyOf(ruleSets), new DetectionList(detections));
    }

    private boolean prepare(final ICTResolvedValidationArtifact artifact, final String resourceId, final List<ICTPreparedRuleSet> ruleSets,
            final List<ICTDetection> detections) {
        final ICTValidationArtifactReference reference = artifact.getReference();
        final String href = reference.getValidationArtifactReference().toString();
        try {
            if (artifact.isPrecompiled()) {
                ruleSets.add(PreparedRuleSet.schematron(reference, artifact.getCompiledArtifact(), engineVersion()));
                detections.add(passThrough(href, resourceId, "already prepared"));
                return true;
            }
            final URI uri = reference.getValidationArtifactReference();
            switch (artifact.getValidationType()) {
                case XSD -> {
                    final Schema schema = this.repository.createSchema(uri);
                    ruleSets.add(PreparedRuleSet.xsd(reference, CompiledValidationArtifact.of(artifact.getValidationType(), schema)));
                    detections.add(compiled(href, resourceId, "XML Schema"));
                }
                case SCHEMATRON_SCH -> {
                    final XsltExecutable executable = this.repository.loadSchematronXslt(uri, this.compilerId);
                    ruleSets.add(PreparedRuleSet.schematron(reference,
                            CompiledValidationArtifact.of(artifact.getValidationType(), executable), engineVersion()));
                    detections.add(compiled(href, resourceId, "Schematron via " + this.compilerId));
                }
                case SCHEMATRON_XSLT -> {
                    final XsltExecutable executable = this.repository.loadXsltScript(uri);
                    ruleSets.add(PreparedRuleSet.schematron(reference,
                            CompiledValidationArtifact.of(artifact.getValidationType(), executable), engineVersion()));
                    detections.add(passThrough(href, resourceId, "transpiled ahead of time"));
                }
                default -> {
                    detections.add(Detection.of(ECTSeverity.FATAL_ERROR, CODE_RULE_PREPARE_ERROR, DetectionLocation.ofResource(resourceId),
                            "Artifact '" + href + "' has unsupported validation type " + artifact.getValidationType().getID()));
                    return false;
                }
            }
            return true;
        } catch (final RuntimeException e) {
            LOGGER.error("Could not prepare artifact {}", href, e);
            detections.add(new Detection(ECTSeverity.FATAL_ERROR, CODE_RULE_PREPARE_ERROR, DetectionLocation.ofResource(resourceId),
                    "Artifact '" + href + "' could not be prepared: " + e.getMessage(), e));
            return false;
        }
    }

    private String engineVersion() {
        return this.repository.getProcessor().getSaxonProductVersion();
    }

    private static ICTDetection compiled(final String href, final String resourceId, final String what) {
        return Detection.of(ECTSeverity.INFO, CODE_RULE_COMPILED, DetectionLocation.ofResource(resourceId),
                "Artifact '" + href + "' compiled (" + what + ")");
    }

    private static ICTDetection passThrough(final String href, final String resourceId, final String why) {
        return Detection.of(ECTSeverity.INFO, CODE_RULE_PRECOMPILED, DetectionLocation.ofResource(resourceId),
                "Artifact '" + href + "' passed through (" + why + ")");
    }
}
