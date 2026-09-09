package org.kosit.validator.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.kosit.validator.client.model.ValidationRunStatus;
import org.kosit.validator.testdata.TestData;
import org.kosit.xvrl.model.XvrlReports;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

/**
 * The client against the server it is built for, in process. Named a test rather than an integration test on purpose:
 * it boots the server itself and needs nothing external, so it belongs into the build — a contract nobody runs is not a
 * contract.
 */
@QuarkusTest
class ValidationClientTest {

    @Inject
    ValidationClient client;

    private static File document(final String name) {
        return new File(TestData.file("examples/simple/input/" + name));
    }

    @Test
    void testCreatingARunHandsOutAnIdentifierAndTheResultLocation() {
        final ValidationRunStatus run = this.client.createRun(document("simple.xml"));

        assertThat(run.getId()).isNotNull();
        assertThat(run.getStatus()).isEqualTo(ValidationRunStatus.StatusEnum.COMPLETED);
        assertThat(run.getResult()).endsWith("/api/validation/result/" + run.getId());
    }

    @Test
    void testAnAdHocRunValidatesAgainstThePostedSchematron() throws IOException {
        final File schematron = new File(TestData.file("examples/simple/repository/simple.sch"));

        final ValidationRunStatus run = this.client.createAdHocRun(document("simple.xml"), schematron);
        assertThat(run.getStatus()).isEqualTo(ValidationRunStatus.StatusEnum.COMPLETED);
        final String content = Files.readString(this.client.fetchResultRaw(run.getId()).toPath());
        // the client sends the real file names, so the scenario is named after the rule set
        assertThat(content).contains("cvr:decision=\"ACCEPT\"").contains("scenario-id=\"simple.sch\"");

        // the one-call form, and a document the rule set rejects
        final XvrlReports rejected = this.client.validateAdHoc(document("simple-schematron-invalid.xml"), schematron);
        assertThat(rejected.getReports()).isNotEmpty();
        assertThat(Files.readString(this.client.validateRaw(document("simple.xml")).toPath())).contains("cvr:decision=\"ACCEPT\"");
    }

    @Test
    void testAnAdHocRunAgainstASetOfArtifacts() throws IOException {
        final File schema = new File(TestData.file("examples/simple/repository/simple.xsd"));
        final File schematron = new File(TestData.file("examples/simple/repository/simple.sch"));

        // schema and rules together: the schema rejects what the rules alone would accept
        final ValidationRunStatus run = this.client.createAdHocRun(document("simple-schema-invalid.xml"), List.of(schema, schematron));
        final String content = Files.readString(this.client.fetchResultRaw(run.getId()).toPath());
        assertThat(content).contains("scenario-id=\"simple.xsd, simple.sch\"").contains("cvr:decision=\"REJECT\"")
                .contains("code=\"schema-violation\"");
    }

    @Test
    void testAnAdHocRunAgainstARepositoryZip() throws IOException {
        // a modular Schematron: the only rule lives in an included file, which the ZIP carries next to it
        final java.nio.file.Path rules = java.nio.file.Paths.get("..", "e2e", "adhoc", "rules");
        final File zip = File.createTempFile("adhoc-rules", ".zip");
        zip.deleteOnExit();
        try ( java.util.zip.ZipOutputStream out = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zip)) ) {
            for (final String entry : List.of("with-include.sch", "abstracts.sch")) {
                out.putNextEntry(new java.util.zip.ZipEntry(entry));
                out.write(Files.readAllBytes(rules.resolve(entry)));
                out.closeEntry();
            }
        }

        final ValidationRunStatus run = this.client.createAdHocRun(document("foo.xml"), zip, List.of("with-include.sch"));
        final String content = Files.readString(this.client.fetchResultRaw(run.getId()).toPath());
        // the included rule fired: the root of foo.xml is not 'simple'
        assertThat(content).contains("scenario-id=\"with-include.sch\"").contains("cvr:decision=\"REJECT\"").contains("inc-1");
    }

    @Test
    void testTheResultOfARunIsACvr() throws IOException {
        final ValidationRunStatus run = this.client.createRun(document("simple.xml"));

        final File raw = this.client.fetchResultRaw(run.getId());
        final String content = Files.readString(raw.toPath());
        assertThat(content).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .contains("xmlns=\"http://www.xproc.org/ns/xvrl\"").contains("xmlns:cvr=\"urn:conformatron:cvr:draft\"")
                .contains("cvr:decision=\"ACCEPT\"");

        final XvrlReports model = this.client.fetchResult(run.getId());
        assertThat(model.getReports()).isNotEmpty();
        // the last report is the decision of step 9
        assertThat(model.getReports().get(model.getReports().size() - 1).getMetadata().getCreators().get(0).getName())
                .isEqualTo("decision-recommendation");
    }

    @Test
    void testValidateDoesBothStepsInOneCall() {
        final XvrlReports result = this.client.validate(document("simple.xml"));

        assertThat(result.getReports()).isNotEmpty();
    }

    @Test
    void testADocumentThatDoesNotParseIsARunNotAnError() {
        // 1.6 rejected such a request; 2.0 answers with the partial report of the cancelled pipeline
        final XvrlReports result = this.client.validate(document("no-xml.file"));

        assertThat(result.getOtherAttribute(new javax.xml.namespace.QName("urn:conformatron:cvr:draft", "status"))).isEqualTo("CANCELLED");
    }

    @Test
    void testAnUnknownRunIsNotFound() {
        assertThatThrownBy(() -> this.client.fetchResultRaw(UUID.randomUUID())).isInstanceOf(WebApplicationException.class)
                .satisfies(e -> assertThat(((WebApplicationException) e).getResponse().getStatus()).isEqualTo(404));
    }
}
