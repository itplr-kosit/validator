package org.kosit.validator.server.impl;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.kosit.validator.api.InputFactory;
import org.kosit.validator.api.Result;
import org.kosit.validator.api.ValidationResource;
import org.kosit.validator.impl.ConversionService;

import java.io.File;

@Path("/api/validate")
public class ValidationController implements ValidationResource {

    private final ValidationService service;

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

    private Response.ResponseBuilder addHeaders(final Result result, final Response.ResponseBuilder responseBuilder) {
        final String headerPrefix = "X-VALIDATOR-";

        responseBuilder.header(headerPrefix + "Schema-Valid", result.isSchemaValid())
                .header(headerPrefix + "Schematron-Valid", result.isSchematronValid())
                .header(headerPrefix + "Acceptance", result.getAcceptRecommendation());
        return responseBuilder;
    }

}
