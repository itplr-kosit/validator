package org.kosit.validator.client;

import java.io.File;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.kosit.validator.api.xvrl.compact.CompactXvrlReportSummary;
import org.kosit.validator.client.api.ValidationApi;
import org.kosit.validator.client.filter.ValidationRequestConfig;
import org.kosit.validator.client.filter.ValidationResponseMetadata;
import org.kosit.xvrl.impl.XvrlConverter;
import org.kosit.xvrl.model.XvrlReports;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
public class ValidationClient {

    private final ValidationApi api;

    private final ValidationResponseMetadata metadata;

    private final ValidationRequestConfig requestConfig;

    public ValidationClient(@RestClient final ValidationApi api, final ValidationResponseMetadata metadata,
            final ValidationRequestConfig requestConfig) {
        this.api = api;
        this.metadata = metadata;
        this.requestConfig = requestConfig;
    }

    public File validateRaw(final File input) {
        return api.validate(input);
    }

    public File validateMinimalRaw(final File input) {
        return api.validateMinimal(input);
    }

    public File validateMinimalRawAsJson(final File input) {
        requestConfig.setAcceptType(MediaType.APPLICATION_JSON_TYPE);
        return api.validateMinimal(input);
    }

    public XvrlReports validate(final File input) {
        return new XvrlConverter().readXml(api.validate(input));
    }

    public CompactXvrlReportSummary validateMinimal(final File input) {
        return new CompactXvrlReportSummary(new XvrlConverter().readXml(api.validateMinimal(input)));
    }

    public ValidationResponse<File> validateRawWithMetadata(final File input) {
        final File result = api.validate(input);
        return toResponse(result);
    }

    public ValidationResponse<File> validateMinimalRawWithMetadata(final File input) {
        final File result = api.validate(input);
        return toResponse(result);
    }

    public ValidationResponse<File> validateMinimalRawAsJsonWithMetadata(final File input) {
        requestConfig.setAcceptType(MediaType.APPLICATION_JSON_TYPE);
        return toResponse(api.validateMinimal(input));
    }

    public ValidationResponse<XvrlReports> validateWithMetadata(final File input) {
        final File result = api.validate(input);
        return toResponse(new XvrlConverter().readXml(result));
    }

    public ValidationResponse<CompactXvrlReportSummary> validateMinimalWithMetadata(final File input) {
        final File result = api.validateMinimal(input);
        return toResponse(new CompactXvrlReportSummary(new XvrlConverter().readXml(result)));
    }

    private <T> ValidationResponse<T> toResponse(final T body) {
        return new ValidationResponse<>(body, metadata.getStatusCode(), metadata.getContentType());
    }
}
