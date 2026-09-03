package org.kosit.validator.impl.conformatron.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

import org.conformatron.api.model.validation.CTValidationArtifactReference;
import org.kosit.base.string.StringHelper;

/**
 * Resolves {@link CTValidationArtifactReference}s against the artifact repository and loads their content
 * (conformatron-api step 5, {@code RETRIEVE_ARTIFACTS}).
 * <p>
 * <b>Security concern</b> (step-05 spec): an artifact reference is untrusted configuration input. Resolution is
 * therefore <b>confined to the repository</b>: the reference is resolved against the repository base URI and the result
 * must stay inside that base. Absolute references pointing elsewhere and relative references escaping the base via
 * {@code ../} are rejected with {@link AccessDeniedException} — they never reach the file system or the network.
 * </p>
 * <p>
 * The repository may live inside an archive ({@code jar:file:/some.jar!/repository/}), which is the case whenever the
 * artifacts are shipped as a jar on the class path. Such URIs are <b>opaque</b>, so {@link URI#resolve(URI)} hands back
 * the bare reference instead of resolving it. They are therefore resolved through the hierarchical URL of the archive
 * ({@code file:/some.jar!/repository/}), whose path carries the entry path — the confinement then covers the archive
 * and the entry path within it in one go.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class ArtifactResolver {

    /** Thrown when a reference resolves outside the configured repository. */
    public static class AccessDeniedException extends Exception {

        public AccessDeniedException(final String message) {
            super(message);
        }
    }

    /** Separator between the archive and the entry path within it, as used by {@code jar:} URIs. */
    private static final String ARCHIVE_SEPARATOR = "!/";

    private final URI repository;

    /** The repository as a hierarchical URI: for an archive that is the URL of the archive plus the entry path. */
    private final URI hierarchicalRepository;

    /**
     * @param repository base URI of the artifact repository; resolution is confined to this location
     */
    public ArtifactResolver(final URI repository) {
        Objects.requireNonNull(repository);
        if (!repository.isAbsolute()) {
            throw new IllegalArgumentException("repository must be an absolute URI, but was '" + repository.toASCIIString() + "'");
        }
        this.repository = normalizeBase(repository);
        // the raw form is used deliberately: the scheme specific part is percent decoded and would not parse again
        this.hierarchicalRepository = isArchive(this.repository) ? URI.create(this.repository.getRawSchemeSpecificPart()).normalize()
                : this.repository;
    }

    /**
     * Resolves the reference against the repository without loading it.
     *
     * @param reference the artifact reference
     * @return the absolute URI inside the repository
     * @throws AccessDeniedException if the reference resolves outside the repository
     */
    public URI resolve(final CTValidationArtifactReference reference) throws AccessDeniedException {
        Objects.requireNonNull(reference);

        final URI resolved = this.hierarchicalRepository.resolve(reference.getValidationArtifactReference()).normalize();
        // component-based containment check: java.net.URI#resolve drops an *empty* authority (file:///C:/... becomes
        // file:/C:/...), so a plain string prefix comparison rejects valid references on Windows-style file URIs
        if (!isInsideRepository(resolved)) {
            throw new AccessDeniedException("Artifact reference '" + reference.getValidationArtifactReference()
                    + "' resolves outside the repository '" + this.repository + "'");
        }
        return toRepositoryForm(resolved);
    }

    private boolean isInsideRepository(final URI resolved) {
        return Objects.equals(resolved.getScheme(), this.hierarchicalRepository.getScheme())
                && Objects.equals(StringHelper.emptyToNull(resolved.getAuthority()),
                        StringHelper.emptyToNull(this.hierarchicalRepository.getAuthority()))
                && resolved.getPath() != null && resolved.getPath().startsWith(this.hierarchicalRepository.getPath());
    }

    /**
     * Turns a resolved location back into the form the repository was configured in, so that it can be opened again.
     *
     * @param resolved the resolved location, hierarchical
     * @return the location as {@code jar:...} again if the repository lives inside an archive, unchanged otherwise
     */
    private URI toRepositoryForm(final URI resolved) {
        return isArchive(this.repository) ? URI.create(this.repository.getScheme() + ":" + resolved.toASCIIString()) : resolved;
    }

    /**
     * Resolves the reference and loads the artifact content.
     *
     * @param reference the artifact reference
     * @return the content of the artifact
     * @throws AccessDeniedException if the reference resolves outside the repository
     * @throws IOException if the artifact can not be read (missing or unreadable)
     */
    public byte[] load(final CTValidationArtifactReference reference) throws AccessDeniedException, IOException {
        return read(resolve(reference));
    }

    /**
     * Reads an already-resolved artifact location. Callers must pass a URI obtained from
     * {@link #resolve(CTValidationArtifactReference)} — this method performs no confinement check of its own.
     *
     * @param resolved the resolved artifact location
     * @return the content of the artifact
     * @throws IOException if the artifact can not be read (missing or unreadable)
     */
    public byte[] read(final URI resolved) throws IOException {
        try ( InputStream in = resolved.toURL().openStream() ) {
            return in.readAllBytes();
        }
    }

    /**
     * @return the repository base this resolver is confined to (always ending with {@code /})
     */
    public URI getRepository() {
        return this.repository;
    }

    /**
     * @param uri the URI to check
     * @return {@code true} if the URI addresses something inside an archive, i.e. is opaque and wraps the URL of the
     *         archive plus an entry path, as in {@code jar:file:/some.jar!/repository/}
     */
    private static boolean isArchive(final URI uri) {
        return uri.isOpaque() && uri.getRawSchemeSpecificPart().contains(ARCHIVE_SEPARATOR);
    }

    private static URI normalizeBase(final URI repository) {
        final URI normalized = repository.normalize();
        return normalized.toString().endsWith("/") ? normalized : URI.create(normalized + "/");
    }
}
