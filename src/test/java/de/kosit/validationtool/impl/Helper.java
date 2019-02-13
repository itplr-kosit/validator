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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

/**
 * Helferlein für Test-Artefakte
 * 
 * @author Andreas Penski
 */
public class Helper {

    public static final URI SOURCE_ROOT = Paths.get("src/main/resources").toUri();

    public static final URI MODEL_ROOT = Paths.get("src/main/model").toUri();

    public static final URI ASSERTION_SCHEMA = MODEL_ROOT.resolve("xsd/assertions.xsd");

    public static final URI SCENARIO_SCHEMA = MODEL_ROOT.resolve("xsd/scenarios.xsd");

    public static final URI TEST_ROOT = Paths.get("src/test/resources").toUri();

    public static final URI EXAMPLES_DIR = TEST_ROOT.resolve("examples/");

    public static final URI ASSERTIONS = EXAMPLES_DIR.resolve("assertions/tests-xrechnung.xml");

    public static final URI SCENARIO_FILE = EXAMPLES_DIR.resolve("UBLReady/scenarios-2.xml");

    public static final URI REPOSITORY = EXAMPLES_DIR.resolve("repository/");

    public static final URI NOT_EXISTING = EXAMPLES_DIR.resolve("doesnotexist");

    public static final URI SAMPLE_DIR = EXAMPLES_DIR.resolve("UBLReady/");

    public static final URI SAMPLE_XSLT = EXAMPLES_DIR.resolve("repository/resources/eRechnung/report.xsl");

    public static final URI SAMPLE = SAMPLE_DIR.resolve("UBLReady_EU_UBL-NL_20170102_FULL.xml");

    public static final URI SAMPLE2 = SAMPLE_DIR.resolve("UBLReady_EU_UBL-NL_20170102_FULL-invalid.xml");

    /**
     * Lädt ein XML-Dokument von der gegebenen URL
     * 
     * @param url die url die geladen werden soll
     * @return ein result objekt mit Dokument
     */
    public static XdmNode load(URL url) {
        try ( InputStream input = url.openStream() ) {
            return ObjectFactory.createProcessor().newDocumentBuilder().build(new StreamSource(input));
        } catch (SaxonApiException | IOException e) {
            throw new IllegalStateException("Fehler beim Laden der XML-Datei", e);

        }

    }

    public static <T> T load(URL url, Class<T> type) throws URISyntaxException {
        ConversionService c = new ConversionService();
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

}
