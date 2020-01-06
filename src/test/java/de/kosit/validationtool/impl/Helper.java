/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
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

        public static final URI SCENARIOS = ROOT.resolve("scenarios.xml");

        public static final URI REPOSITORY = ROOT.resolve("repository/");

        public static final URI INVALID = ROOT.resolve("input/simple-invalid.xml");

        public static final URI NOT_WELLFORMED = ROOT.resolve("input/simple-not-wellformed.xml");

        public static final URI UNKNOWN = ROOT.resolve("input/unknown.xml");

        public static final URI GARBAGE = ROOT.resolve("input/no-xml.file");

        public static final URI NOT_EXISTING = EXAMPLES_DIR.resolve("doesnotexist");

        public static final URI REPORT_XSL = REPOSITORY.resolve("report.xsl");

        public static URI getSchemaLocation() {
            return ROOT.resolve("repository/simple.xsd");
        }
    }

    public static class Invalid {

        public static final URI ROOT = EXAMPLES_DIR.resolve("invaid/");

        public static final URI SCENARIOS = ROOT.resolve("scenarios.xml");

        public static final URI SCENARIOS_ILLFORMED = ROOT.resolve("scenarios-illformed.xml");
    }


    public static final URI MODEL_ROOT = Paths.get("src/main/model").toUri();

    public static final URI ASSERTION_SCHEMA = MODEL_ROOT.resolve("xsd/assertions.xsd");


    public static final URI TEST_ROOT = Paths.get("src/test/resources").toUri();

    public static final URI EXAMPLES_DIR = TEST_ROOT.resolve("examples/");

    public static final URI ASSERTIONS = EXAMPLES_DIR.resolve("assertions/tests-xrechnung.xml");



    public static final URL JAR_REPOSITORY = Helper.class.getClassLoader().getResource("xrechnung/repository/");






    /**
     * Lädt ein XML-Dokument von der gegebenen URL
     * 
     * @param url die url die geladen werden soll
     * @return ein result objekt mit Dokument
     */
    public static XdmNode load(final URL url) {
        try ( final InputStream input = url.openStream() ) {
            return ObjectFactory.createProcessor().newDocumentBuilder().build(new StreamSource(input));
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

    /**
     * Lädt das default test repository mit Artefacten für Unit-Tests
     * 
     * @return ein {@link ContentRepository}
     */
    public static ContentRepository loadTestRepository() {
        return new ContentRepository(ObjectFactory.createProcessor(), new File("src/test/resources/examples/repository").toURI());
    }

    public static String serialize(final Document doc) {
        try ( final StringWriter writer = new StringWriter() ) {
            final Transformer transformer = ObjectFactory.createTransformer(true);
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (final IOException | TransformerException e) {
            throw new IllegalStateException("Can not serialize document", e);
        }
    }

    public static String serialize(final XdmNode node) {
        return serialize((Document) NodeOverNodeInfo.wrap(node.getUnderlyingNode()));
    }

}
