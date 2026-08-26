package org.kosit.validator.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.conformatron.api.model.source.CTReadResource;
import org.jspecify.annotations.NonNull;
import org.kosit.jaxb.JaxbConversionService;
import org.kosit.validator.api.ResolvingConfigurationStrategy;
import org.kosit.validator.impl.conformatron.source.ReadResource;
import org.kosit.validator.impl.conformatron.source.Resource;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.tasks.BusinessReport;
import org.kosit.validator.impl.tasks.DocumentParseTask;
import org.kosit.validator.impl.xml.ProcessorProvider;
import org.kosit.validator.model.XMLSyntaxError;

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

        public static final URI ROOT = EXAMPLES_DIR.resolve("simple/");

        public static final URI EXAMPLES = ROOT.resolve("input/");

        public static final URI SIMPLE_VALID = ROOT.resolve("input/simple.xml");

        public static final URI SIMPLE_ISO_VALID = ROOT.resolve("input/simple-iso.xml");

        public static final URI FOO = ROOT.resolve("input/foo.xml");

        public static final URI FOO_SCHEMATRON_INVALID = EXAMPLES.resolve("foo-schematron-invalid.xml");

        public static final URI REJECTED = ROOT.resolve("input/withManualReject.xml");

        public static final URI SCENARIOS = ROOT.resolve("scenarios.xml");

        public static final URI SCENARIOS_WITH_SCH = ROOT.resolve("scenarios-with-sch.xml");

        public static final URI SCENARIOS_WITH_RELATIVE_PATHS = ROOT.resolve("scenarios-with-relative-paths.xml");

        public static final URI OTHER_SCENARIOS = ROOT.resolve("otherScenarios.xml");

        public static final URI SCENARIOS_WITH_MANY_CONFIGS = ROOT.resolve("scenarios-with-many-configs.xml");

        public static final URI ERROR_SCENARIOS = ROOT.resolve("scenarios-with-errors.xml");

        public static final URI REPOSITORY_URI = ROOT.resolve("repository/");

        public static final URI SCHEMA_INVALID = ROOT.resolve("input/simple-schema-invalid.xml");

        public static final URI SCHEMATRON_INVALID = ROOT.resolve("input/simple-schematron-invalid.xml");

        public static final URI NOT_WELLFORMED = ROOT.resolve("input/simple-not-wellformed.xml");

        public static final URI UNKNOWN = ROOT.resolve("input/unknown.xml");

        public static final URI GARBAGE = ROOT.resolve("input/no-xml.file");

        public static final URI NOT_EXISTING = EXAMPLES_DIR.resolve("doesnotexist");

        public static final URI REPORT_XSL = REPOSITORY_URI.resolve("report.xsl");

        public static final URI SCHEMA = REPOSITORY_URI.resolve("simple.xsd");

        public static final URI SCHEMATRON = REPOSITORY_URI.resolve("simple-schematron-error.xsl");

        public static final ContentRepository createContentRepository() {
            final ResolvingConfigurationStrategy strategy = ResolvingMode.STRICT_RELATIVE.getStrategy();
            return new ContentRepository(TestHelper.getTestProcessor(), strategy, Simple.REPOSITORY_URI);
        }

        public static URI getSchemaLocation() {
            return SCHEMA;
        }
    }

    public static class Invalid {

        public static final URI ROOT = EXAMPLES_DIR.resolve("invaid/");

        public static final URI SCENARIOS = ROOT.resolve("scenarios.xml");

        public static final URI SCENARIOS_ILLFORMED = ROOT.resolve("scenarios-illformed.xml");

    }

    public static class Resolving {

        public static final URI ROOT = EXAMPLES_DIR.resolve("resolving/");

        public static final URI SCHEMA_WITH_REMOTE_REFERENCE = ROOT.resolve("withRemote.xsd");

        public static final URI SCHEMA_WITH_REFERENCE = ROOT.resolve("main.xsd");
    }

    public static final URI TEST_ROOT = Paths.get("src/test/resources").toAbsolutePath().toUri();

    public static final URI EXAMPLES_DIR = TEST_ROOT.resolve("examples/");

    public static final URI ASSERTIONS = EXAMPLES_DIR.resolve("assertions/tests-xrechnung.xml");

    public static final URI JAR_REPOSITORY = URI
            .create(TestHelper.class.getClassLoader().getResource("simple/packaged/repository/").toExternalForm());

    public static final URI LARGE_XML = Paths.get("pom.xml").toUri();

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

    public static <T> T load(final URL url, final Class<T> type) throws URISyntaxException {
        final JaxbConversionService c = JaxbConversionService.forPackages(org.kosit.validator.model.ObjectFactory.class.getPackage(),
                org.kosit.validator.scenario.v1.ObjectFactory.class.getPackage());
        return c.readXml(url.toURI(), type);
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

    public static SingleProcessingResult<XdmNode, XMLSyntaxError> parseDocument(final Processor processor, final CTReadResource input) {
        return new DocumentParseTask(processor).parseDocument(input);
    }

    public static SingleProcessingResult<XdmNode, XMLSyntaxError> parseDocument(final CTReadResource input) {
        return new DocumentParseTask(getTestProcessor()).parseDocument(input);
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
