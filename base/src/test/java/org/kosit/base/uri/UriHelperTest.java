package org.kosit.base.uri;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;

import org.junit.jupiter.api.Test;

public class UriHelperTest {

    private static final URI JAR_DIR = URI.create("jar:file:/some.jar!/dir/");

    private static final URI FILE_DIR = URI.create("file:/tmp/dir/");

    @Test
    public void isArchiveUri() {
        assertThat(UriHelper.isArchiveUri(null)).isFalse();
        assertThat(UriHelper.isArchiveUri(JAR_DIR)).isTrue();
        assertThat(UriHelper.isArchiveUri(URI.create("jar:file:/some.jar!/dir/a.xsd"))).isTrue();
        // a nested archive, as used for the libraries of an executable jar
        assertThat(UriHelper.isArchiveUri(URI.create("jar:file:/app.jar!/lib/inner.jar!/dir/"))).isTrue();

        assertThat(UriHelper.isArchiveUri(FILE_DIR)).isFalse();
        assertThat(UriHelper.isArchiveUri(URI.create("https://example.org/dir/"))).isFalse();
        assertThat(UriHelper.isArchiveUri(URI.create("dir/a.xsd"))).isFalse();
        // opaque, but not an archive
        assertThat(UriHelper.isArchiveUri(URI.create("mailto:someone@example.org"))).isFalse();
    }

    @Test
    public void getHierarchicalUri() {
        assertThat(UriHelper.getHierarchicalUri(JAR_DIR)).isEqualTo(URI.create("file:/some.jar!/dir/"));
        // only the outermost wrapper is removed, the inner separators stay part of the path
        assertThat(UriHelper.getHierarchicalUri(URI.create("jar:file:/app.jar!/lib/inner.jar!/dir/")))
                .isEqualTo(URI.create("file:/app.jar!/lib/inner.jar!/dir/"));
        // the percent encoding of the archive location is retained
        assertThat(UriHelper.getHierarchicalUri(URI.create("jar:file:/a%20b.jar!/dir/"))).isEqualTo(URI.create("file:/a%20b.jar!/dir/"));

        assertThat(UriHelper.getHierarchicalUri(FILE_DIR)).isEqualTo(FILE_DIR);
        assertThat(UriHelper.getHierarchicalUri(URI.create("mailto:someone@example.org")))
                .isEqualTo(URI.create("mailto:someone@example.org"));
    }

    @Test
    public void resolveWithHierarchicalBaseBehavesLikeUriResolve() {
        assertThat(UriHelper.resolve(FILE_DIR, URI.create("a.xsd"), false)).isEqualTo(FILE_DIR.resolve("a.xsd"));
        assertThat(UriHelper.resolve(FILE_DIR, URI.create("sub/a.xsd"), false)).isEqualTo(FILE_DIR.resolve("sub/a.xsd"));
        assertThat(UriHelper.resolve(FILE_DIR, URI.create("../a.xsd"), false)).isEqualTo(FILE_DIR.resolve("../a.xsd"));
        assertThat(UriHelper.resolve(FILE_DIR, URI.create("https://example.org/a.xsd"), false))
                .isEqualTo(URI.create("https://example.org/a.xsd"));
    }

    @Test
    public void resolveDoesNotReachIntoAnArchiveByDefault() {
        // reaching into an archive is opt in, so without it an archive base behaves like it does in URI#resolve:
        // being opaque it can not be resolved against, and the reference is handed back unchanged
        assertThat(JAR_DIR.resolve(URI.create("a.xsd"))).isEqualTo(URI.create("a.xsd"));
        assertThat(UriHelper.resolve(JAR_DIR, URI.create("a.xsd"), false)).isEqualTo(URI.create("a.xsd"));
        assertThat(UriHelper.resolve(JAR_DIR, "a.xsd", false)).isEqualTo(URI.create("a.xsd"));

        // a hierarchical base is not affected by the flag at all
        assertThat(UriHelper.resolve(FILE_DIR, URI.create("a.xsd"), false)).isEqualTo(URI.create("file:/tmp/dir/a.xsd"));
        assertThat(UriHelper.resolve(FILE_DIR, URI.create("a.xsd"), true)).isEqualTo(URI.create("file:/tmp/dir/a.xsd"));
    }

    @Test
    public void resolveWithArchiveBase() {
        assertThat(UriHelper.resolve(JAR_DIR, URI.create("a.xsd"), true)).isEqualTo(URI.create("jar:file:/some.jar!/dir/a.xsd"));
        assertThat(UriHelper.resolve(JAR_DIR, URI.create("sub/a.xsd"), true)).isEqualTo(URI.create("jar:file:/some.jar!/dir/sub/a.xsd"));
        assertThat(UriHelper.resolve(JAR_DIR, URI.create("./a.xsd"), true)).isEqualTo(URI.create("jar:file:/some.jar!/dir/a.xsd"));
        assertThat(UriHelper.resolve(URI.create("jar:file:/some.jar!/dir/sub/"), URI.create("../a.xsd"), true))
                .isEqualTo(URI.create("jar:file:/some.jar!/dir/a.xsd"));
        // the entry path is a path, so it can be left again: first up to the root of the archive, then out of it -
        // it is up to the caller to accept that or not
        assertThat(UriHelper.resolve(JAR_DIR, URI.create("../a.xsd"), true)).isEqualTo(URI.create("jar:file:/some.jar!/a.xsd"));
        assertThat(UriHelper.resolve(JAR_DIR, URI.create("../../a.xsd"), true)).isEqualTo(URI.create("jar:file:/a.xsd"));
        // resolving against an entry, not against a directory
        assertThat(UriHelper.resolve(URI.create("jar:file:/some.jar!/dir/main.xsd"), URI.create("a.xsd"), true))
                .isEqualTo(URI.create("jar:file:/some.jar!/dir/a.xsd"));

        // an absolute reference replaces the base, exactly like URI#resolve does, and is not wrapped
        assertThat(UriHelper.resolve(JAR_DIR, URI.create("file:///etc/passwd"), true)).isEqualTo(URI.create("file:///etc/passwd"));
        assertThat(UriHelper.resolve(JAR_DIR, URI.create("jar:file:/other.jar!/a.xsd"), true))
                .isEqualTo(URI.create("jar:file:/other.jar!/a.xsd"));
    }

    @Test
    public void resolveWithArchiveBaseRetainsPercentEncoding() {
        // the decoded scheme specific part would turn "a%20b.jar" into "a b.jar", which is not a valid URI any more
        assertThat(UriHelper.resolve(URI.create("jar:file:/a%20b.jar!/dir/"), URI.create("a.xsd"), true))
                .isEqualTo(URI.create("jar:file:/a%20b.jar!/dir/a.xsd"));
        assertThat(UriHelper.resolve(URI.create("jar:file:/some.jar!/a%20dir/"), URI.create("a%20b.xsd"), true))
                .isEqualTo(URI.create("jar:file:/some.jar!/a%20dir/a%20b.xsd"));
    }

    @Test
    public void resolveWithNestedArchiveBase() {
        assertThat(UriHelper.resolve(URI.create("jar:file:/app.jar!/lib/inner.jar!/dir/"), URI.create("a.xsd"), true))
                .isEqualTo(URI.create("jar:file:/app.jar!/lib/inner.jar!/dir/a.xsd"));
    }

    @Test
    public void resolveWithStringReference() {
        assertThat(UriHelper.resolve(JAR_DIR, "a.xsd", true)).isEqualTo(URI.create("jar:file:/some.jar!/dir/a.xsd"));
        assertThat(UriHelper.resolve(FILE_DIR, "a.xsd", false)).isEqualTo(URI.create("file:/tmp/dir/a.xsd"));
    }

    @Test
    public void relativize() {
        // this is what the class is about: URI#relativize hands the URI back unchanged if either side is opaque
        assertThat(JAR_DIR.relativize(URI.create("jar:file:/some.jar!/dir/a.xsd"))).isEqualTo(URI.create("jar:file:/some.jar!/dir/a.xsd"));

        assertThat(UriHelper.relativize(JAR_DIR, URI.create("jar:file:/some.jar!/dir/a.xsd"))).isEqualTo(URI.create("a.xsd"));
        assertThat(UriHelper.relativize(JAR_DIR, URI.create("jar:file:/some.jar!/dir/sub/a.xsd"))).isEqualTo(URI.create("sub/a.xsd"));
        // not below the base: the URI is returned unchanged, in the form it was passed in
        assertThat(UriHelper.relativize(JAR_DIR, URI.create("jar:file:/some.jar!/other/a.xsd")))
                .isEqualTo(URI.create("jar:file:/some.jar!/other/a.xsd"));
        assertThat(UriHelper.relativize(JAR_DIR, URI.create("jar:file:/other.jar!/dir/a.xsd")))
                .isEqualTo(URI.create("jar:file:/other.jar!/dir/a.xsd"));
        assertThat(UriHelper.relativize(JAR_DIR, URI.create("file:/tmp/dir/a.xsd"))).isEqualTo(URI.create("file:/tmp/dir/a.xsd"));

        assertThat(UriHelper.relativize(FILE_DIR, URI.create("file:/tmp/dir/a.xsd"))).isEqualTo(URI.create("a.xsd"));
        assertThat(UriHelper.relativize(FILE_DIR, URI.create("file:/tmp/other/a.xsd"))).isEqualTo(URI.create("file:/tmp/other/a.xsd"));
    }

    @Test
    public void normalize() {
        // URI#normalize does nothing on an opaque URI
        assertThat(URI.create("jar:file:/some.jar!/dir/sub/../a.xsd").normalize())
                .isEqualTo(URI.create("jar:file:/some.jar!/dir/sub/../a.xsd"));

        assertThat(UriHelper.normalize(URI.create("jar:file:/some.jar!/dir/sub/../a.xsd")))
                .isEqualTo(URI.create("jar:file:/some.jar!/dir/a.xsd"));
        assertThat(UriHelper.normalize(URI.create("jar:file:/some.jar!/dir/./a.xsd")))
                .isEqualTo(URI.create("jar:file:/some.jar!/dir/a.xsd"));
        assertThat(UriHelper.normalize(JAR_DIR)).isEqualTo(JAR_DIR);

        assertThat(UriHelper.normalize(URI.create("file:/tmp/dir/sub/../a.xsd"))).isEqualTo(URI.create("file:/tmp/dir/a.xsd"));
        assertThat(UriHelper.normalize(URI.create("mailto:someone@example.org"))).isEqualTo(URI.create("mailto:someone@example.org"));
    }

    @Test
    public void nullParametersAreRejected() {
        assertThatThrownBy(() -> UriHelper.getHierarchicalUri(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> UriHelper.normalize(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> UriHelper.relativize(JAR_DIR, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> UriHelper.relativize(null, JAR_DIR)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> UriHelper.resolve(null, URI.create("a.xsd"), false)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> UriHelper.resolve(JAR_DIR, (URI) null, false)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> UriHelper.resolve(JAR_DIR, (String) null, false)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> UriHelper.resolve(null, URI.create("a.xsd"), true)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> UriHelper.resolve(JAR_DIR, (URI) null, true)).isInstanceOf(NullPointerException.class);
    }
}
