package org.kosit.validator.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.kosit.validator.client.api.ValidationApi;
import org.kosit.validator.client.filter.ValidationRequestConfig;
import org.kosit.validator.client.filter.ValidationResponseMetadata;
import org.kosit.validator.model.mvrl.MVRLCompactReport;
import org.kosit.validator.model.xvrl.XVRLReportSummary;

import java.io.File;

@ApplicationScoped
public class ValidationClient {

    private final ValidationApi api;

    private final ValidationResponseMetadata metadata;

    private final JAXBContext jaxbContext;

    private final ValidationRequestConfig requestConfig;

    public ValidationClient(@RestClient ValidationApi api, ValidationResponseMetadata metadata, ValidationRequestConfig requestConfig)
            throws JAXBException {
        this.api = api;
        this.metadata = metadata;
        this.requestConfig = requestConfig;
        this.jaxbContext = JAXBContext.newInstance(XVRLReportSummary.class, MVRLCompactReport.class);
    }

    public File validateRaw(File input) {
        return api.validate(input);
    }

    public File validateMinimalRaw(File input) {
        return api.validateMinimal(input);
    }

    public File validateMinimalRawAsJson(File input) {
        requestConfig.setAcceptType(MediaType.APPLICATION_JSON_TYPE);
        return api.validateMinimal(input);
    }

    public XVRLReportSummary validate(File input) {
        return unmarshal(api.validate(input), XVRLReportSummary.class);
    }

    public MVRLCompactReport validateMinimal(File input) {
        return unmarshal(api.validateMinimal(input), MVRLCompactReport.class);
    }

    public ValidationResponse<File> validateRawWithMetadata(File input) {
        File result = api.validate(input);
        return toResponse(result);
    }

    public ValidationResponse<File> validateMinimalRawWithMetadata(File input) {
        File result = api.validate(input);
        return toResponse(result);
    }

    public ValidationResponse<File> validateMinimalRawAsJsonWithMetadata(File input) {
        requestConfig.setAcceptType(MediaType.APPLICATION_JSON_TYPE);
        return toResponse(api.validateMinimal(input));
    }

    public ValidationResponse<XVRLReportSummary> validateWithMetadata(File input) {
        File result = api.validate(input);
        return toResponse(unmarshal(result, XVRLReportSummary.class));
    }

    public ValidationResponse<MVRLCompactReport> validateMinimalWithMetadata(File input) {
        File result = api.validateMinimal(input);
        return toResponse(unmarshal(result, MVRLCompactReport.class));
    }

    // --- Intern ---

    private <T> ValidationResponse<T> toResponse(T body) {
        return new ValidationResponse<>(body, metadata.getStatusCode(), metadata.getContentType());
    }

    private <T> T unmarshal(File file, Class<T> type) {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return type.cast(unmarshaller.unmarshal(file));
        } catch (JAXBException e) {
            throw new ValidatorClientException("Failed to unmarshal response", e);
        }
    }

}
