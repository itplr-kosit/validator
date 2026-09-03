package org.kosit.validator.impl.conformatron.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

import org.conformatron.api.model.validation.CTValidationArtifactReference;
import org.kosit.base.string.StringHelper;
import org.kosit.base.uri.UriHelper;

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
 * artifacts are shipped as a jar on the class path. That has to be enabled explicitly, see
 * {@link #ArtifactResolver(URI, boolean)}. It is then handled through {@link UriHelper}, because such URIs are opaque,
 * and the confinement compares the URL the archive URI wraps ({@code file:/some.jar!/repository/}), whose path carries
 * the entry path — it therefore covers the archive and the entry path within it in one go.
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

    private final URI repository;

    private final boolean resolveInArchive;

    /** The base of the containment check: for an archive that is the URL of the archive plus the entry path. */
    private final URI containmentBase;

    /**
     * Creates a resolver that does not resolve into an archive repository, see {@link #ArtifactResolver(URI, boolean)}.
     *
     * @param repository base URI of the artifact repository; resolution is confined to this location
     */
    public ArtifactResolver(final URI repository) {
        this(repository, false);
    }

    /**
     * @param repository base URI of the artifact repository; resolution is confined to this location
     * @param resolveInArchive {@code true} to resolve references inside a repository that lives in an archive
     *            ({@code jar:file:/some.jar!/repository/}). Off by default: the artifacts of such a repository are the
     *            content of a single file, which an operator granting access to that file does not necessarily intend
     *            to expose entry by entry. With {@code false} every reference into an archive repository is rejected
     *            with an {@link AccessDeniedException}.
     */
    public ArtifactResolver(final URI repository, final boolean resolveInArchive) {
        Objects.requireNonNull(repository);
        if (!repository.isAbsolute()) {
            throw new IllegalArgumentException("repository must be an absolute URI, but was '" + repository.toASCIIString() + "'");
        }
        this.repository = normalizeBase(repository);
        this.resolveInArchive = resolveInArchive;
        this.containmentBase = resolveInArchive ? UriHelper.getHierarchicalUri(this.repository) : this.repository;
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

        final URI resolved = UriHelper
                .normalize(UriHelper.resolve(this.repository, reference.getValidationArtifactReference(), this.resolveInArchive));
        if (!isInsideRepository(resolved)) {
            throw new AccessDeniedException("Artifact reference '" + reference.getValidationArtifactReference()
                    + "' resolves outside the repository '" + this.repository + "'");
        }
        return resolved;
    }

    private boolean isInsideRepository(final URI resolved) {
        // an archive is only unwrapped when reaching into it is allowed - otherwise an absolute reference in archive
        // form would be a way around that
        final URI candidate = this.resolveInArchive ? UriHelper.getHierarchicalUri(resolved) : resolved;
        // component-based containment check: java.net.URI#resolve drops an *empty* authority (file:///C:/... becomes
        // file:/C:/...), so a plain string prefix comparison rejects valid references on Windows-style file URIs
        return Objects.equals(candidate.getScheme(), this.containmentBase.getScheme())
                && Objects.equals(StringHelper.emptyToNull(candidate.getAuthority()),
                        StringHelper.emptyToNull(this.containmentBase.getAuthority()))
                && this.containmentBase.getPath() != null && candidate.getPath() != null
                && candidate.getPath().startsWith(this.containmentBase.getPath());
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

    private static URI normalizeBase(final URI repository) {
        final URI normalized = UriHelper.normalize(repository);
        return normalized.toString().endsWith("/") ? normalized : URI.create(normalized + "/");
    }
}
