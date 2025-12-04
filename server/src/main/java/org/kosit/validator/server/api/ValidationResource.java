package org.kosit.validator.server.api;

import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.kosit.validator.api.Input;
import org.kosit.validator.api.InputFactory;
import org.kosit.validator.api.Result;
import org.kosit.validator.impl.ConversionService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/api/validate")
public class ValidationResource {

    @Inject
    org.kosit.validator.server.impl.ValidationService service;

    /**
     * Erwartet Multipart-Form mit Feldname "file" (Content-Type: application/xml oder text/xml).
     */
    @POST
    @Path("/form")
    @Blocking
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response validateForm(@RestForm("file") InputStream xmlFile) {
        if (xmlFile == null) {
            throw new BadRequestException("XML-Datei im Feld 'file' ist erforderlich.");
        }
        try {
            final String fileName = "file.xml";
            final Result result = service.validate(InputFactory.read(xmlFile, fileName));
            final ConversionService conversionService = new ConversionService();
            final byte[] resultBytes = conversionService.writeXml(result.getReportSummary()).getBytes();
            return addHeaders(result, Response.ok(resultBytes).type(MediaType.APPLICATION_XML).header("Content-Disposition",
                    "attachment; filename=validation-result.xml")).build();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException("Fehler bei der Validierung", e);
        }
    }

    @POST()
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response validate(InputStream xmlFile) {
        final String fileName = "file.xml";
        final Result result = service.validate(InputFactory.read(xmlFile, fileName));
        final ConversionService conversionService = new ConversionService();
        final byte[] resultBytes = conversionService.writeXml(result.getReportSummary()).getBytes();
        return addHeaders(result, Response.ok(resultBytes).type(MediaType.APPLICATION_XML).header("Content-Disposition",
                "attachment; filename=validation-result.xml")).build();
    }

    private Response.ResponseBuilder addHeaders(final Result result, final Response.ResponseBuilder responseBuilder) {
        final String headerPrefix = "X-VALIDATOR-";

        responseBuilder.header(headerPrefix + "Schema", result.isSchemaValid())
                .header(headerPrefix + "Schematron", result.isSchematronValid())
                .header(headerPrefix + "Acceptance", result.getAcceptRecommendation());
        return responseBuilder;
    }

}
