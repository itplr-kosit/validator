package org.kosit.validator.impl;

import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.kosit.base.xml.SchemaResolver;
import org.kosit.validator.impl.xml.ClassPathResourceResolver;
import org.kosit.xvrl.impl.XvrlConversionService;
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
            final Source source = SchemaResolver.resolve(XvrlConversionService.class.getResource(XvrlConversionService.XVRL_XSD_PATH));
            xvrlSchema = createSchema(sf, new Source[] { source }, new ClassPathResourceResolver(XvrlConversionService.XSD_PATH));
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

}
