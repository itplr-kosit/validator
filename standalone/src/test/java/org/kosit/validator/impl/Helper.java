/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kosit.validator.impl;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import org.kosit.validator.api.Input;
import org.kosit.validator.api.ResolvingConfigurationStrategy;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.tasks.BusinessReport;
import org.kosit.validator.impl.tasks.DocumentParseAction;
import org.kosit.validator.impl.xml.ProcessorProvider;
import org.kosit.validator.model.XMLSyntaxError;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

/**
 * Helferlein für Test-Artefakte
 * 
 * @author Andreas Penski
 */

public class Helper {

    public static class Simple {

        public static final URI ROOT = EXAMPLES_DIR.resolve("simple/");

        public static final URI EXAMPLES = ROOT.resolve("input/");

        public static final URI SIMPLE_VALID = ROOT.resolve("input/simple.xml");

        public static final URI FOO = ROOT.resolve("input/foo.xml");

        public static final URI FOO_SCHEMATRON_INVALID = EXAMPLES.resolve("foo-schematron-invalid.xml");

        public static final URI REJECTED = ROOT.resolve("input/withManualReject.xml");

        public static final URI SCENARIOS = ROOT.resolve("scenarios.xml");

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
            return new ContentRepository(Helper.getTestProcessor(), strategy, Simple.REPOSITORY_URI);
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

    public static final URI MODEL_ROOT = Paths.get("src/main/model").toUri();

    public static final URI ASSERTION_SCHEMA = MODEL_ROOT.resolve("xsd/assertions.xsd");

    public static final URI TEST_ROOT = Paths.get("src/test/resources").toAbsolutePath().toUri();

    public static final URI EXAMPLES_DIR = TEST_ROOT.resolve("examples/");

    public static final URI ASSERTIONS = EXAMPLES_DIR.resolve("assertions/tests-xrechnung.xml");

    public static final URL JAR_REPOSITORY = Helper.class.getClassLoader().getResource("simple/packaged/repository/");

    public static final URI LARGE_XML = Paths.get("pom.xml").toUri();

    public static XdmNode load(final URI url) {
        try {
            return load(url.toURL());
        } catch (final MalformedURLException e) {
            throw new IllegalStateException("Fehler beim Laden der XML-Datei", e);
        }
    }

    /**
     * Lädt ein XML-Dokument von der gegebenen URL
     *
     * @param url die url die geladen werden soll
     * @return ein result objekt mit Dokument
     */
    public static XdmNode load(final URL url) {
        try ( final InputStream input = url.openStream() ) {
            return TestObjectFactory.createProcessor().newDocumentBuilder().build(new StreamSource(input));
        } catch (final SaxonApiException | IOException e) {
            throw new IllegalStateException("Fehler beim Laden der XML-Datei", e);

        }

    }

    public static <T> T load(final URL url, final Class<T> type) throws URISyntaxException {
        final ConversionService c = new ConversionService();
        c.initialize(org.kosit.validator.model.ObjectFactory.class.getPackage(),
                org.kosit.validator.cmd.assertions.ObjectFactory.class.getPackage(),
                org.kosit.validator.model.scenarios.ObjectFactory.class.getPackage());
        return c.readXml(url.toURI(), type);
    }

    public static String serialize(final List<BusinessReport> reports) {
        try ( final StringWriter writer = new StringWriter() ) {
            final Processor processor = Helper.getTestProcessor();
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

    public static Result<XdmNode, XMLSyntaxError> parseDocument(final Processor processor, final Input input) {
        return new DocumentParseAction(processor).parseDocument(input);
    }

    public static Result<XdmNode, XMLSyntaxError> parseDocument(final Input input) {
        return new DocumentParseAction(getTestProcessor()).parseDocument(input);
    }

    public static Processor getTestProcessor() {
        // is always the same at the moment
        return createProcessor();
    }

    public static Processor createProcessor() {
        return ProcessorProvider.getProcessor();
    }
}
