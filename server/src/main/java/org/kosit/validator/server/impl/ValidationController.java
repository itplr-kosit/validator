package org.kosit.validator.server.impl;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.kosit.validator.api.Input;
import org.kosit.validator.api.InputFactory;
import org.kosit.validator.api.Result;
import org.kosit.validator.api.ValidationResource;
import org.kosit.validator.impl.ConversionService;
import org.kosit.validator.model.mvrl.MVRLCompactReport;
import org.kosit.validator.server.api.MVRLCompactReportDto;

import java.io.File;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML_TYPE;

public class ValidationController implements ValidationResource {

    private final ValidationService service;

    @Context
    HttpHeaders headers;

    public ValidationController(ValidationService service) {
        this.service = service;
    }

    public Response validate(File xmlFile) {
        final Result result = service.validate(InputFactory.read(xmlFile));
        final ConversionService conversionService = new ConversionService();
        final byte[] resultBytes = conversionService.writeXml(result.getReportSummary()).getBytes();
        return addHeaders(result, Response.ok(resultBytes).type(MediaType.APPLICATION_XML).header("Content-Disposition",
                "attachment; filename=validation-result.xml")).build();
    }

    @Override
    public Response validateMinimal(File xmlFile) {
        final Input input = InputFactory.read(xmlFile);
        final Result result = service.validate(input);

        final MVRLCompactReport compactReport = service.convertMinimal(input, result);

        MediaType best = headers.getAcceptableMediaTypes().stream()
                .filter(mt -> mt.isCompatible(APPLICATION_JSON_TYPE) || mt.isCompatible(APPLICATION_XML_TYPE)).findFirst()
                .orElse(APPLICATION_XML_TYPE); // Default: XML

        if (best.isCompatible(APPLICATION_JSON_TYPE)) {
            final MVRLCompactReportDto compactJson = MVRLCompactReportMapper.toDto(compactReport);
            return addHeaders(result, Response.ok(compactJson).type(MediaType.APPLICATION_JSON).header("Content-Disposition",
                    "attachment; filename=compact-validation-result.json")).build();
        }

        final ConversionService conversionService = new ConversionService();
        conversionService.initialize(org.kosit.validator.model.mvrl.ObjectFactory.class.getPackage());
        final byte[] resultBytes = conversionService.writeXml(compactReport).getBytes();
        return addHeaders(result, Response.ok(resultBytes).type(MediaType.APPLICATION_XML).header("Content-Disposition",
                "attachment; filename=compact-validation-result.xml")).build();
    }

    private Response.ResponseBuilder addHeaders(final Result result, final Response.ResponseBuilder responseBuilder) {
        final String headerPrefix = "X-VALIDATOR-";

        responseBuilder.header(headerPrefix + "Schema-Valid", result.isSchemaValid())
                .header(headerPrefix + "Schematron-Valid", result.isSchematronValid())
                .header(headerPrefix + "Acceptance", result.getAcceptRecommendation());
        return responseBuilder;
    }

}
