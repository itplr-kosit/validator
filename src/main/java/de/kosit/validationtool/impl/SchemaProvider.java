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
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import de.kosit.validationtool.impl.xml.ClassPathResourceResolver;

/**
 * @author Andreas Penski
 */
public class SchemaProvider {

    private static Schema reportInputSchema;

    /**
     * Liefert das definierte Schema für die Validierung des [@link CreateReportInput}
     *
     * @return ReportInput-Schema
     */
    public static Schema getReportInputSchema() {
        if (reportInputSchema == null) {
            final SchemaFactory sf = ResolvingMode.STRICT_RELATIVE.getStrategy().createSchemaFactory();
            final Source source = resolve(SchemaProvider.class.getResource("/xsd/createReportInput.xsd"));
            reportInputSchema = createSchema(sf, new Source[] { source }, new ClassPathResourceResolver("/xsd"));
        }
        return reportInputSchema;
    }

    private static Schema createSchema(final SchemaFactory sf, final Source[] schemaSources, final LSResourceResolver resourceResolver) {
        try {
            sf.setResourceResolver(resourceResolver);
            return sf.newSchema(schemaSources);
        } catch (final SAXException e) {
            throw new IllegalArgumentException("Can not load schema from sources " + schemaSources[0].getSystemId(), e);
        }
    }

    private static Schema createSchema(final SchemaFactory sf, final Source... schemaSources) {
        return createSchema(sf, schemaSources, null);
    }

    @SuppressWarnings("java:S2095") // xml stack requires not closing the resource here
    private static Source resolve(final URL resource) {
        try {
            final String rawPath = resource.toURI().getRawPath();
            return new StreamSource(resource.openStream(), rawPath);
        } catch (final IOException | URISyntaxException e) {
            throw new IllegalStateException("Can not load schema for resource " + resource.getPath(), e);
        }
    }

    /**
     * Liefert das definiert Schema für die Szenario-Konfiguration
     *
     * @return Scenario-Schema
     */
    public static Schema getScenarioSchema() {
        final SchemaFactory sf = ResolvingMode.STRICT_RELATIVE.getStrategy().createSchemaFactory();
        return createSchema(sf, resolve(SchemaProvider.class.getResource("/xsd/scenarios.xsd")));
    }

}
