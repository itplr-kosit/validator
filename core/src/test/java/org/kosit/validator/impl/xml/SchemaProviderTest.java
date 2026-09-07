package org.kosit.validator.impl.xml;

import javax.xml.validation.SchemaFactory;

import org.kosit.schematron.resolve.ResolvingMode;

/**
 * @author Andreas Penski
 */
public class SchemaProviderTest {

    private final SchemaFactory schemaFactory = ResolvingMode.STRICT_RELATIVE.getStrategy().createSchemaFactory();

}
