package org.kosit.validator.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
