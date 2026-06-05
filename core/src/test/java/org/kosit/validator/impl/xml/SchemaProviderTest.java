package org.kosit.validator.impl.xml;

import javax.xml.validation.SchemaFactory;

import org.kosit.validator.impl.ResolvingMode;

/**
 * @author Andreas Penski
 */
public class SchemaProviderTest {

    private final SchemaFactory schemaFactory = ResolvingMode.STRICT_RELATIVE.getStrategy().createSchemaFactory();

}
