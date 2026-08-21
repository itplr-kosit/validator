package org.kosit.validator.impl.conformatron.action;

import org.kosit.validator.impl.conformatron.model.ValidationArtifactReference;
import org.kosit.validator.impl.conformatron.util.ArtifactResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.source.CTValidationArtifactReference;
import org.conformatron.api.model.validation.ECTValidationType;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction.RetrieveArtifactsResult;

/**
 * Tests {@link RetrieveArtifactsAction} (step 5) including the repository confinement of {@link ArtifactResolver}.
 */
public class RetrieveArtifactsActionTest {

    private static final String DOCUMENT = "simple.xml";

    private final RetrieveArtifactsAction action = new RetrieveArtifactsAction(Simple.REPOSITORY_URI);

    private static List<CTValidationArtifactReference> refs(final String... references) {
        return List.of(references).stream().map(r -> (CTValidationArtifactReference) ValidationArtifactReference.of(r)).toList();
    }

    @Test
    public void testRetrievesAllArtifacts() {
        final RetrieveArtifactsResult result = this.action.execute(refs("simple.xsd", "simple.sch"), DOCUMENT);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.artifacts()).hasSize(2);
        assertThat(result.artifacts()).extracting("validationType").containsExactly(ECTValidationType.XSD,
                ECTValidationType.SCHEMATRON_SCH);
        assertThat(result.artifacts().get(0).getContent()).isNotEmpty();
        assertThat(result.artifacts().get(0).isPrecompiled()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").containsOnly(RetrieveArtifactsAction.CODE_ARTIFACTS_RETRIEVED);
    }

    @Test
    public void testPrecompiledXsltIsTypedAsSchematronXslt() {
        final RetrieveArtifactsResult result = this.action.execute(refs("simple.xsl"), DOCUMENT);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.artifacts().get(0).getValidationType()).isEqualTo(ECTValidationType.SCHEMATRON_XSLT);
    }

    @Test
    public void testMissingArtifactFailsTheStep() {
        final RetrieveArtifactsResult result = this.action.execute(refs("simple.sch", "does-not-exist.sch"), DOCUMENT);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.status()).isEqualTo(ECTStepResult.FAILURE);
        // partial list: the resolvable artifact is still reported
        assertThat(result.artifacts()).hasSize(1);
        assertThat(result.detections().getAll()).extracting("code").contains(RetrieveArtifactsAction.CODE_ARTIFACT_MISSING);
        assertThat(result.detections().containsAtLeastOneError()).isTrue();
    }

    @Test
    public void testReferenceEscapingTheRepositoryIsRejected() {
        final RetrieveArtifactsResult result = this.action.execute(refs("../scenarios.xml"), DOCUMENT);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.artifacts()).isEmpty();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(RetrieveArtifactsAction.CODE_ARTIFACT_ACCESS_DENIED);
    }

    @Test
    public void testAbsoluteReferenceOutsideRepositoryIsRejected() {
        final RetrieveArtifactsResult result = this.action.execute(refs("file:///etc/passwd"), DOCUMENT);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(RetrieveArtifactsAction.CODE_ARTIFACT_ACCESS_DENIED);
    }

    @Test
    public void testUnknownArtifactTypeIsCorrupt() {
        final RetrieveArtifactsResult result = this.action.execute(refs("some.txt"), DOCUMENT);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(RetrieveArtifactsAction.CODE_ARTIFACT_CORRUPT);
    }

    @Test
    public void testResolverRequiresAbsoluteRepository() {
        assertThrows(IllegalArgumentException.class, () -> new ArtifactResolver(java.net.URI.create("relative/path/")));
    }

    @Test
    public void testResolverAcceptsEmptyAuthorityFileUris() {
        // Paths.toUri() produces file:///C:/... (empty authority); URI.resolve drops it (file:/C:/...) — the
        // containment check must compare components, not string prefixes (Windows regression from the E2E run)
        final java.nio.file.Path repositoryPath = java.nio.file.Paths.get(Simple.REPOSITORY_URI);
        final RetrieveArtifactsAction tripleSlash = new RetrieveArtifactsAction(repositoryPath.toUri());

        final RetrieveArtifactsResult result = tripleSlash.execute(refs("simple.xsd"), DOCUMENT);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.artifacts()).hasSize(1);
        // and the confinement still works with that URI form
        assertThat(tripleSlash.execute(refs("../scenarios.xml"), DOCUMENT).detections().getAll()).extracting("code")
                .containsExactly(RetrieveArtifactsAction.CODE_ARTIFACT_ACCESS_DENIED);
    }
}
