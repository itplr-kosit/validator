package org.kosit.schematron;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.validation.Schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.schematron.compiler.IsoSchematronCompiler;
import org.kosit.schematron.compiler.SchXslt2Compiler;
import org.kosit.schematron.compiler.SchXsltCompiler;
import org.kosit.validator.testdata.TestResources;

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
        this.repository = TestHelper.createContentRepository();
    }

    @Test
    public void aSchemaIsCompiledOnceAndThenServedFromTheCache() {
        final Schema first = this.repository.createSchema(TestResources.Simple.SCHEMA);

        // a JAXP Schema is immutable and thread-safe, so recompiling it per document is pure cost
        assertThat(this.repository.createSchema(TestResources.Simple.SCHEMA)).isSameAs(first);
        assertThat(this.repository.createSchema(List.of(TestResources.Simple.SCHEMA.toString()))).isSameAs(first);
    }

    @Test
    public void aFailedCompilationIsNotRemembered() {
        assertThrows(IllegalStateException.class, () -> this.repository.createSchema(TestResources.Simple.NOT_EXISTING));

        // the artifact may be repaired between runs, so the failure must not be cached
        assertThrows(IllegalStateException.class, () -> this.repository.createSchema(TestResources.Simple.NOT_EXISTING));
    }

    @Test
    public void schemasCompileFromManyThreadsThroughOneRepository() throws Exception {
        // one repository is shared by every request thread of the server. A SchemaFactory is not thread-safe by its
        // own contract, so it must not be the same instance for two concurrent compilations
        final int threads = 16;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            final CountDownLatch start = new CountDownLatch(1);
            final List<Future<Schema>> compiled = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                compiled.add(pool.submit(() -> {
                    start.await();
                    return this.repository.createSchema(TestResources.Simple.SCHEMA);
                }));
            }
            start.countDown();

            final List<Schema> schemas = new ArrayList<>();
            for (final Future<Schema> future : compiled) {
                schemas.add(future.get(60, TimeUnit.SECONDS));
            }
            assertThat(schemas).hasSize(threads).doesNotContainNull().allSatisfy(schema -> assertThat(schema).isSameAs(schemas.get(0)));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void testLoadXSLT() {
        final XsltExecutable executable = this.repository.loadXsltScript(TestResources.Simple.REPORT_XSL);
        assertThat(executable).isNotNull();
    }

    @Test
    public void testLoadXSLTNotExisting() {
        assertThrows(IllegalStateException.class, () -> this.repository.loadXsltScript(TestResources.Simple.NOT_EXISTING));
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
        this.repository = new ContentRepository(TestHelper.getTestProcessor(), TestHelper.getTestResolvingStrategy(),
                TestResources.getJarRepository());
        final XsltExecutable xsltExecutable = this.repository.loadXsltScript(URI.create("report.xsl"));
        assertThat(xsltExecutable).isNotNull();
        final Schema schema = this.repository.createSchema(URI.create("main.xsd"));
        assertThat(schema).isNotNull();
    }

    @Test
    public void loadSchematronXsltSchXslt() {
        assertThat(repository.loadSchematronXslt(SchXsltCompiler.COMPILER_ID, URI.create("simple.sch"))).isNotNull();
    }

    @Test
    public void loadSchematronXsltSchXslt2() {
        assertThat(repository.loadSchematronXslt(SchXslt2Compiler.COMPILER_ID, URI.create("simple-xslt3.sch"))).isNotNull();
    }

    @Test
    public void loadSchematronXsltIsoSch() {
        assertThat(repository.loadSchematronXslt(IsoSchematronCompiler.COMPILER_ID, URI.create("simple.sch"))).isNotNull();
    }
}
