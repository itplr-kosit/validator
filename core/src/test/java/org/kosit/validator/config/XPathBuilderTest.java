package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kosit.base.string.StringHelper;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.model.SingleProcessingResult;

import net.sf.saxon.s9api.XPathExecutable;

/**
 * Tests {@link XPathBuilder}.
 * 
 * @author Andreas Penski
 */
public class XPathBuilderTest {

    @Test
    public void testSimpleString() {
        final String name = StringHelper.randomString(5);
        final XPathBuilder b = new XPathBuilder(name);
        b.setXpath("//*");
        final SingleProcessingResult<XPathExecutable, String> result = b.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(b.getNamespaces()).isNotNull();
        assertThat(b.getNamespaces()).isEmpty();
        assertThat(b.getXPath()).isNotEmpty();
        assertThat(b.getName()).isNotEmpty();
    }

    @Test
    public void testStringWithNamespace() {
        final String name = StringHelper.randomString(5);
        final XPathBuilder b = new XPathBuilder(name);
        final Map<String, String> ns = new HashMap<>();
        ns.put("p", "http://somens");
        b.setNamespaces(ns);
        b.setXpath("//p:*");
        final SingleProcessingResult<XPathExecutable, String> result = b.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(b.getNamespaces()).isNotEmpty();
        assertThat(b.getXPath()).isNotEmpty();
    }

    @Test
    public void testStringWithUnknownNamespace() {
        final String name = StringHelper.randomString(5);
        final XPathBuilder b = new XPathBuilder(name);
        final Map<String, String> ns = new HashMap<>();
        ns.put("p", "http://somens");
        b.setNamespaces(ns);
        b.setXpath("//u:*");
        final SingleProcessingResult<XPathExecutable, String> result = b.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void testExecutable() {
        final String name = StringHelper.randomString(5);
        final ContentRepository repository = Simple.createContentRepository();
        final XPathExecutable xpath = repository.createXPath("//*", Collections.emptyMap());
        final XPathBuilder b = new XPathBuilder(name);
        b.setExecutable(xpath);
        final SingleProcessingResult<XPathExecutable, String> result = b.build(repository);
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(b.getNamespaces()).isEmpty();
        assertThat(b.getXPath()).isNotEmpty();
    }

    @Test
    public void testExecutableWithNamespace() {
        final String name = StringHelper.randomString(5);
        final ContentRepository repository = Simple.createContentRepository();
        final Map<String, String> ns = new HashMap<>();
        ns.put("p", "http://somens");
        final XPathExecutable xpath = repository.createXPath("//p:*", ns);
        final XPathBuilder b = new XPathBuilder(name);
        b.setExecutable(xpath);
        final SingleProcessingResult<XPathExecutable, String> result = b.build(repository);
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(b.getNamespaces()).isNotEmpty();
        assertThat(b.getNamespaces()).containsKey("p");
        assertThat(b.getXPath()).isNotEmpty();
    }

    @Test
    public void testNoName() {
        final XPathBuilder b = new XPathBuilder(null);
        b.setXpath("//*");
        final SingleProcessingResult<XPathExecutable, String> result = b.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(b.getName()).isNull();
    }

    @Test
    public void testNoConfig() {
        final String name = StringHelper.randomString(5);
        final XPathBuilder b = new XPathBuilder(name);
        final SingleProcessingResult<XPathExecutable, String> result = b.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
    }
}
