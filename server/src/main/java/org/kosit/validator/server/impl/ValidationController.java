package org.kosit.validator.server.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.validator.api.ValidationResource;
import org.kosit.validator.api.model.ApiValidationRunStatus;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * The REST resource of the validator: a validation is something you create, and its result is something you fetch.
 * <p>
 * {@code POST /api/validation} runs the pipeline and answers {@code 201 Created} with a {@code Location} header that
 * identifies the result and a body that describes the run (RFC 9110, section 9.3.3). {@code GET
 * /api/validation/result/{id}} hands the result out — the CVR, byte for byte as the engine wrote it. A document that
 * fails to parse is not a bad request: it is a run that cancelled, and its partial report is the answer.
 * </p>
 */
public class ValidationController implements ValidationResource {

    private final ValidationService service;

    @Context
    UriInfo uriInfo;

    public ValidationController(final ValidationService service) {
        this.service = service;
    }

    @Override
    public Response createValidation(final File xmlFile) {
        final UUID id;
        try {
            id = this.service.createRun(ReadResource.inMemory(Resource.of(xmlFile)));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        final URI result = resultLocation(id);
        final ApiValidationRunStatus status = new ApiValidationRunStatus().id(id).status(ApiValidationRunStatus.StatusEnum.COMPLETED)
                .result(result.toString());
        return Response.created(result).type(MediaType.APPLICATION_JSON).entity(status).build();
    }

    @Override
    public Response getValidationResult(final UUID id) {
        final Optional<byte[]> cvr = this.service.getResult(id);
        if (cvr.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(cvr.get()).type(MediaType.APPLICATION_XML).build();
    }

    /** The result URI, relative to the request so that it holds behind any proxy or path prefix. */
    private URI resultLocation(final UUID id) {
        return this.uriInfo.getBaseUriBuilder().path("api").path("validation").path("result").path(id.toString()).build();
    }
}
