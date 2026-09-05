package org.kosit.validator.impl.conformatron.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.validation.CTStandardValidationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction.RetrieveArtifactsResult;
import org.kosit.validator.impl.conformatron.model.ValidationArtifactReference;
import org.kosit.validator.impl.conformatron.util.ArtifactResolver;

/**
 * Tests {@link RetrieveArtifactsAction} (step 5) including the repository confinement of {@link ArtifactResolver}.
 */
public class RetrieveArtifactsActionTest {

    private static final String DOCUMENT = "simple.xml";

    private final RetrieveArtifactsAction action = new RetrieveArtifactsAction(Simple.REPOSITORY_URI, true);

    private static List<ValidationArtifactReference> refs(final String... references) {
        return List.of(references).stream().map(ValidationArtifactReference::of).toList();
    }

    @Test
    public void testRetrievesAllArtifacts() {
        final RetrieveArtifactsResult result = this.action.execute(refs("simple.xsd", "simple.sch"), DOCUMENT);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.artifacts()).hasSize(2);
        assertThat(result.artifacts()).extracting("validationType").containsExactly(CTStandardValidationType.XSD,
                CTStandardValidationType.SCHEMATRON_SCHXSLT2_XSLT3);
        assertThat(result.artifacts().get(0).getContent()).isNotEmpty();
        assertThat(result.artifacts().get(0).isPrecompiled()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").containsOnly(RetrieveArtifactsAction.CODE_ARTIFACTS_RETRIEVED);
    }

    @Test
    public void testPrecompiledXsltIsTypedAsSchematronXslt() {
        final RetrieveArtifactsResult result = this.action.execute(refs("simple.xsl"), DOCUMENT);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.artifacts().get(0).getValidationType()).isEqualTo(CTStandardValidationType.SCHEMATRON_XSLT2);
    }

    @Test
    public void testMissingArtifactFailsTheStep() {
        final RetrieveArtifactsResult result = this.action.execute(refs("simple.sch", "does-not-exist.sch"), DOCUMENT);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.status()).isEqualTo(CTStepResult.FAILURE);
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
        assertThrows(IllegalArgumentException.class, () -> new ArtifactResolver(URI.create("relative/path/")));
    }

    @Test
    public void testResolverAcceptsEmptyAuthorityFileUris(@TempDir final Path tempDir) throws IOException {
        // Path.toUri() produces file:///C:/... (empty authority); URI.resolve drops it (file:/C:/...) — the
        // containment check must compare components, not string prefixes (Windows regression from the E2E run).
        // The repository is built here instead of reusing the test data, which is not necessarily an unpacked
        // directory.
        final Path repositoryPath = Files.createDirectory(tempDir.resolve("repository"));
        Files.writeString(repositoryPath.resolve("simple.xsd"), "<xs:schema/>");
        Files.writeString(tempDir.resolve("scenarios.xml"), "<scenarios/>");
        final RetrieveArtifactsAction tripleSlash = new RetrieveArtifactsAction(repositoryPath.toUri());

        final RetrieveArtifactsResult result = tripleSlash.execute(refs("simple.xsd"), DOCUMENT);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.artifacts()).hasSize(1);
        // and the confinement still works with that URI form
        assertThat(tripleSlash.execute(refs("../scenarios.xml"), DOCUMENT).detections().getAll()).extracting("code")
                .containsExactly(RetrieveArtifactsAction.CODE_ARTIFACT_ACCESS_DENIED);
    }

    @Test
    public void testRepositoryInsideAJarIsRejectedByDefault() {
        // reaching into an archive has to be enabled explicitly, so the reference does not resolve at all
        final RetrieveArtifactsAction packaged = new RetrieveArtifactsAction(TestHelper.getJarRepository());

        final RetrieveArtifactsResult result = packaged.execute(refs("simple.xsd"), DOCUMENT);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.artifacts()).isEmpty();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(RetrieveArtifactsAction.CODE_ARTIFACT_ACCESS_DENIED);
        // and an absolute reference in archive form is no way around it either
        assertThat(packaged.execute(refs(TestHelper.getJarRepository() + "simple.xsd"), DOCUMENT).detections().getAll()).extracting("code")
                .containsExactly(RetrieveArtifactsAction.CODE_ARTIFACT_ACCESS_DENIED);
    }

    @Test
    public void testRepositoryInsideAJarIsResolved() {
        // "jar:file:/some.jar!/dir/" is an opaque URI, so the entry path behind the separator has to be resolved
        // separately - a plain URI.resolve() would hand back the bare reference
        final RetrieveArtifactsAction packaged = new RetrieveArtifactsAction(TestHelper.getJarRepository(), true);

        final RetrieveArtifactsResult result = packaged.execute(refs("simple.xsd", "simple.sch"), DOCUMENT);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.artifacts()).hasSize(2);
        assertThat(result.artifacts().get(0).getContent()).isNotEmpty();
        assertThat(result.detections().getAll()).extracting("code").containsOnly(RetrieveArtifactsAction.CODE_ARTIFACTS_RETRIEVED);
    }

    @Test
    public void testReferenceEscapingTheJarRepositoryIsRejected() {
        final RetrieveArtifactsAction packaged = new RetrieveArtifactsAction(TestHelper.getJarRepository(), true);

        // scenarios.xml exists in that jar, but one entry above the repository - and an absolute reference never
        // addresses the archive
        final RetrieveArtifactsResult result = packaged.execute(refs("../scenarios.xml", "file:///etc/passwd"), DOCUMENT);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.artifacts()).isEmpty();
        assertThat(result.detections().getAll()).extracting("code").containsOnly(RetrieveArtifactsAction.CODE_ARTIFACT_ACCESS_DENIED);
    }

    @Test
    public void testMissingArtifactInAJarFailsTheStep() {
        final RetrieveArtifactsAction packaged = new RetrieveArtifactsAction(TestHelper.getJarRepository(), true);

        final RetrieveArtifactsResult result = packaged.execute(refs("does-not-exist.xsd"), DOCUMENT);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.detections().getAll()).extracting("code").containsExactly(RetrieveArtifactsAction.CODE_ARTIFACT_MISSING);
    }
}
