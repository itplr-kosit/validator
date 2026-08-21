package org.kosit.validator.server.impl;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML_TYPE;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.kosit.validator.api.VInput;
import org.kosit.validator.api.VInputFactory;
import org.kosit.validator.api.Result;
import org.kosit.validator.api.ValidationResource;
import org.kosit.validator.api.compact.CompactXVRLReportSummary;
import org.kosit.validator.server.api.CompactValidationResultsDto;
import org.kosit.xvrl.impl.XvrlConversionService;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ValidationController implements ValidationResource {

    private final ValidationService service;

    @Context
    HttpHeaders headers;

    public ValidationController(ValidationService service) {
        this.service = service;
    }

    public Response validate(File xmlFile) {
        final Result result = service.validate(VInputFactory.read(xmlFile));
        final XvrlConversionService conversionService = new XvrlConversionService();
        final byte[] resultBytes = conversionService.writeXml(result.getReportSummary()).getBytes();
        return addHeaders(result, Response.ok(resultBytes).type(MediaType.APPLICATION_XML).header("Content-Disposition",
                "attachment; filename=validation-result.xml")).build();
    }

    @Override
    public Response validateMinimal(File xmlFile) {
        final VInput VInput = VInputFactory.read(xmlFile);
        final Result result = service.validate(VInput);

        final CompactXVRLReportSummary compactReport = service.convertMinimalXvrl(VInput, result);

        MediaType best = headers.getAcceptableMediaTypes().stream()
                .filter(mt -> mt.isCompatible(APPLICATION_JSON_TYPE) || mt.isCompatible(APPLICATION_XML_TYPE)).findFirst()
                .orElse(APPLICATION_XML_TYPE); // Default: XML

        if (best.isCompatible(APPLICATION_JSON_TYPE)) {
            final CompactValidationResultsDto compactJson = CompactXVRLReportSummaryMapper.toDto(compactReport);
            return addHeaders(result, Response.ok(compactJson).type(MediaType.APPLICATION_JSON).header("Content-Disposition",
                    "attachment; filename=compact-validation-result.json")).build();
        }

        final XvrlConversionService conversionService = new XvrlConversionService();
        final byte[] resultBytes = conversionService.writeXml(compactReport.getOriginal()).getBytes(StandardCharsets.UTF_8);
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
