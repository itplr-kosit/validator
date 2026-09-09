package org.kosit.validator.server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.validator.api.model.ApiError;
import org.kosit.validator.api.model.ApiValidationRunStatus;
import org.kosit.validator.server.impl.ValidationService.PostedResource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * {@code POST /api/validation/adhoc}: an ad hoc validation run, the document against a set of posted artifacts instead
 * of the configured scenarios. The contract is the operation {@code createAdHocValidation} of {@code openapi.yml}.
 * <p>
 * Hand-written rather than implementing the generated {@code AdHocResource}: the jaxrs-spec generator turns the
 * repeatable {@code resource} part into a single {@code InputStream} and drops the file names of all parts. Both matter
 * here - several artifacts are the point of the operation, and the file name decides how an artifact is treated
 * ({@code .xsd}, {@code .sch}, {@code .xsl}) and names the scenario in the report. The raw multipart form keeps every
 * part however a client sent it: as a file part with a name, or as a plain part with the bytes as its value.
 * </p>
 */
@Path("/api/validation/adhoc")
public class AdHocValidationController {

    private final ValidationService service;

    @Context
    UriInfo uriInfo;

    public AdHocValidationController(final ValidationService service) {
        this.service = service;
    }

    /**
     * @param form the multipart parts: {@code document} (required), {@code resource} (repeatable), {@code schematron}
     *            (alias of one resource, kept for the first clients), {@code repository} (a ZIP) and {@code artifact}
     *            (repeatable, entry names within the ZIP)
     * @return {@code 201 Created} with the location of the result, or {@code 400} for a request that cannot make a run
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAdHocValidation(final MultipartFormDataInput form) {
        final Map<String, Collection<FormValue>> parts = form.getValues();
        final FormValue document = first(parts, "document");
        if (document == null) {
            return badRequest("An ad hoc validation needs the part 'document'");
        }
        try {
            final List<PostedResource> posted = new ArrayList<>();
            for (final FormValue resource : all(parts, "resource")) {
                posted.add(new PostedResource(resource.getFileName(), bytesOf(resource)));
            }
            for (final FormValue schematron : all(parts, "schematron")) {
                posted.add(new PostedResource(schematron.getFileName(), bytesOf(schematron)));
            }
            final FormValue repository = first(parts, "repository");
            final List<String> artifacts = new ArrayList<>();
            for (final FormValue artifact : all(parts, "artifact")) {
                artifacts.add(new String(bytesOf(artifact), charsetOf(artifact)).trim());
            }
            final UUID id = this.service.createAdHocRun(ReadResource.inMemory(Resource.of(documentName(document), bytesOf(document))),
                    posted, repository == null ? null : bytesOf(repository), artifacts);
            return created(id);
        } catch (final IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Collection<FormValue> all(final Map<String, Collection<FormValue>> parts, final String name) {
        final Collection<FormValue> values = parts.get(name);
        return values == null ? List.of() : values;
    }

    private static FormValue first(final Map<String, Collection<FormValue>> parts, final String name) {
        final Collection<FormValue> values = parts.get(name);
        return values == null || values.isEmpty() ? null : values.iterator().next();
    }

    /** The bytes of a part, whether the client sent it as a file or as a plain value. */
    private static byte[] bytesOf(final FormValue value) throws IOException {
        if (value.isFileItem()) {
            try ( InputStream in = value.getFileItem().getInputStream() ) {
                return in.readAllBytes();
            }
        }
        return value.getValue() == null ? new byte[0] : value.getValue().getBytes(charsetOf(value));
    }

    private static Charset charsetOf(final FormValue value) {
        try {
            return value.getCharset() == null ? StandardCharsets.UTF_8 : Charset.forName(value.getCharset());
        } catch (final IllegalArgumentException e) {
            return StandardCharsets.UTF_8;
        }
    }

    /** The name the report shows for the document: the file name the client sent, without any path, else a default. */
    private static String documentName(final FormValue document) {
        final String name = document.getFileName();
        if (name == null || name.isBlank()) {
            return "document.xml";
        }
        final String normalized = name.replace('\\', '/');
        return normalized.substring(normalized.lastIndexOf('/') + 1);
    }

    private Response created(final UUID id) {
        final URI result = this.uriInfo.getBaseUriBuilder().path("api").path("validation").path("result").path(id.toString()).build();
        final ApiValidationRunStatus status = new ApiValidationRunStatus().id(id).status(ApiValidationRunStatus.StatusEnum.COMPLETED)
                .result(result.toString());
        return Response.created(result).type(MediaType.APPLICATION_JSON).entity(status).build();
    }

    private static Response badRequest(final String message) {
        final ApiError error = new ApiError().code(Response.Status.BAD_REQUEST.getStatusCode()).message(message);
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(error).build();
    }
}
