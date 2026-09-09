package org.kosit.validator.client;

import java.io.File;
import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.kosit.validator.client.api.ValidationApi;
import org.kosit.validator.client.model.ValidationRunStatus;
import org.kosit.xvrl.impl.XvrlConverter;
import org.kosit.xvrl.model.XvrlReports;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * The Java client of the validator server, following the resource model of the API: a validation run is created, and
 * its result is fetched by the identifier the server handed out.
 * <p>
 * The two steps are exposed separately, because a caller may want to hold on to the identifier — to fetch the result
 * again, or later, or from somewhere else. {@link #validate(File)} does both in one go for the common case.
 * </p>
 */
@ApplicationScoped
public class ValidationClient {

    private final ValidationApi api;

    public ValidationClient(@RestClient final ValidationApi api) {
        this.api = api;
    }

    /**
     * Creates a validation run for the document — {@code POST /api/validation}.
     *
     * @param input the document to validate
     * @return the run as the server describes it: identifier, state and where the result lives
     */
    public ValidationRunStatus createRun(final File input) {
        return this.api.createValidation(input);
    }

    /**
     * Fetches the result of a run — {@code GET /api/validation/result/{id}} — as the file the server sent.
     *
     * @param id the identifier of the run
     * @return the CVR, serialized
     */
    public File fetchResultRaw(final UUID id) {
        return this.api.getValidationResult(id);
    }

    /**
     * Fetches the result of a run as the XVRL object model.
     *
     * @param id the identifier of the run
     * @return the CVR
     */
    public XvrlReports fetchResult(final UUID id) {
        return new XvrlConverter().readXml(fetchResultRaw(id));
    }

    /**
     * Creates a run and fetches its result — the one-call form for a caller that does not need the identifier.
     *
     * @param input the document to validate
     * @return the CVR
     */
    public XvrlReports validate(final File input) {
        return fetchResult(createRun(input).getId());
    }

    /**
     * Creates a run and fetches its result as the file the server sent.
     *
     * @param input the document to validate
     * @return the CVR, serialized
     */
    public File validateRaw(final File input) {
        return fetchResultRaw(createRun(input).getId());
    }
}
