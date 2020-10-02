/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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

package de.kosit.validationtool.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import javax.xml.transform.stream.StreamSource;

import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.ResolvingConfigurationStrategy;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.DocumentParseAction;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

/**
 * Helferlein für Test-Artefakte
 * 
 * @author Andreas Penski
 */

public class Helper {

    public static class Simple {

        public static final URI ROOT = EXAMPLES_DIR.resolve("simple/");

        public static final URI EXAMPLES = ROOT.resolve("input/");

        public static final URI SIMPLE_VALID = Simple.ROOT.resolve("input/simple.xml");

        public static final URI FOO = Simple.ROOT.resolve("input/foo.xml");

        public static final URI FOO_SCHEMATRON_INVALID = EXAMPLES.resolve("foo-schematron-invalid.xml");

        public static final URI REJECTED = Simple.ROOT.resolve("input/withManualReject.xml");

        public static final URI SCENARIOS = ROOT.resolve("scenarios.xml");

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

        public static final ContentRepository createContentRepository() {
            final ResolvingConfigurationStrategy strategy = ResolvingMode.STRICT_RELATIVE.getStrategy();
            return new ContentRepository(strategy, Simple.REPOSITORY_URI);
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

    public static final URI TEST_ROOT = Paths.get("src/test/resources").toUri();

    public static final URI EXAMPLES_DIR = TEST_ROOT.resolve("examples/");

    public static final URI ASSERTIONS = EXAMPLES_DIR.resolve("assertions/tests-xrechnung.xml");

    public static final URL JAR_REPOSITORY = Helper.class.getClassLoader().getResource("simple/packaged/repository/");

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
        c.initialize(de.kosit.validationtool.model.reportInput.ObjectFactory.class.getPackage(),
                de.kosit.validationtool.cmd.assertions.ObjectFactory.class.getPackage(),
                de.kosit.validationtool.model.scenarios.ObjectFactory.class.getPackage());
        return c.readXml(url.toURI(), type);
    }

    public static String serialize(final XdmNode node) {
        try ( final StringWriter writer = new StringWriter() ) {
            final Processor processor = Helper.getTestProcessor();
            final Serializer serializer = processor.newSerializer(writer);
            serializer.serializeNode(node);
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
        return ResolvingMode.STRICT_RELATIVE.getStrategy().getProcessor();
    }
}
