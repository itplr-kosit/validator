package org.kosit.validator.scenario.xsd;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.kosit.jaxb.xml.XMLHelper;
import org.xml.sax.SAXException;

/**
 * Provides the XML Schema for validating a scenario configuration. The schema is built with the hardened
 * {@link SchemaFactory} of {@link XMLHelper}.
 */
public final class ScenarioSchemaProvider {

    private static Schema scenarioSchema;

    private ScenarioSchemaProvider() {
    }

    private static Source resolve(final URL resource) {
        try {
            final String rawPath = resource.toURI().getRawPath();
            return new StreamSource(resource.openStream(), rawPath);
        } catch (final IOException | URISyntaxException e) {
            throw new IllegalStateException("Can not load schema for resource " + resource.getPath(), e);
        }
    }

    /**
     * Returns the defined schema for the scenario configuration.
     *
     * @return scenario schema
     */
    public static Schema getScenarioSchema() {
        if (scenarioSchema == null) {
            final Source source = resolve(ScenarioSchemaProvider.class.getResource(ScenarioSchemas.SCENARIOS_XSD_PATH));
            try {
                scenarioSchema = XMLHelper.createSafeSchemaFactory().newSchema(source);
            } catch (final SAXException e) {
                throw new IllegalArgumentException("Can not load schema from source " + source.getSystemId(), e);
            }
        }
        return scenarioSchema;
    }
}
