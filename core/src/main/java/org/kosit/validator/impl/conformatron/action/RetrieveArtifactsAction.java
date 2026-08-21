package org.kosit.validator.impl.conformatron.action;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.conformatron.api.model.action.CTAction;
import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.scenario.CTScenarioMatch;
import org.conformatron.api.model.source.CTResolvedValidationArtifact;
import org.conformatron.api.model.source.CTValidationArtifactReference;
import org.conformatron.api.model.validation.CTStandardValidationType;
import org.conformatron.api.model.validation.CTValidationType;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.kosit.validator.impl.conformatron.model.ResolvedValidationArtifact;
import org.kosit.validator.impl.conformatron.util.ArtifactResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Step 5 of the canonical pipeline, {@code RETRIEVE_ARTIFACTS} (see
 * {@code conformatron-api/doc/steps/step-05-retrieve-artifacts.md}): resolves and loads all validation artifacts
 * referenced by the selected scenario, so that steps 6 and 7 run without repository access.
 * <p>
 * Resolution is confined to the repository by {@link ArtifactResolver} (security concern of the step spec).
 * Completeness is enforced: a single unresolvable artifact fails the step — partial resolution would make the later
 * steps fail non-deterministically. All references are attempted first, so the report lists every problem, not just the
 * first.
 * </p>
 * <p>
 * The validation type per artifact is derived from the reference extension ({@code .xsd}, {@code .sch},
 * {@code .xsl}/{@code .xslt}), mirroring the legacy {@code ContentRepository} logic — the reference carries the
 * location only, by design (pure carrier).
 * </p>
 *
 * @author Andreas Schmitz
 */
public class RetrieveArtifactsAction implements CTAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveArtifactsAction.class);

    /** Detection code per successfully retrieved artifact (INFO). */
    public static final String CODE_ARTIFACTS_RETRIEVED = "artifacts-retrieved";

    /** Detection code when a reference does not resolve or can not be read (ERROR, cancels the process). */
    public static final String CODE_ARTIFACT_MISSING = "artifact-missing";

    /** Detection code when an artifact resolves but is unusable (ERROR, cancels the process). */
    public static final String CODE_ARTIFACT_CORRUPT = "artifact-corrupt";

    /**
     * Detection code when a reference resolves outside the repository (ERROR, cancels the process). Extension to the
     * step spec: an escaping reference is a security rejection, not a missing artifact.
     */
    public static final String CODE_ARTIFACT_ACCESS_DENIED = "artifact-access-denied";

    private final ArtifactResolver resolver;

    /**
     * @param repository base URI of the artifact repository; resolution is confined to this location
     */
    public RetrieveArtifactsAction(final URI repository) {
        this(new ArtifactResolver(repository));
    }

    public RetrieveArtifactsAction(final ArtifactResolver resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException("resolver may not be null");
        }
        this.resolver = resolver;
    }

    /**
     * Result of a single execution of this action.
     *
     * @param status success or failure (failure cancels the process)
     * @param artifacts the resolved artifacts; on failure the partial list of those that did resolve
     * @param detections this execution's contribution to the report; never {@code null}
     */
    public record RetrieveArtifactsResult(CTStepResult status, List<CTResolvedValidationArtifact> artifacts, CTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == CTStepResult.SUCCESS;
        }
    }

    @Override
    public String getName() {
        return CTActionType.RETRIEVE_ARTIFACTS.getName();
    }

    @Override
    public CTActionType getType() {
        return CTActionType.RETRIEVE_ARTIFACTS;
    }

    /**
     * Resolves and loads all artifacts declared by the selected scenario.
     *
     * @param selectedScenario the scenario selected in step 4
     * @return the result including the resolved artifacts and any detections
     */
    public RetrieveArtifactsResult execute(final CTScenarioMatch selectedScenario) {
        if (selectedScenario == null) {
            throw new IllegalArgumentException("selectedScenario may not be null");
        }
        return execute(selectedScenario.getArtifactReferences(), selectedScenario.getParsedSource().getSource().getName());
    }

    /**
     * Resolves and loads the given artifact references.
     *
     * @param references the references to resolve; must not be {@code null}
     * @param resourceId the document name used as detection location
     * @return the result including the resolved artifacts and any detections
     */
    public RetrieveArtifactsResult execute(final List<? extends CTValidationArtifactReference> references, final String resourceId) {
        if (references == null) {
            throw new IllegalArgumentException("references may not be null");
        }
        final List<CTResolvedValidationArtifact> artifacts = new ArrayList<>();
        final List<CTDetection> detections = new ArrayList<>();
        for (final CTValidationArtifactReference reference : references) {
            retrieve(reference, resourceId, artifacts, detections);
        }
        // completeness: every declared artifact must have resolved
        final boolean complete = artifacts.size() == references.size();
        return new RetrieveArtifactsResult(complete ? CTStepResult.SUCCESS : CTStepResult.FAILURE, List.copyOf(artifacts),
                new DetectionList(detections));
    }

    private void retrieve(final CTValidationArtifactReference reference, final String resourceId,
            final List<CTResolvedValidationArtifact> artifacts, final List<CTDetection> detections) {
        final String href = reference.getValidationArtifactReference().toString();
        try {
            // security first: a reference escaping the repository is rejected before it is interpreted or read
            final URI resolved = this.resolver.resolve(reference);
            final CTValidationType validationType = determineValidationType(reference);
            final byte[] content = this.resolver.read(resolved);
            if (content.length == 0) {
                detections.add(Detection.of(CTStandardSeverity.ERROR, CODE_ARTIFACT_CORRUPT, DetectionLocation.ofResource(resourceId),
                        "Artifact '" + href + "' is empty"));
                return;
            }
            artifacts.add(ResolvedValidationArtifact.loaded(reference, validationType, content));
            detections.add(Detection.of(CTStandardSeverity.NONE, CODE_ARTIFACTS_RETRIEVED, DetectionLocation.ofResource(resourceId),
                    "Artifact '" + href + "' retrieved as " + validationType.getID()));
        } catch (final ArtifactResolver.AccessDeniedException e) {
            LOGGER.error("Rejected artifact reference {}", href, e);
            detections.add(new Detection(CTStandardSeverity.ERROR, CODE_ARTIFACT_ACCESS_DENIED, DetectionLocation.ofResource(resourceId),
                    e.getMessage(), e));
        } catch (final IOException e) {
            LOGGER.error("Could not read artifact {}", href, e);
            detections.add(new Detection(CTStandardSeverity.ERROR, CODE_ARTIFACT_MISSING, DetectionLocation.ofResource(resourceId),
                    "Artifact '" + href + "' could not be read: " + e.getMessage(), e));
        } catch (final IllegalArgumentException e) {
            detections.add(new Detection(CTStandardSeverity.ERROR, CODE_ARTIFACT_CORRUPT, DetectionLocation.ofResource(resourceId),
                    "Artifact '" + href + "' is not usable: " + e.getMessage(), e));
        }
    }

    /**
     * Derives the validation type from the reference extension, mirroring the legacy {@code ContentRepository}: a
     * {@code .sch} is transpiled by step 6, a {@code .xsl}/{@code .xslt} is an ahead-of-time transpiled Schematron and
     * only needs compilation.
     *
     * @param reference the artifact reference
     * @return the validation type
     */
    public static CTValidationType determineValidationType(final CTValidationArtifactReference reference) {
        final String path = reference.getValidationArtifactReference().toString().toLowerCase(Locale.ROOT);
        if (path.endsWith(".xsd")) {
            return CTStandardValidationType.XSD;
        }
        if (path.endsWith(".sch")) {
            return CTStandardValidationType.SCHEMATRON_SCHXSLT2_XSLT3;
        }
        if (path.endsWith(".xsl") || path.endsWith(".xslt")) {
            return CTStandardValidationType.SCHEMATRON_XSLT2;
        }
        throw new IllegalArgumentException("Can not determine validation type from reference '" + path + "'");
    }
}
