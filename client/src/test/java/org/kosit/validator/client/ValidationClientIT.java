package org.kosit.validator.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.AcceptRecommendation;
import org.kosit.validator.api.compact.CompactXVRLReportSummary;
import org.kosit.validator.model.xvrl.XVRLReportSummary;
import org.kosit.validator.server.api.CompactValidationResultsDto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ValidationClientIT {

    @Inject
    ValidationClient validationClient;

    @Test
    void shouldValidateXmlRaw() throws IOException {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        File result = validationClient.validateRaw(input);

        assertThat(result).isNotNull();
        assertThat(result.length() > 0).isTrue();
        String content = Files.readString(result.toPath());
        assertThat(content.isBlank()).isFalse();
    }

    @Test
    void shouldValidateMinimalXmlRaw() {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        File result = validationClient.validateMinimalRaw(input);

        assertThat(result).isNotNull();
        assertThat(result.length() > 0).isTrue();
    }

    @Test
    void shouldValidateXml() {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        XVRLReportSummary result = validationClient.validate(input);

        assertThat(result).isNotNull();
        assertThat(result.getReports().isEmpty()).isFalse();
    }

    @Test
    void shouldValidateMinimalXml() {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        CompactXVRLReportSummary result = validationClient.validateMinimal(input);

<<<<<<< HEAD
        assertThat(result.getReports()).isNotNull();
        assertThat(result.getReports().isEmpty()).isFalse();
        assertThat(result.getReports().get(0).getAcceptance()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
=======
        assertThat(result.getResult()).isNotNull();
        assertThat(result.getResult().isEmpty()).isFalse();
        assertThat(result.getResult().get(0).getAcceptance()).isEqualTo(AcceptanceStatusType.ACCEPTABLE);
>>>>>>> 0c78197 (extend methods)
        assertThat(result.getAcceptable() > 0).isTrue();
    }

    @Test
    void shouldValidateMinimalXmlRawWithMetadata() {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        ValidationResponse<File> result = validationClient.validateMinimalRawWithMetadata(input);
        File report = result.getBody();

        assertThat(report).isNotNull();
        assertThat(report.length() > 0).isTrue();
    }

    @Test
    void shouldValidateMinimalRawAsJsonWithMetadata() throws IOException {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        ValidationResponse<File> result = validationClient.validateMinimalRawAsJsonWithMetadata(input);
        File report = result.getBody();

        assertThat(report).isNotNull();
        assertThat(report.length() > 0).isTrue();

<<<<<<< HEAD
        CompactValidationResultsDto dto = new ObjectMapper().readValue(report, CompactValidationResultsDto.class);

        assertThat(dto.results()).isNotEmpty();
        assertThat(dto.results().get(0).acceptance()).isEqualTo(AcceptRecommendation.ACCEPTABLE.name());
=======
        MVRLCompactReportDto dto = new ObjectMapper().readValue(report, MVRLCompactReportDto.class);

        assertThat(dto.results()).isNotEmpty();
        assertThat(dto.results().get(0).acceptance()).isEqualTo(AcceptanceStatusType.ACCEPTABLE.value());
>>>>>>> 0c78197 (extend methods)
        assertThat(dto.acceptable()).isEqualTo(1);
    }

    @Test
    void shouldValidateXmlWithMetadata() {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        ValidationResponse<XVRLReportSummary> result = validationClient.validateWithMetadata(input);
        XVRLReportSummary report = result.getBody();

        assertThat(report).isNotNull();
        assertThat(report.getReports().isEmpty()).isFalse();
        assertThat(result.getStatusCode()).isEqualTo(RestResponse.StatusCode.OK);
        assertThat(result.getContentType()).isEqualTo(MediaType.APPLICATION_XML_TYPE);
    }

    @Test
    void shouldValidateRawWithMetadata() {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        ValidationResponse<File> result = validationClient.validateRawWithMetadata(input);
        File report = result.getBody();

        assertThat(report).isNotNull();
        assertThat(report.length() > 0).isTrue();
    }

    @Test
    void shouldValidateMinimalXmlWithMetadata() {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        ValidationResponse<CompactXVRLReportSummary> result = validationClient.validateMinimalWithMetadata(input);
        CompactXVRLReportSummary report = result.getBody();

        assertThat(report.getReports()).isNotNull();
        assertThat(report.getReports().isEmpty()).isFalse();
        assertThat(report.getReports().get(0).getAcceptance()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
        assertThat(report.getAcceptable() > 0).isTrue();
        assertThat(result.getStatusCode()).isEqualTo(RestResponse.StatusCode.OK);
        assertThat(result.getContentType()).isEqualTo(MediaType.APPLICATION_XML_TYPE);
    }

    @Test
    void shouldValidateMinimalRawAsJson() throws IOException {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        File result = validationClient.validateMinimalRawAsJson(input);

        assertThat(result).isNotNull();
        assertThat(result.length() > 0).isTrue();

        CompactValidationResultsDto dto = new ObjectMapper().readValue(result, CompactValidationResultsDto.class);

        assertThat(dto.results()).isNotEmpty();
        assertThat(dto.results().get(0).acceptance()).isEqualTo(AcceptRecommendation.ACCEPTABLE.name());
        assertThat(dto.acceptable()).isEqualTo(1);
    }

}