package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.validation.Schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.Helper.Simple;

import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Tests the repository.
 *
 * @author Andreas Penski
 */
public class ContentRepositoryTest {

    private ContentRepository repository;

    @BeforeEach
    public void setup() {
        this.repository = Simple.createContentRepository();
    }

    @Test
    public void testCreateSchema() throws MalformedURLException {
        final Schema schema = this.repository.createSchema(Helper.ASSERTION_SCHEMA.toURL());
        assertThat(schema).isNotNull();
    }

    @Test
    public void testCreateSchemaNotExisting() throws Exception {
        assertThrows(IllegalStateException.class, () -> this.repository.createSchema(Simple.NOT_EXISTING.toURL()));
    }

    @Test
    public void testLoadXSLT() {
        final XsltExecutable executable = this.repository.loadXsltScript(Simple.REPORT_XSL);
        assertThat(executable).isNotNull();
    }

    @Test
    public void testLoadXSLTNotExisting() {
        assertThrows(IllegalStateException.class, () -> this.repository.loadXsltScript(Simple.NOT_EXISTING));
    }

    @Test
    public void testXpathCreation() {
        XPathExecutable xPath = this.repository.createXPath("//html", null);
        assertThat(xPath).isNotNull();
        xPath = this.repository.createXPath("//html", Collections.emptyMap());
        assertThat(xPath).isNotNull();
        final Map<String, String> namespace = new HashMap<>();
        namespace.put("html", "http://www.w3.org/1999/xhtml");
        xPath = this.repository.createXPath("//html:html", namespace);
        assertThat(xPath).isNotNull();
    }

    @Test
    public void testXpathCreationWithoutNamespace() {
        assertThrows(IllegalStateException.class, () -> this.repository.createXPath("//html:html", null));
    }

    @Test
    public void testIllegalXpath() {
        assertThrows(IllegalStateException.class, () -> this.repository.createXPath("not an xpath expression", null));
    }

    @Test
    public void loadFromJar() {
        assert Helper.JAR_REPOSITORY != null;
        this.repository = new ContentRepository(Helper.getTestProcessor(), ResolvingMode.STRICT_RELATIVE.getStrategy(),
                Helper.JAR_REPOSITORY);
        final XsltExecutable xsltExecutable = this.repository.loadXsltScript(URI.create("report.xsl"));
        assertThat(xsltExecutable).isNotNull();
        final Schema schema = this.repository.createSchema(URI.create("main.xsd"));
        assertThat(schema).isNotNull();
    }

    @Test
    public void loadSchematronXsltSchXslt() {
        assertThat(repository.loadSchematronXslt(URI.create("simple.sch"), SchXsltCompiler.COMPILER_ID)).isNotNull();
    }

    @Test
    public void loadSchematronXsltSchXslt2() {
        assertThat(repository.loadSchematronXslt(URI.create("simple-xslt3.sch"), SchXslt2Compiler.COMPILER_ID)).isNotNull();
    }

    @Test
    public void loadSchematronXsltIsoSch() {
        assertThat(repository.loadSchematronXslt(URI.create("simple.sch"), IsoSchematronCompiler.COMPILER_ID)).isNotNull();
    }
}
