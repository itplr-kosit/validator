package org.kosit.validator.server.impl;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML_TYPE;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.conformatron.api.model.source.CTReadResource;
import org.kosit.validator.api.VResult;
import org.kosit.validator.api.ValidationResource;
import org.kosit.validator.api.xvrl.compact.CompactXVRLReportSummary;
import org.kosit.validator.impl.conformatron.source.ReadResource;
import org.kosit.validator.impl.conformatron.source.Resource;
import org.kosit.validator.server.api.CompactValidationResultsDto;
import org.kosit.xvrl.impl.XvrlConversionService;
import org.kosit.xvrl.model.ObjectFactory;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ValidationController implements ValidationResource {

    private final ValidationService service;

    @Context
    HttpHeaders headers;

    public ValidationController(final ValidationService service) {
        this.service = service;
    }

    public Response validate(final File xmlFile) {
        VResult result;
        try {
            result = service.validate(ReadResource.inMemory(Resource.of(xmlFile)));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        final XvrlConversionService conversionService = new XvrlConversionService();
        final byte[] resultBytes = conversionService.writeXml(new ObjectFactory().createReports(result.getReportSummary())).getBytes();
        return addHeaders(result, Response.ok(resultBytes).type(MediaType.APPLICATION_XML).header("Content-Disposition",
                "attachment; filename=validation-result.xml")).build();
    }

    @Override
    public Response validateMinimal(final File xmlFile) {
        CTReadResource input;
        try {
            input = ReadResource.inMemory(Resource.of(xmlFile));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        final VResult result = service.validate(input);

        final CompactXVRLReportSummary compactReport = service.convertMinimalXvrl(input, result);

        final MediaType best = headers.getAcceptableMediaTypes().stream()
                .filter(mt -> mt.isCompatible(APPLICATION_JSON_TYPE) || mt.isCompatible(APPLICATION_XML_TYPE)).findFirst()
                .orElse(APPLICATION_XML_TYPE); // Default: XML

        if (best.isCompatible(APPLICATION_JSON_TYPE)) {
            final CompactValidationResultsDto compactJson = CompactXVRLReportSummaryMapper.toDto(compactReport);
            return addHeaders(result, Response.ok(compactJson).type(MediaType.APPLICATION_JSON).header("Content-Disposition",
                    "attachment; filename=compact-validation-result.json")).build();
        }

        final XvrlConversionService conversionService = new XvrlConversionService();
        final byte[] resultBytes = conversionService.writeXml(new ObjectFactory().createReports(compactReport.getOriginal()))
                .getBytes(StandardCharsets.UTF_8);
        return addHeaders(result, Response.ok(resultBytes).type(MediaType.APPLICATION_XML).header("Content-Disposition",
                "attachment; filename=compact-validation-result.xml")).build();
    }

    private Response.ResponseBuilder addHeaders(final VResult result, final Response.ResponseBuilder responseBuilder) {
        final String headerPrefix = "X-VALIDATOR-";

        responseBuilder.header(headerPrefix + "Schema-Valid", Boolean.valueOf(result.isSchemaValid()))
                .header(headerPrefix + "Schematron-Valid", Boolean.valueOf(result.isSchematronValid()))
                .header(headerPrefix + "Acceptance", result.getAcceptRecommendation());
        return responseBuilder;
    }

}
