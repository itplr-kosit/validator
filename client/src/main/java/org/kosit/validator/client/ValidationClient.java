package org.kosit.validator.client;

import java.io.File;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.kosit.validator.api.xvrl.compact.CompactXVRLReportSummary;
import org.kosit.validator.client.api.ValidationApi;
import org.kosit.validator.client.filter.ValidationRequestConfig;
import org.kosit.validator.client.filter.ValidationResponseMetadata;
import org.kosit.xvrl.model.XVRLReportsType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
public class ValidationClient {

    private final ValidationApi api;

    private final ValidationResponseMetadata metadata;

    private final ValidationRequestConfig requestConfig;

    private final XmlConversionService xmlConversionService;

    public ValidationClient(@RestClient final ValidationApi api, final ValidationResponseMetadata metadata,
            final ValidationRequestConfig requestConfig) {
        this.api = api;
        this.metadata = metadata;
        this.requestConfig = requestConfig;
        this.xmlConversionService = new XmlConversionService();
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

    public XVRLReportsType validate(final File input) {
        return unmarshal(api.validate(input), XVRLReportsType.class);
    }

    public CompactXVRLReportSummary validateMinimal(final File input) {
        return new CompactXVRLReportSummary(unmarshal(api.validateMinimal(input), XVRLReportsType.class));
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

    public ValidationResponse<XVRLReportsType> validateWithMetadata(final File input) {
        final File result = api.validate(input);
        return toResponse(unmarshal(result, XVRLReportsType.class));
    }

    public ValidationResponse<CompactXVRLReportSummary> validateMinimalWithMetadata(final File input) {
        final File result = api.validateMinimal(input);
        return toResponse(new CompactXVRLReportSummary(unmarshal(api.validateMinimal(input), XVRLReportsType.class)));
    }

    private <T> ValidationResponse<T> toResponse(final T body) {
        return new ValidationResponse<>(body, metadata.getStatusCode(), metadata.getContentType());
    }

    private <T> T unmarshal(final File file, final Class<T> type) {
        return xmlConversionService.readXml(file, type);
    }

}
