package org.kosit.base.uri;

import java.net.URI;
import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Helper for {@link URI} handling, in particular for URIs that address something inside an archive, as in
 * {@code jar:file:/some.jar!/dir/}.
 * <p>
 * Such URIs are <b>opaque</b>: everything behind the scheme is one scheme specific part and not a hierarchical path. As
 * a consequence {@link URI#resolve(URI)} hands the reference back unchanged instead of resolving it, and
 * {@link URI#normalize()} does nothing at all. The methods of this class unwrap an archive URI to the hierarchical URL
 * it wraps ({@code file:/some.jar!/dir/}) - the path of that URL carries the entry path within the archive - perform
 * the operation there and wrap the result again. For every other URI they behave exactly like their {@link URI}
 * counterparts.
 *
 * @author Philip Helger
 */
public final class UriHelper {

    /** Separator between the archive and the entry path within it, as used by {@code jar:} URIs. */
    public static final String ARCHIVE_SEPARATOR = "!/";

    /**
     * @param uri the URI to check. May be <code>null</code>.
     * @return <code>true</code> if the passed URI addresses something inside an archive, meaning it is opaque and its
     *         scheme specific part is the URL of the archive, followed by {@value #ARCHIVE_SEPARATOR} and the entry
     *         path, as in <code>jar:file:/some.jar!/dir/</code>.
     */
    public static boolean isArchiveUri(final @Nullable URI uri) {
        return uri != null && uri.isOpaque() && uri.getRawSchemeSpecificPart().contains(ARCHIVE_SEPARATOR);
    }

    /**
     * Unwraps an archive URI to the hierarchical URL it wraps, so that <code>jar:file:/some.jar!/dir/</code> becomes
     * <code>file:/some.jar!/dir/</code>. Only the outermost wrapper is removed, so an entry within a nested archive
     * like <code>jar:file:/app.jar!/lib/inner.jar!/dir/</code> stays addressable as a whole.
     *
     * @param uri the URI to unwrap. May not be <code>null</code>.
     * @return the hierarchical form of an archive URI, and the passed URI itself for anything else. Never
     *         <code>null</code>.
     */
    public static @NonNull URI getHierarchicalUri(final @NonNull URI uri) {
        Objects.requireNonNull(uri);

        // the raw form is used deliberately: the decoded scheme specific part of e.g. "jar:file:/a%20b.jar!/dir/"
        // would contain a space and could not be parsed again
        return isArchiveUri(uri) ? URI.create(uri.getRawSchemeSpecificPart()) : uri;
    }

    /**
     * Resolves the passed reference against the passed base URI, like {@link URI#resolve(URI)} does, optionally with
     * support for a base URI that addresses something inside an archive.
     *
     * @param base the base URI to resolve against. May not be <code>null</code>.
     * @param reference the reference to resolve. May not be <code>null</code>. An absolute reference is returned
     *            unchanged, exactly like {@link URI#resolve(URI)} does.
     * @param resolveInArchive <code>true</code> to resolve into an archive base URI. Reaching into an archive is opt
     *            in, because it makes the content of a file addressable that is a single opaque resource to everybody
     *            who only looks at the base URI. When it is <code>false</code>, an archive base behaves as it does in
     *            {@link URI#resolve(URI)}: being opaque it can not be resolved against, so the reference is returned
     *            unchanged.
     * @return the resolved URI, in the same form as the base URI, so that a reference resolved against an archive URI
     *         is an archive URI again. Never <code>null</code>.
     */
    public static @NonNull URI resolve(final @NonNull URI base, final @NonNull URI reference, final boolean resolveInArchive) {
        Objects.requireNonNull(base);
        Objects.requireNonNull(reference);

        if (!resolveInArchive || !isArchiveUri(base) || reference.isAbsolute()) {
            // not allowed to reach into the archive, nothing to unwrap, or a reference that replaces the base anyway
            return base.resolve(reference);
        }
        return wrapAsArchiveUri(base.getScheme(), getHierarchicalUri(base).resolve(reference));
    }

    /**
     * Resolves the passed reference against the passed base URI, like {@link URI#resolve(String)} does, optionally with
     * support for a base URI that addresses something inside an archive.
     *
     * @param base the base URI to resolve against. May not be <code>null</code>.
     * @param reference the reference to resolve. May not be <code>null</code>.
     * @param resolveInArchive <code>true</code> to resolve into an archive base URI, see
     *            {@link #resolve(URI, URI, boolean)}.
     * @return the resolved URI, in the same form as the base URI. Never <code>null</code>.
     * @throws IllegalArgumentException if the reference is not a valid URI.
     */
    public static @NonNull URI resolve(final @NonNull URI base, final @NonNull String reference, final boolean resolveInArchive) {
        Objects.requireNonNull(reference);
        return resolve(base, URI.create(reference), resolveInArchive);
    }

    /**
     * Relativizes the passed URI against the passed base URI, like {@link URI#relativize(URI)} does, but with support
     * for an archive URI on either side.
     *
     * @param base the base URI to relativize against. May not be <code>null</code>.
     * @param uri the URI to relativize. May not be <code>null</code>.
     * @return the relative URI, or the passed URI unchanged if it can not be relativized against the base, exactly like
     *         {@link URI#relativize(URI)} does. Never <code>null</code>.
     */
    public static @NonNull URI relativize(final @NonNull URI base, final @NonNull URI uri) {
        Objects.requireNonNull(base);
        Objects.requireNonNull(uri);

        if (!isArchiveUri(base) && !isArchiveUri(uri)) {
            return base.relativize(uri);
        }

        final URI relativized = getHierarchicalUri(base).relativize(getHierarchicalUri(uri));
        // a URI that can not be relativized is returned unchanged, and then it keeps the form it was passed in
        return relativized.isAbsolute() ? uri : relativized;
    }

    /**
     * Normalizes the passed URI, like {@link URI#normalize()} does, but with support for a URI that addresses something
     * inside an archive, where the {@code .} and {@code ..} segments are part of the entry path.
     *
     * @param uri the URI to normalize. May not be <code>null</code>.
     * @return the normalized URI, in the same form as the passed one. Never <code>null</code>.
     */
    public static @NonNull URI normalize(final @NonNull URI uri) {
        Objects.requireNonNull(uri);

        if (!isArchiveUri(uri)) {
            return uri.normalize();
        }
        return wrapAsArchiveUri(uri.getScheme(), getHierarchicalUri(uri).normalize());
    }

    /**
     * The inverse of {@link #getHierarchicalUri(URI)}.
     *
     * @param scheme the scheme of the archive URI, e.g. <code>jar</code>.
     * @param hierarchicalUri the URL of the archive including the entry path.
     * @return the archive URI.
     */
    private static @NonNull URI wrapAsArchiveUri(final @NonNull String scheme, final @NonNull URI hierarchicalUri) {
        return URI.create(scheme + ":" + hierarchicalUri.toASCIIString());
    }

    private UriHelper() {
    }

}
