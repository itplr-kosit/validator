package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.junit.jupiter.api.Test;
import org.kosit.validator.xml.resolve.RelativeUriResolver;

/**
 * Tests the URI resolver that should resolve relatively.
 *
 * @author Andreas Penski
 */
public class RelativeUriResolverTest {

    private static final URI BASE;

    static {
        try {
            BASE = RelativeUriResolver.class.getResource("/examples/simple/").toURI();
        } catch (final URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private URIResolver resolver = new RelativeUriResolver(BASE);

    @Test
    public void testSuccess() throws TransformerException {
        final Source resource = this.resolver.resolve("scenarios.xml", BASE.toASCIIString());
        assertThat(resource).isNotNull();
    }

    @Test
    public void testNotExisting() {
        assertThrows(TransformerException.class, () -> this.resolver.resolve("ubl-0001", BASE.toASCIIString()));
    }

    @Test
    public void testOutOfPath() {
        assertThrows(TransformerException.class, () -> this.resolver.resolve("../results/report.xml", BASE.toASCIIString()));
    }

    @Test
    public void testClasspathLocal() throws URISyntaxException, TransformerException {
        this.resolver = new RelativeUriResolver(RelativeUriResolver.class.getClassLoader().getResource("loading").toURI());
        final URL moz = RelativeUriResolverTest.class.getClassLoader().getResource("loading/main.xsd");
        final Source resolved = this.resolver.resolve("./resources/reference.xsd", moz.toURI().toASCIIString());
        assertThat(resolved).isNotNull();
    }

    @Test
    public void testClasspathJAR() throws URISyntaxException, TransformerException {
        this.resolver = new RelativeUriResolver(RelativeUriResolver.class.getClassLoader().getResource("packaged").toURI());
        final URL moz = RelativeUriResolverTest.class.getClassLoader().getResource("packaged/main.xsd");
        final Source resolved = this.resolver.resolve("./resources/reference.xsd", moz.toURI().toASCIIString());
        assertThat(resolved).isNotNull();
    }
}
