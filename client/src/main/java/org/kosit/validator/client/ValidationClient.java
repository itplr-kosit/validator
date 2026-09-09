package org.kosit.validator.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.client.api.ClientMultipartForm;
import org.kosit.validator.client.api.ValidationApi;
import org.kosit.validator.client.model.ValidationRunStatus;
import org.kosit.xvrl.impl.XvrlConverter;
import org.kosit.xvrl.model.XvrlReports;

import io.vertx.core.buffer.Buffer;
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

    private final AdHocMultipartApi adHoc;

    public ValidationClient(@RestClient final ValidationApi api, @RestClient final AdHocMultipartApi adHoc) {
        this.api = api;
        this.adHoc = adHoc;
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
     * Creates an ad hoc validation run — {@code POST /api/validation/adhoc}: the document is validated against the
     * given Schematron alone, no scenario configuration of the server involved.
     *
     * @param input the document to validate
     * @param schematron the Schematron ({@code .sch}) or precompiled Schematron XSLT ({@code .xsl}); the only artifact
     *            of the run, relative includes are not resolved
     * @return the run as the server describes it: identifier, state and where the result lives
     */
    public ValidationRunStatus createAdHocRun(final File input, final File schematron) {
        return createAdHocRun(input, List.of(schematron));
    }

    /**
     * Creates an ad hoc validation run against a set of artifacts: XML Schemas ({@code .xsd}), Schematrons
     * ({@code .sch}) and precompiled Schematron XSLTs ({@code .xsl}), applied in this order. The server tells them
     * apart by their file names, which name the scenario of the report.
     * <p>
     * The files travel as one repository ZIP with their names as entries - the repository form of the operation -
     * rather than as several {@code resource} parts: the REST client groups parts of the same name into a nested
     * {@code multipart/mixed} body (RFC 1738 encoding), which the server does not unwrap. A welcome side effect:
     * includes and imports between the given files resolve, as they lie next to each other in the repository. For files
     * that include others not in the list, zip them yourself and use {@link #createAdHocRun(File, File, List)}.
     * </p>
     *
     * @param input the document to validate
     * @param resources the artifacts; at least one
     * @return the run as the server describes it
     * @throws IllegalArgumentException without any artifact
     * @throws UncheckedIOException if an artifact cannot be read
     */
    public ValidationRunStatus createAdHocRun(final File input, final List<File> resources) {
        if (resources.isEmpty()) {
            throw new IllegalArgumentException("An ad hoc validation needs at least one artifact");
        }
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final List<String> entries = new ArrayList<>();
        try ( ZipOutputStream zip = new ZipOutputStream(bytes) ) {
            for (final File resource : resources) {
                final String entry = uniqueEntry(entries, resource.getName());
                zip.putNextEntry(new ZipEntry(entry));
                Files.copy(resource.toPath(), zip);
                zip.closeEntry();
                entries.add(entry);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException("Can not read the artifacts of the ad hoc validation", e);
        }
        final ClientMultipartForm form = ClientMultipartForm.create()
                .binaryFileUpload("document", input.getName(), input.getPath(), "application/xml")
                .binaryFileUpload("repository", "resources.zip", Buffer.buffer(bytes.toByteArray()), "application/zip");
        for (final String entry : entries) {
            form.attribute("artifact", entry, null);
        }
        return this.adHoc.createAdHocValidation(form);
    }

    /** The name of the file as ZIP entry; two files of the same name from different directories are told apart. */
    private static String uniqueEntry(final List<String> taken, final String name) {
        String entry = name;
        for (int n = 2; taken.contains(entry); n++) {
            entry = n + "-" + name;
        }
        return entry;
    }

    /**
     * Creates an ad hoc validation run against a repository: a ZIP whose entries keep their paths, so that
     * {@code xs:import}, {@code sch:include} and {@code xsl:import} between them resolve, and the names of the entries
     * that are applied as rule sets.
     *
     * @param input the document to validate
     * @param repositoryZip the ZIP that becomes the artifact repository of the run
     * @param artifacts the entries of the ZIP to apply, in this order ({@code .xsd}, {@code .sch}, {@code .xsl})
     * @return the run as the server describes it
     */
    public ValidationRunStatus createAdHocRun(final File input, final File repositoryZip, final List<String> artifacts) {
        final ClientMultipartForm form = ClientMultipartForm.create()
                .binaryFileUpload("document", input.getName(), input.getPath(), "application/xml")
                .binaryFileUpload("repository", repositoryZip.getName(), repositoryZip.getPath(), "application/zip");
        for (final String artifact : artifacts) {
            form.attribute("artifact", artifact, null);
        }
        return this.adHoc.createAdHocValidation(form);
    }

    /**
     * Creates an ad hoc run and fetches its result — the one-call form for a caller that does not need the identifier.
     *
     * @param input the document to validate
     * @param schematron the Schematron to validate against
     * @return the CVR
     */
    public XvrlReports validateAdHoc(final File input, final File schematron) {
        return fetchResult(createAdHocRun(input, schematron).getId());
    }

    /**
     * Creates an ad hoc run against a set of artifacts and fetches its result.
     *
     * @param input the document to validate
     * @param resources the artifacts; at least one
     * @return the CVR
     */
    public XvrlReports validateAdHoc(final File input, final List<File> resources) {
        return fetchResult(createAdHocRun(input, resources).getId());
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
