package org.kosit.validator.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.kosit.validator.api.xsd.ValidatorSchemas;
import org.kosit.validator.impl.xml.ClassPathResourceResolver;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

/**
 * @author Andreas Penski
 */
public class SchemaProvider {

    private static Schema xvrlSchema;

    private SchemaProvider() {

    }

    /**
     * Returns the defined schema for validating the an XVRL report.
     *
     * @return ReportInput schema
     */
    public static Schema getXVRLSchema() {
        if (xvrlSchema == null) {
            final SchemaFactory sf = ResolvingMode.STRICT_RELATIVE.getStrategy().createSchemaFactory();
            final Source source = resolve(SchemaProvider.class.getResource(ValidatorSchemas.XVRL_XSD_PATH));
            xvrlSchema = createSchema(sf, new Source[] { source }, new ClassPathResourceResolver(ValidatorSchemas.XSD_PATH));
        }
        return xvrlSchema;
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
        final SchemaFactory sf = ResolvingMode.STRICT_RELATIVE.getStrategy().createSchemaFactory();
        return createSchema(sf, resolve(SchemaProvider.class.getResource(ValidatorSchemas.SCENARIOS_XSD_PATH)));
    }

}
