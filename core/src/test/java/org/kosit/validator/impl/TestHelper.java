package org.kosit.validator.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.conformatron.api.model.source.CTReadResource;
import org.jspecify.annotations.NonNull;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.api.ResolvingConfigurationStrategy;
import org.kosit.validator.impl.conformatron.source.ReadResource;
import org.kosit.validator.impl.conformatron.source.Resource;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.saxon.ProcessorProvider;
import org.kosit.validator.impl.tasks.BusinessReport;
import org.kosit.validator.impl.tasks.DocumentParseTask;
import org.kosit.validator.testdata.TestData;
import org.kosit.validator.xml.resolve.StrictRelativeResolvingStrategy;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

/**
 * Helper for test artifacts.
 *
 * @author Andreas Penski
 */

public class TestHelper {

    public static class Simple {

        public static final URI ROOT = TestData.dir("examples/simple/");

        public static final URI EXAMPLES = TestData.dir("examples/simple/input/");

        public static final URI SIMPLE_VALID = TestData.file("examples/simple/input/simple.xml");

        public static final URI SIMPLE_ISO_VALID = TestData.file("examples/simple/input/simple-iso.xml");

        /** Valid instance that is actually encoded in ISO-8859-1 (not UTF-8), for the base64 embedding path. */
        public static final URI SIMPLE_LATIN1 = TestData.file("examples/simple/input/simple-latin1.xml");

        public static final URI FOO = TestData.file("examples/simple/input/foo.xml");

        public static final URI FOO_SCHEMATRON_INVALID = TestData.file("examples/simple/input/foo-schematron-invalid.xml");

        public static final URI REJECTED = TestData.file("examples/simple/input/withManualReject.xml");

        public static final URI SCENARIOS = TestData.file("examples/simple/scenarios.xml");

        public static final URI SCENARIOS_WITH_SCH = TestData.file("examples/simple/scenarios-with-sch.xml");

        /** Configuration with two scenarios matching the same document, for the ambiguity path. */
        public static final URI SCENARIOS_AMBIGUOUS = TestData.file("examples/simple/scenarios-ambiguous.xml");

        /** Configuration whose scenario references a rule set that does not resolve (step 5 failure). */
        public static final URI SCENARIOS_ARTIFACT_MISSING = TestData.file("examples/simple/scenarios-artifact-missing.xml");

        /** Configuration whose scenario references a rule set that resolves but does not compile (step 6 failure). */
        public static final URI SCENARIOS_RULES_BROKEN = TestData.file("examples/simple/scenarios-rules-broken.xml");

        /** Configuration whose scenario references a rule set that fails while running (step 7 failure). */
        public static final URI SCENARIOS_ENGINE_ERROR = TestData.file("examples/simple/scenarios-engine-error.xml");

        public static final URI SCENARIOS_WITH_RELATIVE_PATHS = TestData.file("examples/simple/scenarios-with-relative-paths.xml");

        public static final URI OTHER_SCENARIOS = TestData.file("examples/simple/otherScenarios.xml");

        public static final URI SCENARIOS_WITH_MANY_CONFIGS = TestData.file("examples/simple/scenarios-with-many-configs.xml");

        public static final URI ERROR_SCENARIOS = TestData.file("examples/simple/scenarios-with-errors.xml");

        public static final URI REPOSITORY_URI = TestData.dir("examples/simple/repository/");

        public static final URI SCHEMA_INVALID = TestData.file("examples/simple/input/simple-schema-invalid.xml");

        public static final URI SCHEMATRON_INVALID = TestData.file("examples/simple/input/simple-schematron-invalid.xml");

        public static final URI NOT_WELLFORMED = TestData.file("examples/simple/input/simple-not-wellformed.xml");

        public static final URI UNKNOWN = TestData.file("examples/simple/input/unknown.xml");

        public static final URI GARBAGE = TestData.file("examples/simple/input/no-xml.file");

        public static final URI NOT_EXISTING = TestData.missing("examples/", "doesnotexist");

        public static final URI REPORT_XSL = TestData.file("examples/simple/repository/report.xsl");

        public static final URI SCHEMA = TestData.file("examples/simple/repository/simple.xsd");

        public static final URI SCHEMATRON = TestData.file("examples/simple/repository/simple-schematron-error.xsl");

        public static final ContentRepository createContentRepository() {
            return new ContentRepository(TestHelper.getTestProcessor(), getTestResolvingStrategy(), Simple.REPOSITORY_URI);
        }

        public static URI getSchemaLocation() {
            return SCHEMA;
        }
    }

    public static class Invalid {

        public static final URI ROOT = TestData.dir("examples/invalid/");

        public static final URI SCENARIOS = TestData.file("examples/invalid/scenarios.xml");

        public static final URI SCENARIOS_ILLFORMED = TestData.file("examples/invalid/scenarios-illformed.xml");

    }

    public static class Resolving {

        public static final URI ROOT = TestData.dir("examples/resolving/");

        public static final URI SCHEMA_WITH_REMOTE_REFERENCE = TestData.file("examples/resolving/withRemote.xsd");

        public static final URI SCHEMA_WITH_REFERENCE = TestData.file("examples/resolving/main.xsd");
    }

    public static final URI EXAMPLES_DIR = TestData.dir("examples/");

    /**
     * Repository that lives inside an archive instead of an unpacked directory, for the tests covering that code path.
     *
     * @return the URI of the packaged repository, never {@code null}
     */
    public static URI getJarRepository() {
        return TestData.inArchive("simple/packaged/repository/");
    }

    public static XdmNode load(final URI url) {
        try {
            return load(url.toURL());
        } catch (final MalformedURLException e) {
            throw new IllegalStateException("Error loading the XML file", e);
        }
    }

    /**
     * Loads an XML document from the given URL.
     *
     * @param url the url to load
     * @return a result object containing the document
     */
    public static XdmNode load(final URL url) {
        try ( final InputStream input = url.openStream() ) {
            return TestObjectFactory.getProcessor().newDocumentBuilder().build(new StreamSource(input));
        } catch (final SaxonApiException | IOException e) {
            throw new IllegalStateException("Error loading the XML file", e);
        }
    }

    public static String serialize(final List<BusinessReport> reports) {
        try ( final StringWriter writer = new StringWriter() ) {
            final Processor processor = TestHelper.getTestProcessor();
            final Serializer serializer = processor.newSerializer(writer);
            for (final BusinessReport report : reports) {
                final XdmNode node = report.getContent();
                serializer.serializeNode(node);
            }
            return writer.toString();
        } catch (final SaxonApiException | IOException e) {
            throw new IllegalStateException("Can not serialize document", e);
        }
    }

    public static SingleProcessingResult<XdmNode, SimpleError> parseDocument(final Processor processor, final CTReadResource input) {
        return new DocumentParseTask(processor).parseDocument(input);
    }

    public static SingleProcessingResult<XdmNode, SimpleError> parseDocument(final CTReadResource input) {
        return new DocumentParseTask(getTestProcessor()).parseDocument(input);
    }

    /**
     * Part of the shared test data lives inside an archive, either because the build packaged this module or because
     * {@link TestData#inArchive(String)} did. Resolving into an archive is off by default, and the tests are the ones
     * that explicitly allow it.
     *
     * @return the resolving strategy of the tests, never {@code null}
     */
    public static ResolvingConfigurationStrategy getTestResolvingStrategy() {
        return new StrictRelativeResolvingStrategy(true);
    }

    public static Processor getTestProcessor() {
        // is always the same at the moment
        return createProcessor();
    }

    public static Processor createProcessor() {
        return ProcessorProvider.getProcessor();
    }

    public static @NonNull CTReadResource read(final @NonNull URI u) {
        try {
            return ReadResource.inMemory(Resource.of(u));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static @NonNull CTReadResource read(final @NonNull File f) {
        try {
            return ReadResource.inMemory(Resource.of(f));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
