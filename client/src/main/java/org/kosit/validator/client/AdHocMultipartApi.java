package org.kosit.validator.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.client.api.ClientMultipartForm;
import org.kosit.validator.client.model.ValidationRunStatus;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * The ad hoc operation of the server API - {@code POST /api/validation/adhoc} - over a hand-built multipart form.
 * <p>
 * Hand-written next to the generated {@code AdHocApi} of the same operation: the generated form sends every part under
 * a fixed file name and turns the repeatable {@code resource} part into a text part with the paths of the files, so
 * neither several artifacts nor their real names reach the server. The names matter - they decide how an artifact is
 * treated ({@code .xsd}, {@code .sch}, {@code .xsl}) and name the scenario in the report. Same {@code configKey}, so
 * both talk to the same server.
 * </p>
 */
@Path("/api/validation/adhoc")
@RegisterRestClient(configKey = "validator")
public interface AdHocMultipartApi {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    ValidationRunStatus createAdHocValidation(ClientMultipartForm form);
}
