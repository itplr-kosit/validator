package org.kosit.validator.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.xvrl.compact.AcceptRecommendation;
import org.kosit.validator.api.xvrl.compact.CompactXvrlReportSummary;
import org.kosit.validator.server.api.CompactValidationResultsDto;
import org.kosit.validator.testdata.TestData;
import org.kosit.xvrl.model.XvrlReports;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
class ValidationClientIT {

    @Inject
    ValidationClient validationClient;

    @Test
    void shouldValidateXmlRaw() throws IOException {
        final File input = new File(TestData.file("examples/simple/input/simple.xml"));

        final File result = validationClient.validateRaw(input);

        assertThat(result).isNotNull();
        assertThat(result.length() > 0).isTrue();
        final String content = Files.readString(result.toPath());
        assertThat(content.isBlank()).isFalse();
    }

    @Test
    void shouldValidateMinimalXmlRaw() {
        final File input = new File(TestData.file("examples/simple/input/simple.xml"));

        final File result = validationClient.validateMinimalRaw(input);

        assertThat(result).isNotNull();
        assertThat(result.length() > 0).isTrue();
    }

    @Test
    void shouldValidateXml() {
        final File input = new File(TestData.file("examples/simple/input/simple.xml"));

        final XvrlReports result = validationClient.validate(input);

        assertThat(result).isNotNull();
        assertThat(result.getReports().isEmpty()).isFalse();
    }

    @Test
    void shouldValidateMinimalXml() {
        final File input = new File(TestData.file("examples/simple/input/simple.xml"));

        final CompactXvrlReportSummary result = validationClient.validateMinimal(input);

        assertThat(result.getReports()).isNotNull();
        assertThat(result.getReports().isEmpty()).isFalse();
        assertThat(result.getReports().get(0).getAcceptance()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
        assertThat(result.getAcceptable() > 0).isTrue();
    }

    @Test
    void shouldValidateMinimalXmlRawWithMetadata() {
        final File input = new File(TestData.file("examples/simple/input/simple.xml"));

        final ValidationResponse<File> result = validationClient.validateMinimalRawWithMetadata(input);
        final File report = result.getBody();

        assertThat(report).isNotNull();
        assertThat(report.length() > 0).isTrue();
    }

    @Test
    void shouldValidateMinimalRawAsJsonWithMetadata() throws IOException {
        final File input = new File(TestData.file("examples/simple/input/simple.xml"));

        final ValidationResponse<File> result = validationClient.validateMinimalRawAsJsonWithMetadata(input);
        final File report = result.getBody();

        assertThat(report).isNotNull();
        assertThat(report.length() > 0).isTrue();

        final CompactValidationResultsDto dto = new ObjectMapper().readValue(report, CompactValidationResultsDto.class);

        assertThat(dto.results()).isNotEmpty();
        assertThat(dto.results().get(0).acceptance()).isEqualTo(AcceptRecommendation.ACCEPTABLE.getID());
        assertThat(dto.acceptable()).isEqualTo(1);
    }

    @Test
    void shouldValidateXmlWithMetadata() {
        final File input = new File(TestData.file("examples/simple/input/simple.xml"));

        final ValidationResponse<XvrlReports> result = validationClient.validateWithMetadata(input);
        final XvrlReports report = result.getBody();

        assertThat(report).isNotNull();
        assertThat(report.getReports().isEmpty()).isFalse();
        assertThat(result.getStatusCode()).isEqualTo(RestResponse.StatusCode.OK);
        assertThat(result.getContentType()).isEqualTo(MediaType.APPLICATION_XML_TYPE);
    }

    @Test
    void shouldValidateRawWithMetadata() {
        final File input = new File(TestData.file("examples/simple/input/simple.xml"));

        final ValidationResponse<File> result = validationClient.validateRawWithMetadata(input);
        final File report = result.getBody();

        assertThat(report).isNotNull();
        assertThat(report.length() > 0).isTrue();
    }

    @Test
    void shouldValidateMinimalXmlWithMetadata() {
        final File input = new File(TestData.file("examples/simple/input/simple.xml"));

        final ValidationResponse<CompactXvrlReportSummary> result = validationClient.validateMinimalWithMetadata(input);
        final CompactXvrlReportSummary report = result.getBody();

        assertThat(report.getReports()).isNotNull();
        assertThat(report.getReports().isEmpty()).isFalse();
        assertThat(report.getReports().get(0).getAcceptance()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
        assertThat(report.getAcceptable() > 0).isTrue();
        assertThat(result.getStatusCode()).isEqualTo(RestResponse.StatusCode.OK);
        assertThat(result.getContentType()).isEqualTo(MediaType.APPLICATION_XML_TYPE);
    }

    @Test
    void shouldValidateMinimalRawAsJson() throws IOException {
        final File input = new File(TestData.file("examples/simple/input/simple.xml"));

        final File result = validationClient.validateMinimalRawAsJson(input);

        assertThat(result).isNotNull();
        assertThat(result.length() > 0).isTrue();

        final CompactValidationResultsDto dto = new ObjectMapper().readValue(result, CompactValidationResultsDto.class);

        assertThat(dto.results()).isNotEmpty();
        assertThat(dto.results().get(0).acceptance()).isEqualTo(AcceptRecommendation.ACCEPTABLE.getID());
        assertThat(dto.acceptable()).isEqualTo(1);
    }

}