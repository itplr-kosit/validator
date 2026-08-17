package org.kosit.validator.impl.conformatron;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.conformatron.api.model.source.ICTValidationArtifactReference;

/**
 * Resolves {@link ICTValidationArtifactReference}s against the artifact repository and loads their content
 * (conformatron-api step 5, {@code RETRIEVE_ARTIFACTS}).
 * <p>
 * <b>Security concern</b> (step-05 spec): an artifact reference is untrusted configuration input. Resolution is
 * therefore <b>confined to the repository</b>: the reference is resolved against the repository base URI and the result
 * must stay inside that base. Absolute references pointing elsewhere and relative references escaping the base via
 * {@code ../} are rejected with {@link AccessDeniedException} — they never reach the file system or the network.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class ArtifactResolver {

    /** Thrown when a reference resolves outside the configured repository. */
    public static class AccessDeniedException extends Exception {

        private static final long serialVersionUID = 1L;

        public AccessDeniedException(final String message) {
            super(message);
        }
    }

    private final URI repository;

    /**
     * @param repository base URI of the artifact repository; resolution is confined to this location
     */
    public ArtifactResolver(final URI repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository may not be null");
        }
        if (!repository.isAbsolute()) {
            throw new IllegalArgumentException("repository must be an absolute URI, but was " + repository);
        }
        this.repository = normalizeBase(repository);
    }

    /**
     * Resolves the reference against the repository without loading it.
     *
     * @param reference the artifact reference
     * @return the absolute URI inside the repository
     * @throws AccessDeniedException if the reference resolves outside the repository
     */
    public URI resolve(final ICTValidationArtifactReference reference) throws AccessDeniedException {
        if (reference == null) {
            throw new IllegalArgumentException("reference may not be null");
        }
        final URI resolved = this.repository.resolve(reference.getValidationArtifactReference()).normalize();
        if (!resolved.toString().startsWith(this.repository.toString())) {
            throw new AccessDeniedException("Artifact reference '" + reference.getValidationArtifactReference()
                    + "' resolves outside the repository '" + this.repository + "'");
        }
        return resolved;
    }

    /**
     * Resolves the reference and loads the artifact content.
     *
     * @param reference the artifact reference
     * @return the content of the artifact
     * @throws AccessDeniedException if the reference resolves outside the repository
     * @throws IOException if the artifact can not be read (missing or unreadable)
     */
    public byte[] load(final ICTValidationArtifactReference reference) throws AccessDeniedException, IOException {
        return read(resolve(reference));
    }

    /**
     * Reads an already-resolved artifact location. Callers must pass a URI obtained from
     * {@link #resolve(ICTValidationArtifactReference)} — this method performs no confinement check of its own.
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
        final URI normalized = repository.normalize();
        return normalized.toString().endsWith("/") ? normalized : URI.create(normalized + "/");
    }
}
